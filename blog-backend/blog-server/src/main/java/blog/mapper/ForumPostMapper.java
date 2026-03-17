package blog.mapper;

import blog.entity.ForumPost;
import blog.vo.ForumPostVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ForumPostMapper
{
    List<ForumPostVO> selectPage(@Param("sort") String sort,
                                 @Param("offset") int offset,
                                 @Param("size") int size,
                                 @Param("keyword") String keyword,
                                 @Param("excludeId") Long excludeId,
                                 @Param("featuredOnly") Boolean featuredOnly);

    Long countPage(@Param("keyword") String keyword,
                   @Param("excludeId") Long excludeId,
                   @Param("featuredOnly") Boolean featuredOnly);

    ForumPostVO selectById(Long id);

    void updateMeta(ForumPost forumPost);

    @Insert("INSERT INTO forum_post(author_id, title, summary, content, nickname, email, avatar, view_count, is_pinned, is_featured, create_time, update_time, last_activity_time) " +
            "VALUES(#{authorId}, #{title}, #{summary}, #{content}, #{nickname}, #{email}, #{avatar}, #{viewCount}, #{isPinned}, #{isFeatured}, #{createTime}, #{updateTime}, #{lastActivityTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ForumPost forumPost);

    @Update("UPDATE forum_post SET view_count = view_count + 1 WHERE id = #{id}")
    void incrementViewCount(Long id);

    @Update("UPDATE forum_post SET last_activity_time = #{activityTime} WHERE id = #{id}")
    void updateLastActivityTime(@Param("id") Long id, @Param("activityTime") LocalDateTime activityTime);

    @Delete("DELETE FROM forum_post WHERE id = #{id}")
    void deleteById(Long id);

    @Select("SELECT COUNT(*) FROM forum_post")
    Long countAll();

    @Select("SELECT COUNT(*) FROM forum_post WHERE is_pinned = TRUE")
    Long countPinned();

    @Select("SELECT COUNT(*) FROM forum_post WHERE is_featured = TRUE")
    Long countFeatured();
}
