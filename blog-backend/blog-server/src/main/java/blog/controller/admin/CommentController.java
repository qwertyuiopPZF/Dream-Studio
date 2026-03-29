package blog.controller.admin;

import blog.result.Result;
import blog.service.AccessControlService;
import blog.service.CommentService;
import blog.vo.CommentVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("adminCommentsController")
@RequestMapping("/admin/comments")
@Slf4j
@ApiOperation("评论管理")
public class CommentController
{
    @Autowired
    private CommentService commentService;

    @Autowired
    private AccessControlService accessControlService;

    @PutMapping("/status")
    @ApiOperation("更新评论公开状态")
    public Result<String> updateStatus(@RequestParam Long id,
                                       @RequestParam Boolean status,
                                       Authentication authentication)
    {
        if (authentication == null || authentication.getName() == null) {
            return Result.error(401, "请先登录");
        }

        try {
            accessControlService.requireAdmin(authentication.getName());
            commentService.updateStatus(id, status);
            return Result.success("操作成功");
        } catch (IllegalArgumentException e) {
            return Result.error(403, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除评论")
    public Result<String> deleteCommentById(@PathVariable Long id,
                                            Authentication authentication)
    {
        if (authentication == null || authentication.getName() == null) {
            return Result.error(401, "请先登录");
        }

        try {
            accessControlService.requireAdmin(authentication.getName());
            commentService.deleteCommentById(id);
            return Result.success("评论删除成功");
        } catch (IllegalArgumentException e) {
            return Result.error(403, e.getMessage());
        }
    }

    @GetMapping
    @ApiOperation("查询评论详情")
    public Result<List<CommentVO>> getComments(@RequestParam(required = false) String page,
                                               @RequestParam(required = false) Long blogId,
                                               Authentication authentication)
    {
        if (authentication == null || authentication.getName() == null) {
            return Result.error(401, "请先登录");
        }

        try {
            accessControlService.requireAdmin(authentication.getName());
            return Result.success(commentService.getComments(page, blogId, -1L));
        } catch (IllegalArgumentException e) {
            return Result.error(403, e.getMessage());
        }
    }
}
