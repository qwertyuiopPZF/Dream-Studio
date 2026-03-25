package blog.mapper;

import blog.entity.UserNotification;
import blog.vo.UserNotificationVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserNotificationMapper
{
    @Insert("""
            INSERT INTO user_notification(user_id, type, title, content, target_type, target_id, related_report_id, is_read, create_time, read_time)
            VALUES(#{userId}, #{type}, #{title}, #{content}, #{targetType}, #{targetId}, #{relatedReportId}, #{isRead}, #{createTime}, #{readTime})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(UserNotification notification);

    @Select("""
            SELECT id,
                   user_id AS userId,
                   type,
                   title,
                   content,
                   target_type AS targetType,
                   target_id AS targetId,
                   related_report_id AS relatedReportId,
                   is_read AS notificationRead,
                   create_time AS createTime,
                   read_time AS readTime
            FROM user_notification
            WHERE user_id = #{userId}
            ORDER BY is_read ASC, create_time DESC
            LIMIT #{limit}
            """)
    @Results({
            @Result(property = "read", column = "notificationRead")
    })
    List<UserNotificationVO> selectByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM user_notification WHERE user_id = #{userId} AND is_read = FALSE")
    Long countUnreadByUserId(Long userId);

    @Update("""
            UPDATE user_notification
            SET is_read = TRUE,
                read_time = #{readTime}
            WHERE id = #{id}
              AND user_id = #{userId}
            """)
    int markAsRead(@Param("userId") Long userId, @Param("id") Long id, @Param("readTime") LocalDateTime readTime);
}
