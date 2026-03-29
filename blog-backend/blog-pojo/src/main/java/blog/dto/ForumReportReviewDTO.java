package blog.dto;

import lombok.Data;

@Data
public class ForumReportReviewDTO
{
    private String status;

    private String targetAction;

    private String reviewerNote;
}
