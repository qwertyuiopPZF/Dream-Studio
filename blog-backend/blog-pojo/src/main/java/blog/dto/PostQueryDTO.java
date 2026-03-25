package blog.dto;

import lombok.Data;

@Data
public class PostQueryDTO {
    private Integer page = 1;
    private Integer size = 10;
    private String sortBy = "time";
    private String order = "desc";
    private String keyword;
}
