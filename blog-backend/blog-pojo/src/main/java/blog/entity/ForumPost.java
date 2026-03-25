package blog.entity;


import lombok.Data;
import java.time.LocalDateTime;

/**
 * 论坛帖子实体类
 */
@Data
public class ForumPost {

    private Long id;

    private Long authorId;

    private String title;

    private String summary;

    private String content;

    private String nickname;

    private String email;

    private String avatar;

    private Integer viewCount;

    private Integer commentCount;

    private Integer likeCount;

    private Boolean isPinned;

    private Boolean isFeatured;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private LocalDateTime lastActivityTime;
}
