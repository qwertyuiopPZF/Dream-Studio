package blog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Data
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class ForumPostDTO implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long authorId;

    private String title;

    private String summary;

    private String content;

    private Long categoryId;

    private String tags;

    private String nickname;

    private String email;

    private String avatar;
}
