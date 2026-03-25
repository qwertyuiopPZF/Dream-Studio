package blog.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCenterOverviewVO
{
    private UserProfileVO profile;

    private Long postCount;

    private Long commentCount;

    private Long unreadNotificationCount;

    private Long pendingReportCount;

    private List<ForumPostVO> myPosts = new ArrayList<>();

    private List<CommentVO> myComments = new ArrayList<>();

    private List<ForumReportVO> reports = new ArrayList<>();

    private List<UserNotificationVO> notifications = new ArrayList<>();

    private List<ForumPostVO> moderationPosts = new ArrayList<>();
}
