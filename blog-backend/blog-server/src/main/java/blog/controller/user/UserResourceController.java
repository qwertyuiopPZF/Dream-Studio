package blog.controller.user;

import blog.result.Result;
import blog.service.ResourceService;
import blog.vo.ResourceVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/user/resources")
public class UserResourceController
{
    private final ResourceService resourceService;

    public UserResourceController(ResourceService resourceService)
    {
        this.resourceService = resourceService;
    }

    @GetMapping
    @ApiOperation("获取当前用户上传的资源")
    public Result<List<ResourceVO>> listMyResources(Authentication authentication)
    {
        if (authentication == null || authentication.getName() == null) {
            return Result.error(401, "未登录");
        }

        try {
            return Result.success(resourceService.listMyResources(authentication.getName()));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping
    @ApiOperation("上传资源")
    public Result<ResourceVO> uploadResource(@RequestParam("file") MultipartFile file,
                                             Authentication authentication)
    {
        if (authentication == null || authentication.getName() == null) {
            return Result.error(401, "未登录");
        }

        try {
            return Result.success(resourceService.uploadResource(authentication.getName(), file));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除资源")
    public Result<Void> deleteResource(@PathVariable Long id,
                                       Authentication authentication)
    {
        if (authentication == null || authentication.getName() == null) {
            return Result.error(401, "未登录");
        }

        try {
            resourceService.deleteResource(authentication.getName(), id);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
}
