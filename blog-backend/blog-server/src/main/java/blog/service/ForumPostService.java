package blog.service;

import blog.dto.ForumPostAdminUpdateDTO;
import blog.dto.ForumPostDTO;
import blog.dto.PostCreateDTO;
import blog.dto.PostQueryDTO;
import blog.result.PageResult;
import blog.vo.ForumPostVO;
import blog.vo.PostDetailVO;
import blog.vo.PostVO;

import java.util.List;
import java.util.Map;

public interface ForumPostService {



    Map<String, Object> listPosts(String sort, int page, int size);

    ForumPostVO getPostById(Long id);

    ForumPostVO createPost(ForumPostDTO forumPostDTO);

    Map<String, List<ForumPostVO>> getSidebarData(Long currentPostId, int limit);

    Map<String, Object> listAdminPosts(int page, int size, String keyword);

    void updateAdminPostMeta(Long id, ForumPostAdminUpdateDTO updateDTO);

    void deletePost(Long id);

    void touchLastActivityByPage(String page);

    Long countTotal();



    /**
     * 获取文章列表（分页+排序）
     */
    PageResult<PostVO> getPostList(PostQueryDTO query);

    /**
     * 获取文章详情
     */
    PostDetailVO getPostDetail(Long postId);

    /**
     * 创建文章（新方法）
     */
    Long createNewPost(PostCreateDTO dto, Long userId, String nickname, String email, String avatar);

    /**
     * 更新文章
     */
    void updatePost(Long postId, PostCreateDTO dto, Long userId);

    /**
     * 删除文章（带权限控制）
     */
    void deletePostById(Long postId, Long userId, boolean isAdmin);

    /**
     * 获取用户的所有文章
     */
    PageResult<PostVO> getUserPostsByUserId(Long userId, Integer page, Integer size);

    /**
     * 获取置顶/精华文章
     */
    List<PostVO> getPromotedPostList();

    /**
     * 文章评论数 +1
     */
    void incrementCommentCount(Long postId);

    /**
     * 点赞 +1
     */
    void likePost(Long postId, Long userId);

    /**
     * 取消点赞（如需）
     */
    void unlikePost(Long postId, Long userId);
}
