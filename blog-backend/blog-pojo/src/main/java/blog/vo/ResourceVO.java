package blog.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourceVO
{
    private Long id;

    private Long uploaderId;

    private String uploaderName;

    private String uploaderAvatar;

    private String originalName;

    private String fileUrl;

    private Long fileSize;

    private String extension;

    private String categoryKey;

    private String mimeType;

    private String status;

    private String reviewNote;

    private Long reviewerId;

    private String reviewerName;

    private Integer downloadCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private LocalDateTime reviewTime;
}
