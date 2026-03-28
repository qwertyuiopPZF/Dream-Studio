package blog.mapper;

<<<<<<< HEAD
import blog.dto.PostQueryDTO;
import blog.entity.ForumPost;
import blog.vo.ForumPostVO;
import org.apache.ibatis.annotations.Mapper;
=======
import blog.entity.ForumPost;
import blog.vo.ForumPostVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
<<<<<<< HEAD
public interface ForumPostMapper {

    // ========== 原有方法 ==========

=======
public interface ForumPostMapper
{
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
    List<ForumPostVO> selectPage(@Param("sort") String sort,
                                 @Param("offset") int offset,
                                 @Param("size") int size,
                                 @Param("keyword") String keyword,
<<<<<<< HEAD
                                 @Param("excludeId") Long excludeId,
                                 @Param("featuredOnly") boolean featuredOnly);

    Long countPage(@Param("keyword") String keyword,
                   @Param("excludeId") Long excludeId,
                   @Param("featuredOnly") boolean featuredOnly);

    ForumPostVO selectById(@Param("id") Long id);

    List<ForumPostVO> selectByAuthorId(@Param("authorId") Long authorId, @Param("limit") int limit);

    int updateMeta(ForumPost forumPost);

    int insert(ForumPost forumPost);

    @Update("DELETE FROM forum_post WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Update("UPDATE forum_post SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(@Param("id") Long id);
=======
                                 @Param("authorId") Long authorId,
                                 @Param("excludeId") Long excludeId,
                                 @Param("featuredOnly") Boolean featuredOnly);

    Long countPage(@Param("keyword") String keyword,
                   @Param("authorId") Long authorId,
                   @Param("excludeId") Long excludeId,
                   @Param("featuredOnly") Boolean featuredOnly);

    ForumPostVO selectById(Long id);

    List<ForumPostVO> selectByAuthorId(@Param("authorId") Long authorId, @Param("limit") int limit);

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
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8

    @Select("SELECT COUNT(*) FROM forum_post")
    Long countAll();

<<<<<<< HEAD
    @Select("SELECT COUNT(*) FROM forum_post WHERE is_featured = 1")
    Long countFeatured();

    @Select("SELECT COUNT(*) FROM forum_post WHERE is_pinned = 1")
    Long countPinned();

    @Update("UPDATE forum_post SET last_activity_time = #{time} WHERE id = #{id}")
    int updateLastActivityTime(@Param("id") Long id, @Param("time") LocalDateTime time);

    // ========== 新增方法 ==========

    /**
     * 统计作者文章总数
     */
    @Select("SELECT COUNT(*) FROM forum_post WHERE author_id = #{authorId}")
    Long countByAuthorId(@Param("authorId") Long authorId);

    /**
     * 分页查询文章列表（新增，返回实体）
     */
    List<ForumPost> selectPostListNew(@Param("query") PostQueryDTO query, @Param("offset") int offset);

    /**
     * 统计文章总数（新增）
     */
    Long selectPostCountNew(@Param("keyword") String keyword);

    /**
     * 根据ID查询文章（新增，返回实体）
     */
    ForumPost selectByIdEntity(@Param("id") Long id);

    /**
     * 插入新文章（新增）
     */
    int insertNew(ForumPost post);

    /**
     * 更新文章（新增）
     */
    int updateByIdEntity(ForumPost post);

    /**
     * 删除文章（新增）
     */
    int deleteByIdEntity(@Param("id") Long id);

    /**
     * 增加浏览量（新增）
     */
    int incrementViewCountNew(@Param("postId") Long postId);

    /**
     * 根据作者ID分页查询文章（新增）
     */
    List<ForumPost> selectByAuthorIdWithPageNew(@Param("authorId") Long authorId,
                                                @Param("offset") int offset,
                                                @Param("size") int size);

    /**
     * 统计作者文章数（新增）
     */
    Long countByAuthorIdNew(@Param("authorId") Long authorId);

    /**
     * 获取置顶/精华文章（新增）
     */
    List<ForumPost> selectPromotedPostsNew(@Param("limit") int limit);

    /**
     * 评论数 +1
     */
    @Update("UPDATE forum_post SET comment_count = comment_count + 1, last_activity_time = NOW() WHERE id = #{postId}")
    int incrementCommentCount(@Param("postId") Long postId);

    /**
     * 点赞 +1
     */
    @Update("UPDATE forum_post SET like_count = like_count + 1, last_activity_time = NOW() WHERE id = #{postId}")
    int incrementLikeCount(@Param("postId") Long postId);

    /**
     * 点赞 -1（不低于0）
     */
    @Update("UPDATE forum_post SET like_count = CASE WHEN like_count > 0 THEN like_count - 1 ELSE 0 END, last_activity_time = NOW() WHERE id = #{postId}")
    int decrementLikeCount(@Param("postId") Long postId);
=======
    @Select("SELECT COUNT(*) FROM forum_post WHERE is_pinned = TRUE")
    Long countPinned();

    @Select("SELECT COUNT(*) FROM forum_post WHERE is_featured = TRUE")
    Long countFeatured();

    @Select("SELECT COUNT(*) FROM forum_post WHERE author_id = #{authorId}")
    Long countByAuthorId(Long authorId);
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
}
