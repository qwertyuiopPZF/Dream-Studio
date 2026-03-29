package blog.service.impl;

import blog.dto.ForumPostAdminUpdateDTO;
import blog.dto.ForumReportCreateDTO;
import blog.dto.ForumReportReviewDTO;
import blog.entity.ForumReport;
import blog.entity.UserAccount;
import blog.entity.UserNotification;
import blog.mapper.CommentMapper;
import blog.mapper.ForumPostMapper;
import blog.mapper.ForumReportMapper;
import blog.mapper.UserAccountMapper;
import blog.mapper.UserNotificationMapper;
import blog.service.AccessControlService;
import blog.service.ForumPostService;
import blog.service.UserAccountService;
import blog.service.UserCenterService;
import blog.vo.CommentVO;
import blog.vo.ForumPostVO;
import blog.vo.ForumReportVO;
import blog.vo.UserCenterOverviewVO;
import blog.vo.UserNotificationVO;
import blog.vo.UserProfileVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class UserCenterServiceImpl implements UserCenterService
{
    private static final int CENTER_LIST_LIMIT = 20;

    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private UserAccountMapper userAccountMapper;

    @Autowired
    private ForumPostMapper forumPostMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private ForumReportMapper forumReportMapper;

    @Autowired
    private UserNotificationMapper userNotificationMapper;

    @Autowired
    private ForumPostService forumPostService;

    @Autowired
    private AccessControlService accessControlService;

    @Override
    public UserCenterOverviewVO getOverview(String username)
    {
        UserAccount currentUser = requireUser(username);
        UserProfileVO profile = userAccountService.getProfileByUsername(username);
        boolean admin = isAdmin(currentUser);

        List<ForumPostVO> myPosts = forumPostMapper.selectByAuthorId(currentUser.getId(), CENTER_LIST_LIMIT);
        List<CommentVO> myComments = commentMapper.selectByUserId(currentUser.getId(), CENTER_LIST_LIMIT);
        List<ForumReportVO> reports = admin
                ? forumReportMapper.selectAll(CENTER_LIST_LIMIT)
                : forumReportMapper.selectByReporterId(currentUser.getId(), CENTER_LIST_LIMIT);
        List<UserNotificationVO> notifications = userNotificationMapper.selectByUserId(currentUser.getId(), CENTER_LIST_LIMIT);
        List<ForumPostVO> moderationPosts = admin
                ? forumPostMapper.selectPage("latest", 0, CENTER_LIST_LIMIT, null, null, null, false, null)
                : Collections.emptyList();

        UserCenterOverviewVO overview = new UserCenterOverviewVO();
        overview.setProfile(profile);
        overview.setPostCount(safeLong(forumPostMapper.countByAuthorId(currentUser.getId())));
        overview.setCommentCount(safeLong(commentMapper.countByUserId(currentUser.getId())));
        overview.setUnreadNotificationCount(safeLong(userNotificationMapper.countUnreadByUserId(currentUser.getId())));
        overview.setPendingReportCount(admin
                ? safeLong(forumReportMapper.countPending())
                : safeLong(forumReportMapper.countPendingByReporterId(currentUser.getId())));
        overview.setMyPosts(myPosts);
        overview.setMyComments(myComments);
        overview.setReports(reports);
        overview.setNotifications(notifications);
        overview.setModerationPosts(moderationPosts);
        return overview;
    }

    @Override
    public ForumReportVO createReport(String username, ForumReportCreateDTO request)
    {
        UserAccount currentUser = requireUser(username);
        validateReportRequest(request);

        ForumPostVO targetPost = forumPostMapper.selectById(request.getTargetId(), 1);
        if (targetPost == null) {
            throw new IllegalArgumentException("被举报的帖子不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        ForumReport report = new ForumReport();
        report.setReporterId(currentUser.getId());
        report.setTargetType("forum-post");
        report.setTargetId(targetPost.getId());
        report.setTargetTitle(targetPost.getTitle());
        report.setReason(request.getReason().trim().toUpperCase());
        report.setDetail(trimToNull(request.getDetail()));
        report.setStatus("PENDING");
        report.setTargetAction("NONE");
        report.setCreateTime(now);
        report.setUpdateTime(now);
        forumReportMapper.insert(report);

        String reporterName = StringUtils.hasText(currentUser.getNickname()) ? currentUser.getNickname() : currentUser.getUsername();
        List<UserAccount> admins = userAccountMapper.selectAdmins();
        for (UserAccount admin : admins) {
            createNotification(
                    admin.getId(),
                    "REPORT_CREATED",
                    "收到新的帖子举报",
                    reporterName + " 举报了帖子《" + targetPost.getTitle() + "》",
                    "forum-post",
                    targetPost.getId(),
                    report.getId()
            );
        }

        return forumReportMapper.selectById(report.getId());
    }

    @Override
    public void markNotificationAsRead(String username, Long notificationId)
    {
        UserAccount currentUser = requireUser(username);
        int affected = userNotificationMapper.markAsRead(currentUser.getId(), notificationId, LocalDateTime.now());
        if (affected == 0) {
            throw new IllegalArgumentException("通知不存在或无权限操作");
        }
    }

    @Override
    public ForumReportVO reviewReport(String username, Long reportId, ForumReportReviewDTO request)
    {
        UserAccount currentUser = accessControlService.requireAdmin(username);
        if (request == null || !StringUtils.hasText(request.getStatus())) {
            throw new IllegalArgumentException("处理状态不能为空");
        }

        String status = request.getStatus().trim().toUpperCase();
        if (!"RESOLVED".equals(status) && !"REJECTED".equals(status)) {
            throw new IllegalArgumentException("仅支持标记为 RESOLVED 或 REJECTED");
        }

        String targetAction = normalizeTargetAction(status, request.getTargetAction());

        ForumReportVO existingReport = forumReportMapper.selectById(reportId);
        if (existingReport == null) {
            throw new IllegalArgumentException("举报记录不存在");
        }

        applyReviewAction(existingReport, status, targetAction);

        ForumReport update = new ForumReport();
        update.setId(reportId);
        update.setStatus(status);
        update.setTargetAction(targetAction);
        update.setReviewerId(currentUser.getId());
        update.setReviewerNote(trimToNull(request.getReviewerNote()));
        update.setUpdateTime(LocalDateTime.now());
        forumReportMapper.updateReview(update);

        createNotification(
                existingReport.getReporterId(),
                "REPORT_REVIEWED",
                "你的举报已有处理结果",
                buildReviewNotificationContent(existingReport.getTargetTitle(), status, targetAction),
                existingReport.getTargetType(),
                existingReport.getTargetId(),
                existingReport.getId()
        );

        return forumReportMapper.selectById(reportId);
    }

    @Override
    public void updateAdminPostMeta(String username, Long postId, ForumPostAdminUpdateDTO request)
    {
        accessControlService.requireAdmin(username);
        forumPostService.updateAdminPostMeta(postId, request);
    }

    @Override
    public void deleteAdminPost(String username, Long postId)
    {
        accessControlService.requireAdmin(username);
        forumPostService.deletePost(postId);
    }

    private void validateReportRequest(ForumReportCreateDTO request)
    {
        if (request == null) {
            throw new IllegalArgumentException("举报内容不能为空");
        }
        if (!StringUtils.hasText(request.getTargetType()) || !"forum-post".equalsIgnoreCase(request.getTargetType().trim())) {
            throw new IllegalArgumentException("当前仅支持举报论坛帖子");
        }
        if (request.getTargetId() == null) {
            throw new IllegalArgumentException("举报目标不能为空");
        }
        if (!StringUtils.hasText(request.getReason())) {
            throw new IllegalArgumentException("举报原因不能为空");
        }
        if (request.getDetail() != null && request.getDetail().trim().length() > 500) {
            throw new IllegalArgumentException("补充说明不能超过 500 个字符");
        }
    }

    private void createNotification(Long userId,
                                    String type,
                                    String title,
                                    String content,
                                    String targetType,
                                    Long targetId,
                                    Long relatedReportId)
    {
        UserNotification notification = new UserNotification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setTargetType(targetType);
        notification.setTargetId(targetId);
        notification.setRelatedReportId(relatedReportId);
        notification.setIsRead(false);
        notification.setCreateTime(LocalDateTime.now());
        userNotificationMapper.insert(notification);
    }

    private void applyReviewAction(ForumReportVO existingReport, String status, String targetAction)
    {
        if (!"RESOLVED".equals(status) || "NONE".equals(targetAction)) {
            return;
        }
        if (!"forum-post".equalsIgnoreCase(existingReport.getTargetType())) {
            throw new IllegalArgumentException("当前举报目标暂不支持该处理动作");
        }

        if ("UNPUBLISH".equals(targetAction)) {
            forumPostService.updatePostStatus(existingReport.getTargetId(), 0);
            return;
        }
        if ("DELETE".equals(targetAction)) {
            forumPostService.deletePost(existingReport.getTargetId());
            return;
        }

        throw new IllegalArgumentException("不支持的处理动作");
    }

    private String normalizeTargetAction(String status, String targetAction)
    {
        if (!"RESOLVED".equals(status)) {
            return "NONE";
        }

        if (!StringUtils.hasText(targetAction)) {
            throw new IllegalArgumentException("请先选择处理动作");
        }

        String normalizedAction = targetAction.trim().toUpperCase();
        if (!"UNPUBLISH".equals(normalizedAction) && !"DELETE".equals(normalizedAction)) {
            throw new IllegalArgumentException("仅支持设为不公开或删除内容");
        }
        return normalizedAction;
    }

    private String buildReviewNotificationContent(String targetTitle, String status, String targetAction)
    {
        if ("REJECTED".equals(status)) {
            return "你对《" + targetTitle + "》的举报已被驳回";
        }

        return "你对《" + targetTitle + "》的举报已处理，管理员已" + targetActionLabel(targetAction);
    }

    private UserAccount requireUser(String username)
    {
        return accessControlService.requireUser(username);
    }

    private UserAccount requireAdmin(String username)
    {
        return accessControlService.requireAdmin(username);
    }

    private boolean isAdmin(UserAccount user)
    {
        return accessControlService.isAdmin(user);
    }

    private String trimToNull(String value)
    {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private long safeLong(Long value)
    {
        return value == null ? 0L : value;
    }

    private String statusLabel(String status)
    {
        if ("RESOLVED".equals(status)) {
            return "已处理";
        }
        if ("REJECTED".equals(status)) {
            return "已驳回";
        }
        return status;
    }

    private String targetActionLabel(String targetAction)
    {
        if ("UNPUBLISH".equals(targetAction)) {
            return "设为不公开";
        }
        if ("DELETE".equals(targetAction)) {
            return "删除该帖子";
        }
        return statusLabel(targetAction);
    }
}
