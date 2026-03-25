package blog.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PostVO {
    private Long id;
    private String title;
    private String summary;
    private String nickname;
    private String avatar;
    private Integer viewCount;
    private Integer commentCount;
    private Boolean isPinned;
    private Boolean isFeatured;
    private LocalDateTime createTime;
    private LocalDateTime lastActivityTime;
}
