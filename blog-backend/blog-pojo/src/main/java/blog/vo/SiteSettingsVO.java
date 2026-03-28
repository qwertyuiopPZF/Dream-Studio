package blog.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SiteSettingsVO
{
    private String name;
    private String description;
    private String keywords;
    private String author;
    private String icp;
}
