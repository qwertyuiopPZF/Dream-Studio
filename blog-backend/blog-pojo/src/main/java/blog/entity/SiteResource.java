package blog.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SiteResource
{
    private Long id;

    private Long uploaderId;

    private String originalName;

    private String fileUrl;

    private Long fileSize;

    private String extension;

    private String categoryKey;

    private String mimeType;

    private String status;

    private String reviewNote;

    private Long reviewerId;

    private Integer downloadCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private LocalDateTime reviewTime;
}
