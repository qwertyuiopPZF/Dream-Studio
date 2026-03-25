package blog.mapper;

import blog.entity.ForumReport;
import blog.vo.ForumReportVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ForumReportMapper
{
    @Insert("""
            INSERT INTO forum_report(reporter_id, target_type, target_id, target_title, reason, detail, status, reviewer_id, reviewer_note, create_time, update_time)
            VALUES(#{reporterId}, #{targetType}, #{targetId}, #{targetTitle}, #{reason}, #{detail}, #{status}, #{reviewerId}, #{reviewerNote}, #{createTime}, #{updateTime})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ForumReport report);

    @Select("""
            SELECT fr.id,
                   fr.reporter_id AS reporterId,
                   reporter.nickname AS reporterName,
                   fr.target_type AS targetType,
                   fr.target_id AS targetId,
                   fr.target_title AS targetTitle,
                   fr.reason,
                   fr.detail,
                   fr.status,
                   fr.reviewer_id AS reviewerId,
                   reviewer.nickname AS reviewerName,
                   fr.reviewer_note AS reviewerNote,
                   fr.create_time AS createTime,
                   fr.update_time AS updateTime
            FROM forum_report fr
            LEFT JOIN user_account reporter ON reporter.id = fr.reporter_id
            LEFT JOIN user_account reviewer ON reviewer.id = fr.reviewer_id
            WHERE fr.id = #{id}
            LIMIT 1
            """)
    ForumReportVO selectById(Long id);

    @Select("""
            SELECT fr.id,
                   fr.reporter_id AS reporterId,
                   reporter.nickname AS reporterName,
                   fr.target_type AS targetType,
                   fr.target_id AS targetId,
                   fr.target_title AS targetTitle,
                   fr.reason,
                   fr.detail,
                   fr.status,
                   fr.reviewer_id AS reviewerId,
                   reviewer.nickname AS reviewerName,
                   fr.reviewer_note AS reviewerNote,
                   fr.create_time AS createTime,
                   fr.update_time AS updateTime
            FROM forum_report fr
            LEFT JOIN user_account reporter ON reporter.id = fr.reporter_id
            LEFT JOIN user_account reviewer ON reviewer.id = fr.reviewer_id
            WHERE fr.reporter_id = #{reporterId}
            ORDER BY CASE WHEN fr.status = 'PENDING' THEN 0 ELSE 1 END, fr.create_time DESC
            LIMIT #{limit}
            """)
    List<ForumReportVO> selectByReporterId(@Param("reporterId") Long reporterId, @Param("limit") int limit);

    @Select("""
            SELECT fr.id,
                   fr.reporter_id AS reporterId,
                   reporter.nickname AS reporterName,
                   fr.target_type AS targetType,
                   fr.target_id AS targetId,
                   fr.target_title AS targetTitle,
                   fr.reason,
                   fr.detail,
                   fr.status,
                   fr.reviewer_id AS reviewerId,
                   reviewer.nickname AS reviewerName,
                   fr.reviewer_note AS reviewerNote,
                   fr.create_time AS createTime,
                   fr.update_time AS updateTime
            FROM forum_report fr
            LEFT JOIN user_account reporter ON reporter.id = fr.reporter_id
            LEFT JOIN user_account reviewer ON reviewer.id = fr.reviewer_id
            ORDER BY CASE WHEN fr.status = 'PENDING' THEN 0 ELSE 1 END, fr.create_time DESC
            LIMIT #{limit}
            """)
    List<ForumReportVO> selectAll(@Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM forum_report WHERE reporter_id = #{reporterId} AND status = 'PENDING'")
    Long countPendingByReporterId(Long reporterId);

    @Select("SELECT COUNT(*) FROM forum_report WHERE status = 'PENDING'")
    Long countPending();

    @Update("""
            UPDATE forum_report
            SET status = #{status},
                reviewer_id = #{reviewerId},
                reviewer_note = #{reviewerNote},
                update_time = #{updateTime}
            WHERE id = #{id}
            """)
    void updateReview(ForumReport report);
}
