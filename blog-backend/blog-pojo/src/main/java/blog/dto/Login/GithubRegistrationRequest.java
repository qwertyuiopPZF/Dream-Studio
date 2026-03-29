package blog.dto.Login;

import lombok.Data;

@Data
public class GithubRegistrationRequest
{
    private String registrationToken;

    private String username;

    private String phone;

    private String password;
}
