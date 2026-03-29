package blog.controller.admin;

import blog.dto.CategoryDTO;
import blog.result.Result;
import blog.service.AccessControlService;
import blog.service.CategoryService;
import blog.vo.CategoryVO;
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

@RestController("adminCategoriesController")
@RequestMapping("/admin/categories")
@Slf4j
@ApiOperation("分类管理页面")
public class CategoryController
{
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AccessControlService accessControlService;

    @PostMapping
    @ApiOperation("创建分类")
    public Result<String> createCategory(@RequestBody CategoryDTO categoryDTO,
                                         Authentication authentication)
    {
        try {
            accessControlService.requireAdmin(requireUsername(authentication));
            categoryService.insert(categoryDTO);
            return Result.success("分类创建成功");
        } catch (IllegalArgumentException e) {
            return Result.error(403, e.getMessage());
        } catch (RuntimeException e) {
            log.warn("创建分类失败：{}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @PutMapping
    @ApiOperation("修改分类")
    public Result<String> update(@RequestBody CategoryDTO categoryDTO,
                                 Authentication authentication)
    {
        try {
            accessControlService.requireAdmin(requireUsername(authentication));
            categoryService.update(categoryDTO);
            return Result.success("修改分类成功");
        } catch (IllegalArgumentException e) {
            return Result.error(403, e.getMessage());
        } catch (RuntimeException e) {
            log.warn("修改分类失败：{}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @GetMapping
    @ApiOperation("查询所有分类")
    public Result<List<CategoryVO>> selectAll(Authentication authentication)
    {
        try {
            accessControlService.requireAdmin(requireUsername(authentication));
            return Result.success(categoryService.selectAll());
        } catch (IllegalArgumentException e) {
            return Result.error(403, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation("根据id删除分类")
    public Result<Void> deleteCategory(@PathVariable Long id,
                                       Authentication authentication)
    {
        try {
            accessControlService.requireAdmin(requireUsername(authentication));
            categoryService.delete(id);
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
