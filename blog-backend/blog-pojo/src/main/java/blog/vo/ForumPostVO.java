package blog.vo;

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
public class ForumPostVO
{
    private Long id;

    private Long authorId;

    private String title;

    private String summary;

    private String content;

    private Long categoryId;

    private String categoryName;

    private String tagIds;

    private String tags;

    private String nickname;

    private String avatar;

    private String authorNickname;

    private String authorAvatar;

    private Integer viewCount;

    private Integer commentCount;

    private Integer status;

    private Boolean isPinned;

    private Boolean isFeatured;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private LocalDateTime lastActivityTime;
}
