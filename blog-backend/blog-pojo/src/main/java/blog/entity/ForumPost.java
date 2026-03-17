package blog.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class ForumPost
{
    private Long id;

    private Long authorId;

    private String title;

    private String summary;

    private String content;

    private String nickname;

    private String email;

    private String avatar;

    private Integer viewCount;

    private Boolean isPinned;

    private Boolean isFeatured;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private LocalDateTime lastActivityTime;
}
