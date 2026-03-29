package blog.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForumReport
{
    private Long id;

    private Long reporterId;

    private String targetType;

    private Long targetId;

    private String targetTitle;

    private String reason;

    private String detail;

    private String status;

    private String targetAction;

    private Long reviewerId;

    private String reviewerNote;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
