package blog.mapper;

import blog.entity.ForumPost;
import blog.vo.ForumPostVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
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
                                 @Param("authorId") Long authorId,
                                 @Param("excludeId") Long excludeId,
                                 @Param("featuredOnly") Boolean featuredOnly,
                                 @Param("status") Integer status);

    Long countPage(@Param("keyword") String keyword,
                   @Param("authorId") Long authorId,
                   @Param("excludeId") Long excludeId,
                   @Param("featuredOnly") Boolean featuredOnly,
                   @Param("status") Integer status);

    ForumPostVO selectById(@Param("id") Long id, @Param("status") Integer status);

    List<ForumPostVO> selectByAuthorId(@Param("authorId") Long authorId, @Param("limit") int limit);

    void updateMeta(ForumPost forumPost);

    @Update("UPDATE forum_post SET status = #{status}, update_time = NOW() WHERE id = #{id}")
    void updateStatus(@Param("id") Long id, @Param("status") Integer status);

    void insert(ForumPost forumPost);

    @Update("UPDATE forum_post SET view_count = view_count + 1 WHERE id = #{id}")
    void incrementViewCount(Long id);

    @Update("UPDATE forum_post SET last_activity_time = #{activityTime} WHERE id = #{id}")
    void updateLastActivityTime(@Param("id") Long id, @Param("activityTime") LocalDateTime activityTime);

    @Delete("DELETE FROM forum_post WHERE id = #{id}")
    void deleteById(Long id);

    @Select("SELECT COUNT(*) FROM forum_post")
    Long countAll();

    @Select("SELECT COUNT(*) FROM forum_post WHERE status = 1 AND is_pinned = TRUE")
    Long countPinned();

    @Select("SELECT COUNT(*) FROM forum_post WHERE status = 1 AND is_featured = TRUE")
    Long countFeatured();

    @Select("SELECT COUNT(*) FROM forum_post WHERE author_id = #{authorId}")
    Long countByAuthorId(Long authorId);
}
