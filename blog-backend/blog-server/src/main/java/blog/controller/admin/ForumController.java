package blog.controller.admin;

import blog.dto.ForumPostAdminUpdateDTO;
import blog.entity.UserAccount;
import blog.result.Result;
import blog.service.AccessControlService;
import blog.service.ForumPostService;
import blog.vo.ForumPostVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController("adminForumController")
@RequestMapping("/admin/forum")
@Slf4j
@ApiOperation("论坛管理")
public class ForumController
{
    @Autowired
    private ForumPostService forumPostService;

    @Autowired
    private AccessControlService accessControlService;

    @GetMapping("/posts")
    @ApiOperation("查询论坛帖子列表")
    public Result<Map<String, Object>> getPosts(@RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size,
                                                @RequestParam(required = false) String keyword,
                                                Authentication authentication)
    {
        if (authentication == null || authentication.getName() == null) {
            return Result.error(401, "请先登录");
        }

        UserAccount currentUser = accessControlService.requireUser(authentication.getName());
        Long authorId = accessControlService.isAdmin(currentUser) ? null : currentUser.getId();
        return Result.success(forumPostService.listAdminPosts(page, size, keyword, authorId));
    }

    @PutMapping("/posts/{id}")
    @ApiOperation("更新论坛帖子状态")
    public Result<String> updatePostMeta(@PathVariable Long id,
                                         @RequestBody Map<String, Object> payload,
                                         Authentication authentication)
    {
        ForumPostAdminUpdateDTO updateDTO = buildForumPostAdminUpdateDTO(payload);

        if (authentication == null || authentication.getName() == null) {
            return Result.error(401, "请先登录");
        }

        try {
            accessControlService.requireAdmin(authentication.getName());
            forumPostService.updateAdminPostMeta(id, updateDTO);
            return Result.success("帖子状态更新成功");
        } catch (IllegalArgumentException e) {
            return Result.error(403, e.getMessage());
        }
    }

    @PutMapping("/posts/{id}/status")
    @ApiOperation("更新论坛帖子可见状态")
    public Result<String> updatePostStatus(@PathVariable Long id,
                                           @RequestBody Map<String, Object> payload,
                                           Authentication authentication)
    {
        if (authentication == null || authentication.getName() == null) {
            return Result.error(401, "请先登录");
        }

        ForumPostVO existingPost = forumPostService.findPostById(id);
        if (existingPost == null) {
            return Result.error("帖子不存在");
        }

        try {
            accessControlService.requireAdminOrOwner(authentication.getName(), existingPost.getAuthorId(), "该帖子");
            forumPostService.updatePostStatus(id, asInteger(payload.get("status")));
            return Result.success("帖子状态更新成功");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/posts/{id}")
    @ApiOperation("删除论坛帖子")
    public Result<String> deletePost(@PathVariable Long id,
                                     Authentication authentication)
    {
        if (authentication == null || authentication.getName() == null) {
            return Result.error(401, "请先登录");
        }

        ForumPostVO existingPost = forumPostService.findPostById(id);
        if (existingPost == null) {
            return Result.error("帖子不存在");
        }

        try {
            accessControlService.requireAdminOrOwner(authentication.getName(), existingPost.getAuthorId(), "该帖子");
            forumPostService.deletePost(id);
            return Result.success("帖子删除成功");
        } catch (IllegalArgumentException e) {
            return Result.error(403, e.getMessage());
        }
    }

    private ForumPostAdminUpdateDTO buildForumPostAdminUpdateDTO(Map<String, Object> payload)
    {
        ForumPostAdminUpdateDTO updateDTO = new ForumPostAdminUpdateDTO();
        updateDTO.setIsPinned(asBoolean(payload.get("isPinned")));
        updateDTO.setIsFeatured(asBoolean(payload.get("isFeatured")));
        updateDTO.setCategoryId(asLong(payload.get("categoryId")));
        updateDTO.setTags(asString(payload.get("tags")));
        return updateDTO;
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

    private Boolean asBoolean(Object value)
    {
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private Integer asInteger(Object value)
    {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return null;
        }
        return Integer.parseInt(text);
    }
}
