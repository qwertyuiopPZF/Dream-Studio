package blog.service;

import blog.dto.ForumPostAdminUpdateDTO;
import blog.dto.ForumPostDTO;
import blog.vo.ForumPostVO;

import java.util.List;
import java.util.Map;

public interface ForumPostService
{
    Map<String, Object> listPosts(String sort, int page, int size);

    ForumPostVO getPostById(Long id);

    ForumPostVO createPost(ForumPostDTO forumPostDTO);

    Map<String, List<ForumPostVO>> getSidebarData(Long currentPostId, int limit);

    Map<String, Object> listAdminPosts(int page, int size, String keyword);

    void updateAdminPostMeta(Long id, ForumPostAdminUpdateDTO updateDTO);

    void deletePost(Long id);

    void touchLastActivityByPage(String page);
}
