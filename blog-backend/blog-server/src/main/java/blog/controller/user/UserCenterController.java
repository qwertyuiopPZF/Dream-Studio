package blog.controller.user;

import blog.dto.ForumPostAdminUpdateDTO;
import blog.dto.ForumReportCreateDTO;
import blog.dto.ForumReportReviewDTO;
import blog.result.Result;
import blog.service.UserCenterService;
import blog.vo.ForumReportVO;
import blog.vo.UserCenterOverviewVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserCenterController
{
    @Autowired
    private UserCenterService userCenterService;

    @GetMapping("/me/overview")
    @ApiOperation("获取个人中心概览")
    public Result<UserCenterOverviewVO> getOverview(Authentication authentication)
    {
        if (authentication == null || authentication.getName() == null) {
            return Result.error(401, "未登录");
        }

        try {
            return Result.success(userCenterService.getOverview(authentication.getName()));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/reports")
    @ApiOperation("提交论坛举报")
    public Result<ForumReportVO> createReport(@RequestBody ForumReportCreateDTO request,
                                              Authentication authentication)
    {
        if (authentication == null || authentication.getName() == null) {
            return Result.error(401, "未登录");
        }

        try {
            return Result.success(userCenterService.createReport(authentication.getName(), request));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/notifications/{id}/read")
    @ApiOperation("标记通知已读")
    public Result<Void> markNotificationAsRead(@PathVariable Long id,
                                               Authentication authentication)
    {
        if (authentication == null || authentication.getName() == null) {
            return Result.error(401, "未登录");
        }

        try {
            userCenterService.markNotificationAsRead(authentication.getName(), id);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/admin/reports/{id}")
    @ApiOperation("管理员处理举报")
    public Result<ForumReportVO> reviewReport(@PathVariable Long id,
                                              @RequestBody ForumReportReviewDTO request,
                                              Authentication authentication)
    {
        if (authentication == null || authentication.getName() == null) {
            return Result.error(401, "未登录");
        }

        try {
            return Result.success(userCenterService.reviewReport(authentication.getName(), id, request));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/admin/posts/{id}")
    @ApiOperation("管理员更新帖子状态")
    public Result<Void> updateAdminPostMeta(@PathVariable Long id,
                                            @RequestBody Map<String, Object> payload,
                                            Authentication authentication)
    {
        ForumPostAdminUpdateDTO request = buildForumPostAdminUpdateDTO(payload);

        if (authentication == null || authentication.getName() == null) {
            return Result.error(401, "未登录");
        }

        try {
            userCenterService.updateAdminPostMeta(authentication.getName(), id, request);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/admin/posts/{id}")
    @ApiOperation("管理员删除帖子")
    public Result<Void> deleteAdminPost(@PathVariable Long id,
                                        Authentication authentication)
    {
        if (authentication == null || authentication.getName() == null) {
            return Result.error(401, "未登录");
        }

        try {
            userCenterService.deleteAdminPost(authentication.getName(), id);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    private ForumPostAdminUpdateDTO buildForumPostAdminUpdateDTO(Map<String, Object> payload)
    {
        ForumPostAdminUpdateDTO request = new ForumPostAdminUpdateDTO();
        request.setIsPinned(asBoolean(payload.get("isPinned")));
        request.setIsFeatured(asBoolean(payload.get("isFeatured")));
        request.setCategoryId(asLong(payload.get("categoryId")));
        request.setTags(asString(payload.get("tags")));
        return request;
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
}
