package blog.service;

import blog.dto.ForumPostAdminUpdateDTO;
import blog.dto.ForumReportCreateDTO;
import blog.dto.ForumReportReviewDTO;
import blog.vo.ForumReportVO;
import blog.vo.UserCenterOverviewVO;

public interface UserCenterService
{
    UserCenterOverviewVO getOverview(String username);

    ForumReportVO createReport(String username, ForumReportCreateDTO request);

    void markNotificationAsRead(String username, Long notificationId);

    ForumReportVO reviewReport(String username, Long reportId, ForumReportReviewDTO request);

    void updateAdminPostMeta(String username, Long postId, ForumPostAdminUpdateDTO request);

    void deleteAdminPost(String username, Long postId);
}
