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
<<<<<<< HEAD
=======
import blog.service.AccessControlService;
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
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

<<<<<<< HEAD
=======
    @Autowired
    private AccessControlService accessControlService;

>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
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
<<<<<<< HEAD
                ? forumPostMapper.selectPage("latest", 0, CENTER_LIST_LIMIT, null, null, false)
=======
                ? forumPostMapper.selectPage("latest", 0, CENTER_LIST_LIMIT, null, null, null, false)
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
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

        ForumPostVO targetPost = forumPostMapper.selectById(request.getTargetId());
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
        report.setCreateTime(now);
        report.setUpdateTime(now);
        forumReportMapper.insert(report);

        String reporterName = StringUtils.hasText(currentUser.getNickname()) ? currentUser.getNickname() : currentUser.getUsername();
<<<<<<< HEAD
        List<UserAccount> admins = userAccountMapper.selectByRole("ADMIN");
=======
        List<UserAccount> admins = userAccountMapper.selectAdmins();
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
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
<<<<<<< HEAD
        UserAccount currentUser = requireAdmin(username);
=======
        UserAccount currentUser = accessControlService.requireAdmin(username);
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
        if (request == null || !StringUtils.hasText(request.getStatus())) {
            throw new IllegalArgumentException("处理状态不能为空");
        }

        String status = request.getStatus().trim().toUpperCase();
        if (!"RESOLVED".equals(status) && !"REJECTED".equals(status)) {
            throw new IllegalArgumentException("仅支持标记为 RESOLVED 或 REJECTED");
        }

        ForumReportVO existingReport = forumReportMapper.selectById(reportId);
        if (existingReport == null) {
            throw new IllegalArgumentException("举报记录不存在");
        }

        ForumReport update = new ForumReport();
        update.setId(reportId);
        update.setStatus(status);
        update.setReviewerId(currentUser.getId());
        update.setReviewerNote(trimToNull(request.getReviewerNote()));
        update.setUpdateTime(LocalDateTime.now());
        forumReportMapper.updateReview(update);

        createNotification(
                existingReport.getReporterId(),
                "REPORT_REVIEWED",
                "你的举报已有处理结果",
                "你对《" + existingReport.getTargetTitle() + "》的举报已被标记为 " + statusLabel(status),
                existingReport.getTargetType(),
                existingReport.getTargetId(),
                existingReport.getId()
        );

        return forumReportMapper.selectById(reportId);
    }

    @Override
    public void updateAdminPostMeta(String username, Long postId, ForumPostAdminUpdateDTO request)
    {
<<<<<<< HEAD
        requireAdmin(username);
=======
        accessControlService.requireAdmin(username);
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
        forumPostService.updateAdminPostMeta(postId, request);
    }

    @Override
    public void deleteAdminPost(String username, Long postId)
    {
<<<<<<< HEAD
        requireAdmin(username);
=======
        accessControlService.requireAdmin(username);
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
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

    private UserAccount requireUser(String username)
    {
<<<<<<< HEAD
        UserAccount user = userAccountService.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("当前用户不存在");
        }
        return user;
=======
        return accessControlService.requireUser(username);
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
    }

    private UserAccount requireAdmin(String username)
    {
<<<<<<< HEAD
        UserAccount user = requireUser(username);
        if (!isAdmin(user)) {
            throw new IllegalArgumentException("当前用户没有管理员权限");
        }
        return user;
=======
        return accessControlService.requireAdmin(username);
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
    }

    private boolean isAdmin(UserAccount user)
    {
<<<<<<< HEAD
        return user != null && "ADMIN".equalsIgnoreCase(user.getRole());
=======
        return accessControlService.isAdmin(user);
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
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
}
