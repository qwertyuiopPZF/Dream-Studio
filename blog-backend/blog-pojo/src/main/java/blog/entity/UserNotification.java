package blog.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserNotification
{
    private Long id;

    private Long userId;

    private String type;

    private String title;

    private String content;

    private String targetType;

    private Long targetId;

    private Long relatedReportId;

    private Boolean isRead;

    private LocalDateTime createTime;

    private LocalDateTime readTime;
}
