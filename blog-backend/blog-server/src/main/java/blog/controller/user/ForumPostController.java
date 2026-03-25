package blog.controller.user;

import blog.dto.PostCreateDTO;
import blog.dto.PostQueryDTO;
import blog.dto.CommentDTO;
import blog.entity.UserAccount;
import blog.result.PageResult;
import blog.result.Result;
import blog.service.CommentService;
import blog.service.ForumPostService;
import blog.service.UserAccountService;
import blog.utils.IpUtils;
import blog.vo.CommentVO;
import blog.vo.PostDetailVO;
import blog.vo.PostVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/forum")
@RequiredArgsConstructor
public class ForumPostController {

    private final ForumPostService forumPostService;
    private final UserAccountService userAccountService;
    private final CommentService commentService;

    /**
     * 获取文章列表（分页+排序）
     * GET /api/forum/posts?page=1&size=10&sortBy=time&order=desc
     */
    @GetMapping("/posts")
    public Result<PageResult<PostVO>> getPostList(@Valid PostQueryDTO query) {
        PageResult<PostVO> result = forumPostService.getPostList(query);
        return Result.success(result);
    }

    /**
     * 获取文章详情
     * GET /api/forum/posts/{id}
     */
    @GetMapping("/posts/{id}")
    public Result<PostDetailVO> getPostDetail(@PathVariable Long id) {
        PostDetailVO post = forumPostService.getPostDetail(id);
        return Result.success(post);
    }

    /**
     * 获取文章评论
     */
    @GetMapping("/posts/{id}/comments")
    public Result<List<CommentVO>> getPostComments(@PathVariable Long id) {
        List<CommentVO> comments = commentService.getComments("forum_post", id, -1L);
        return Result.success(comments);
    }

    /**
     * 文章发表评论
     */
    @PostMapping("/posts/{id}/comments")
    public Result<Void> createPostComment(@PathVariable Long id,
                                          @RequestBody CommentDTO dto,
                                          @RequestAttribute(value = "userId", required = false) Long userId,
                                          jakarta.servlet.http.HttpServletRequest request) {
        if (userId == null) {
            return Result.error(401, "请先登录再评论");
        }

        UserAccount user = userAccountService.findById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        if (dto == null || !StringUtils.hasText(dto.getContent())) {
            return Result.error("评论内容不能为空");
        }

        CommentDTO comment = new CommentDTO();
        comment.setPage("forum_post");
        comment.setBlogId(id);
        comment.setUserId(userId);
        comment.setNickname(user.getNickname());
        comment.setEmail(user.getEmail());
        comment.setAvatar(user.getAvatar());
        comment.setContent(dto.getContent().trim());
        comment.setParentCommentId(dto.getParentCommentId());
        comment.setStatus(true);
        comment.setIp(IpUtils.getClientIpAddress(request));

        commentService.createComment(comment);
        forumPostService.incrementCommentCount(id);

        return Result.success("评论成功", null);
    }

    /**
     * 发布文章
     * POST /api/forum/posts
     */
    @PostMapping("/posts")
    public Result<Map<String, Object>> createPost(
            @Valid @RequestBody PostCreateDTO dto,
            @RequestAttribute("userId") Long userId) {

        UserAccount user = userAccountService.findById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        Long postId = forumPostService.createNewPost(dto, userId, user.getNickname(), user.getEmail(), user.getAvatar());

        // 根据接口文档格式返回
        Map<String, Object> data = new HashMap<>();
        data.put("id", postId);
        data.put("title", dto.getTitle());
        data.put("createTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        return Result.success(data);
    }

    /**
     * 编辑文章
     * PUT /api/forum/posts/{id}
     */
    @PutMapping("/posts/{id}")
    public Result<Map<String, Object>> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody PostCreateDTO dto,
            @RequestAttribute("userId") Long userId) {

        forumPostService.updatePost(id, dto, userId);

        // 根据接口文档格式返回
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("title", dto.getTitle());
        data.put("updateTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        data.put("updatedFields", List.of("title", "content", "summary"));

        return Result.success("更新成功", data);
    }

    /**
     * 删除文章
     * DELETE /api/forum/posts/{id}
     */
    @DeleteMapping("/posts/{id}")
    public Result<Void> deletePost(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId) {

        forumPostService.deletePostById(id, userId, false);
        return Result.success("帖子删除成功", null);
    }

    /**
     * 获取用户的所有文章
     * GET /api/forum/user/{userId}/posts?page=1&size=10
     */
    @GetMapping("/user/{userId}/posts")
    public Result<PageResult<PostVO>> getUserPosts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        PageResult<PostVO> result = forumPostService.getUserPostsByUserId(userId, page, size);
        return Result.success(result);
    }

    /**
     * 获取置顶/精华文章
     * GET /api/forum/promoted
     */
    @GetMapping("/promoted")
    public Result<List<PostVO>> getPromotedPosts() {
        List<PostVO> posts = forumPostService.getPromotedPostList();
        return Result.success(posts);
    }

    /**
     * 点赞
     */
    @PostMapping("/posts/{id}/like")
    public Result<Void> likePost(@PathVariable Long id,
                                 @RequestAttribute(value = "userId", required = false) Long userId) {
        if (userId == null) {
            return Result.error(401, "请先登录再点赞");
        }
        forumPostService.likePost(id, userId);
        return Result.success("点赞成功", null);
    }

    /**
     * 取消点赞
     */
    @DeleteMapping("/posts/{id}/like")
    public Result<Void> unlikePost(@PathVariable Long id,
                                   @RequestAttribute(value = "userId", required = false) Long userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        forumPostService.unlikePost(id, userId);
        return Result.success("已取消点赞", null);
    }
}
