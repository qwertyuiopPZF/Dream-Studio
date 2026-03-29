package blog.controller.user;

import blog.dto.ResourceReviewDTO;
import blog.result.Result;
import blog.service.ResourceService;
import blog.vo.ResourceVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user/admin/resources")
public class AdminResourceController
{
    private final ResourceService resourceService;

    public AdminResourceController(ResourceService resourceService)
    {
        this.resourceService = resourceService;
    }

    @GetMapping
    @ApiOperation("管理员获取资源审核列表")
    public Result<List<ResourceVO>> listAdminResources(@RequestParam(required = false) String status,
                                                       Authentication authentication)
    {
        if (authentication == null || authentication.getName() == null) {
            return Result.error(401, "未登录");
        }

        try {
            return Result.success(resourceService.listAdminResources(authentication.getName(), status));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/review")
    @ApiOperation("管理员审核资源")
    public Result<ResourceVO> reviewResource(@PathVariable Long id,
                                             @RequestBody ResourceReviewDTO request,
                                             Authentication authentication)
    {
        if (authentication == null || authentication.getName() == null) {
            return Result.error(401, "未登录");
        }

        try {
            return Result.success(resourceService.reviewResource(authentication.getName(), id, request));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
}
