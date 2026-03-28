package blog.entity;

<<<<<<< HEAD

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 论坛帖子实体类
 */
@Data
public class ForumPost {

=======
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
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
    private Long id;

    private Long authorId;

    private String title;

    private String summary;

    private String content;

    private String nickname;

    private String email;

    private String avatar;

    private Integer viewCount;

<<<<<<< HEAD
    private Integer commentCount;

    private Integer likeCount;

=======
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
    private Boolean isPinned;

    private Boolean isFeatured;

<<<<<<< HEAD
    private Integer status;

=======
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private LocalDateTime lastActivityTime;
}
