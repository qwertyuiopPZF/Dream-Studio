package blog.dto;

import lombok.Data;

@Data
public class ForumReportCreateDTO
{
    private String targetType;

    private Long targetId;

    private String reason;

    private String detail;
}
