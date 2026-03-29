package blog.controller.user;

import blog.dto.ForumPostDTO;
import blog.result.Result;
import blog.service.RateLimitService;
import blog.service.ForumPostService;
import blog.service.UserAccountService;
import blog.utils.IpUtils;
import blog.vo.ForumPostVO;
import blog.vo.UserProfileVO;
import blog.entity.UserAccount;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController("userPostController")
@RequestMapping("/api/post")
@Slf4j
@ApiOperation("论坛管理")
public class ForumController
{

    @Autowired
    private ForumPostService forumPostService;

    @Autowired
    private RateLimitService rateLimitService;

    @Autowired
    private UserAccountService userAccountService;

    @GetMapping
    @ApiOperation("获取帖子列表")
    public Result<Map<String, Object>> getPosts(@RequestParam(defaultValue = "latest") String sort,
                                                @RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size)
    {
        log.info("获取论坛帖子列表，排序方式：{}，页码：{}，每页数量：{}", sort, page, size);
        return Result.success(forumPostService.listPosts(sort, page, size));
    }

    @GetMapping("/{id}")
    @ApiOperation("获取帖子详情")
    public Result<ForumPostVO> getPostById(@PathVariable Long id)
    {
        log.info("获取论坛帖子详情，ID：{}", id);
        ForumPostVO forumPost = forumPostService.getPostById(id);
        if (forumPost == null) {
            return Result.error("帖子不存在");
        }
        return Result.success(forumPost);
    }

    @PostMapping
    @ApiOperation("发布帖子")
    public Result<ForumPostVO> createPost(@RequestBody Map<String, Object> payload,
                                          HttpServletRequest request,
                                          Authentication authentication)
    {
        ForumPostDTO forumPostDTO = buildForumPostDTO(payload);

        if (authentication == null || authentication.getName() == null) {
            return Result.error(401, "请先登录后再发帖");
        }

        UserProfileVO currentUser = userAccountService.getProfileByUsername(authentication.getName());
        if (currentUser == null) {
            return Result.error(401, "未找到当前登录用户资料");
        }

        forumPostDTO.setNickname(currentUser.getNickname());
        forumPostDTO.setEmail(currentUser.getEmail());
        forumPostDTO.setAvatar(currentUser.getAvatar());
        UserAccount currentUserAccount = userAccountService.findByUsername(authentication.getName());
        if (currentUserAccount != null) {
            forumPostDTO.setAuthorId(currentUserAccount.getId());
        }

        log.info("发布论坛帖子，标题：{}，用户：{}", forumPostDTO.getTitle(), authentication.getName());
        try {
            String clientIp = IpUtils.getClientIpAddress(request);
            boolean allowed = rateLimitService.tryAcquire("forum-post-create", clientIp, 5, 600);
            if (!allowed) {
                return Result.error("发帖过于频繁，请稍后再试");
            }
            return Result.success(forumPostService.createPost(forumPostDTO));
        } catch (IllegalArgumentException e) {
            log.warn("发布论坛帖子参数校验失败：{}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("发布论坛帖子失败", e);
            return Result.error("帖子发布失败，请稍后重试");
        }
    }

    @GetMapping("/sidebar")
    @ApiOperation("获取帖子详情侧栏数据")
    public Result<Map<String, List<ForumPostVO>>> getSidebarData(@RequestParam Long currentPostId,
                                                                 @RequestParam(defaultValue = "5") int limit)
    {
        return Result.success(forumPostService.getSidebarData(currentPostId, limit));
    }

    private ForumPostDTO buildForumPostDTO(Map<String, Object> payload)
    {
        ForumPostDTO forumPostDTO = new ForumPostDTO();
        forumPostDTO.setTitle(asString(payload.get("title")));
        forumPostDTO.setSummary(asString(payload.get("summary")));
        forumPostDTO.setContent(asString(payload.get("content")));
        forumPostDTO.setCategoryId(asLong(payload.get("categoryId")));
        forumPostDTO.setTags(asString(payload.get("tags")));
        return forumPostDTO;
    }

    private String asString(Object value)
    {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    private Long asLong(Object value)
    {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return null;
        }
        return Long.parseLong(text);
    }

}
