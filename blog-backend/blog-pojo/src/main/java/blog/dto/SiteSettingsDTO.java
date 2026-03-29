package blog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SiteSettingsDTO
{
    private String name;
    private String description;
    private String keywords;
    private String author;
    private String icp;
}
