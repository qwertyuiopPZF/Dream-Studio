package blog.dto.Login;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GithubRegistrationSession
{
    private Long existingUserId;

    private Long githubId;

    private String githubLogin;

    private String nickname;

    private String avatar;

    private String email;

    private String phone;

    private String bio;

    private String suggestedUsername;
}
