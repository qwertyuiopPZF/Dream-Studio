package blog.controller.user;

import blog.result.Result;
import blog.service.ResourceService;
import blog.vo.ResourceVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
public class ResourceController
{
    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService)
    {
        this.resourceService = resourceService;
    }

    @GetMapping
    @ApiOperation("获取公开资源列表")
    public Result<List<ResourceVO>> listPublicResources()
    {
        return Result.success(resourceService.listPublicResources());
    }

    @PostMapping("/{id}/downloads")
    @ApiOperation("记录资源下载次数")
    public Result<Void> recordDownload(@PathVariable Long id)
    {
        try {
            resourceService.recordDownload(id);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
}
