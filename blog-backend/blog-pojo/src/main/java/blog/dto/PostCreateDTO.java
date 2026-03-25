package blog.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PostCreateDTO {
    @NotBlank(message = "标题不能为空")
    @Size(min = 1, max = 200, message = "标题长度为1-200字符")
    private String title;

    @Size(max = 500, message = "摘要最多500字符")
    private String summary;

    @NotBlank(message = "内容不能为空")
    private String content;

    private Boolean autoSummary = true;
}
