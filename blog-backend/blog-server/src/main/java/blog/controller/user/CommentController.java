package blog.controller.user;

import blog.dto.CommentDTO;
import blog.dto.CommentQueryDTO;
import blog.result.Result;
import blog.service.CommentService;
import blog.service.RateLimitService;
import blog.service.UserAccountService;
import blog.utils.IpUtils;
import blog.vo.CommentVO;
import blog.vo.UserProfileVO;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 评论管理控制器
 *
 * @author Eleven
 * @version 1.0
 */
@RestController("userCommentsController")
@RequestMapping("/api/comments")
@Slf4j
@ApiOperation("评论管理")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private RateLimitService rateLimitService;

    @Autowired
    private UserAccountService userAccountService;


    /**
     *   根据页面或文章id查询所有评论
     * 	 @param page     要查询的页面(博客文章or友链...)
     * 	 @param blogId   如果是博客文章页面 需要提供博客id
     */
    @GetMapping
    @ApiOperation("查询评论详情")
    public Result<List<CommentVO>> getComments(@RequestParam(required = false) String page,
                                               @RequestParam(required = false) Long blogId,
                                               Authentication authentication)
    {
        log.info("查询评论详情，ID：{}", blogId);

        List<CommentVO> comments = commentService.getComments(page,blogId,-1L);

        if (authentication != null && StringUtils.hasText(authentication.getName())) {
            blog.entity.UserAccount currentUser = userAccountService.findByUsername(authentication.getName());
            if (currentUser != null) {
                markOwnership(comments, currentUser.getId());
            }
        }

        return Result.success(comments);
    }

    /**
     * 提交评论
     */
    @PostMapping("/comment")
    @ApiOperation("提交评论")
    public Result postComment(
            @RequestBody CommentDTO commentDTO,
            jakarta.servlet.http.HttpServletRequest request,
            Authentication authentication)
    {

        try {
            if (authentication == null || !StringUtils.hasText(authentication.getName())) {
                return Result.error(401, "请先登录后再评论");
            }

            UserProfileVO currentUser = userAccountService.getProfileByUsername(authentication.getName());
            if (currentUser == null) {
                return Result.error(401, "未找到当前登录用户资料");
            }

            commentDTO.setNickname(currentUser.getNickname());
            commentDTO.setEmail(currentUser.getEmail());
            commentDTO.setAvatar(currentUser.getAvatar());
            blog.entity.UserAccount currentUserAccount = userAccountService.findByUsername(authentication.getName());
            if (currentUserAccount != null) {
                commentDTO.setUserId(currentUserAccount.getId());
            }

            String clientIp = IpUtils.getClientIpAddress(request);
            boolean allowed = rateLimitService.tryAcquire("forum-comment-create", clientIp, 12, 600);
            if (!allowed) {
                return Result.error("评论过于频繁，请稍后再试");
            }

            // 验证必填参数
            String content = commentDTO.getContent();
            String nickname = commentDTO.getNickname();

            if (content == null || content.trim().isEmpty()) {
                return Result.error("评论内容不能为空");
            }
            if (nickname == null || nickname.trim().isEmpty()) {
                return Result.error("昵称不能为空");
            }

            // 内容长度验证
            if (content.trim().length() > 1000) {
                return Result.error("评论内容不能超过1000字符");
            }
            if (nickname.trim().length() > 50) {
                return Result.error("昵称不能超过50字符");
            }

            // 创建评论DTO
            commentDTO.setNickname(nickname.trim());
            commentDTO.setContent(content.trim());
            commentDTO.setEmail(commentDTO.getEmail() != null ? commentDTO.getEmail() .trim() : "");
            commentDTO.setWebsite(commentDTO.getWebsite() != null ? commentDTO.getWebsite() .trim() : "");
            // 默认公开
            commentDTO.setStatus(true);
            // 获取真实用户IP
            commentDTO.setIp(clientIp);



            commentService.createComment(commentDTO);


        } catch (Exception e) {
            log.error("提交评论失败，错误：{}", e.getMessage(), e);
            return Result.error("评论提交失败，请稍后重试");
        }
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除评论")
    public Result<Void> deleteComment(@PathVariable Long id, Authentication authentication)
    {
        if (authentication == null || !StringUtils.hasText(authentication.getName())) {
            return Result.error(401, "请先登录");
        }

        CommentVO comment = commentService.getCommentById(id);
        if (comment == null) {
            return Result.error("评论不存在");
        }

        blog.entity.UserAccount currentUser = userAccountService.findByUsername(authentication.getName());
        if (currentUser == null) {
            return Result.error(401, "未找到当前登录用户");
        }

        boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
        boolean isAuthor = comment.getUserId() != null && comment.getUserId().equals(currentUser.getId());
        if (!isAdmin && !isAuthor) {
            return Result.error(403, "你没有权限删除这条评论");
        }

        commentService.deleteCommentById(id);
        return Result.success();
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getClientIpAddress(jakarta.servlet.http.HttpServletRequest request) {
        return IpUtils.getClientIpAddress(request);
    }

    private void markOwnership(List<CommentVO> comments, Long currentUserId)
    {
        for (CommentVO comment : comments) {
            comment.setOwnedByCurrentUser(comment.getUserId() != null && comment.getUserId().equals(currentUserId));
            if (comment.getReplyComments() != null && !comment.getReplyComments().isEmpty()) {
                markOwnership(comment.getReplyComments(), currentUserId);
            }
        }
    }
}

