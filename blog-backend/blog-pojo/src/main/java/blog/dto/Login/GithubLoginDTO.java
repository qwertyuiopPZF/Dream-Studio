package blog.dto.Login;

import lombok.Data;

@Data
public class GithubLoginDTO
{
    private String code;

    private String state;
}
