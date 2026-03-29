package blog.dto.Login;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GithubOAuthUserDTO
{
    private Long githubId;

    private String githubLogin;

    private String nickname;

    private String avatar;

    private String email;

    private String bio;
}
