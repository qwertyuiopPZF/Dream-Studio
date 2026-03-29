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
public class ForumPostAdminUpdateDTO implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Boolean isPinned;

    private Boolean isFeatured;

    private Integer status;

    private Long categoryId;

    private String tags;
}
