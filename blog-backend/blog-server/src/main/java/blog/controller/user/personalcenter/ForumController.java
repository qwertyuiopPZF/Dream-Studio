package blog.controller.user.personalcenter;

import blog.dto.ForumPostAdminUpdateDTO;
import blog.result.Result;
import blog.service.ForumPostService;
import blog.vo.ForumPostVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @GetMapping("/posts")
    @ApiOperation("查询论坛帖子列表")
    public Result<Map<String, Object>> getPosts(@RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size,
                                                @RequestParam(required = false) String keyword)
    {
        return Result.success(forumPostService.listAdminPosts(page, size, keyword));
    }

    @PutMapping("/posts/{id}")
    @ApiOperation("更新论坛帖子状态")
    public Result updatePostMeta(@PathVariable Long id,
                                 @RequestBody ForumPostAdminUpdateDTO updateDTO)
    {
        try {
            forumPostService.updateAdminPostMeta(id, updateDTO);
            return Result.success("帖子状态更新成功");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/posts/{id}")
    @ApiOperation("删除论坛帖子")
    public Result deletePost(@PathVariable Long id)
    {
        try {
            forumPostService.deletePost(id);
            return Result.success("帖子删除成功");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    /*
        删除评论/帖子
        审核帖子/评论
        是否置顶/加精，帖子/评论
     */

}
