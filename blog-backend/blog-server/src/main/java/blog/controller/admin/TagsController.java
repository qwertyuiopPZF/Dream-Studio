package blog.controller.admin;

import blog.dto.TagsDTO;
import blog.result.Result;
import blog.service.AccessControlService;
import blog.service.TagsService;
import blog.vo.TagsVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
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

import java.util.List;

@RestController("adminTagsController")
@RequestMapping("/admin/tags")
@Slf4j
@ApiOperation("标签管理页面")
public class TagsController
{
    @Autowired
    private TagsService tagsService;

    @Autowired
    private AccessControlService accessControlService;

    @PostMapping
    @ApiOperation("创建标签")
    public Result<String> createTags(@RequestBody TagsDTO tagsDTO,
                                     Authentication authentication)
    {
        try {
            accessControlService.requireAdmin(requireUsername(authentication));
            tagsService.insertTags(tagsDTO);
            return Result.success("标签创建成功");
        } catch (IllegalArgumentException e) {
            return Result.error(403, e.getMessage());
        } catch (RuntimeException e) {
            log.warn("创建标签失败：{}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @PutMapping
    @ApiOperation("修改标签")
    public Result<String> updateTag(@RequestBody TagsDTO tagsDTO,
                                    Authentication authentication)
    {
        try {
            accessControlService.requireAdmin(requireUsername(authentication));
            tagsService.update(tagsDTO);
            return Result.success("标签修改成功");
        } catch (IllegalArgumentException e) {
            return Result.error(403, e.getMessage());
        } catch (RuntimeException e) {
            log.warn("修改标签失败：{}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/list")
    @ApiOperation("查询所有标签")
    public Result<List<TagsVO>> selectAll(Authentication authentication)
    {
        try {
            accessControlService.requireAdmin(requireUsername(authentication));
            return Result.success(tagsService.selectAll());
        } catch (IllegalArgumentException e) {
            return Result.error(403, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation("根据id删除标签")
    public Result<Void> deleteTags(@PathVariable Long id,
                                   Authentication authentication)
    {
        try {
            accessControlService.requireAdmin(requireUsername(authentication));
            tagsService.deleteTag(id);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.error(403, e.getMessage());
        }
    }

    private String requireUsername(Authentication authentication)
    {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalArgumentException("请先登录");
        }
        return authentication.getName();
    }
}
