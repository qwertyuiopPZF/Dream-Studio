package blog.dto.Login;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginResponse
{
    private String accessToken;
    private String refreshToken;
    private String username;
    private Boolean needsPasswordSetup;
    private Boolean needsRegistration;
    private String registrationToken;
    private String githubLogin;
    private String nickname;
    private String avatar;
    private String email;
    private String phone;
    private String role;

    public LoginResponse(String accessToken,
                         String refreshToken,
                         String username,
                         Boolean needsPasswordSetup,
                         String role)
    {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.username = username;
        this.needsPasswordSetup = needsPasswordSetup;
        this.needsRegistration = false;
        this.role = role;
    }

    public static LoginResponse githubRegistrationPending(String registrationToken,
                                                          String suggestedUsername,
                                                          String githubLogin,
                                                          String nickname,
                                                          String avatar,
                                                          String email,
                                                          String phone)
    {
        LoginResponse response = new LoginResponse();
        response.setUsername(suggestedUsername);
        response.setNeedsPasswordSetup(false);
        response.setNeedsRegistration(true);
        response.setRegistrationToken(registrationToken);
        response.setGithubLogin(githubLogin);
        response.setNickname(nickname);
        response.setAvatar(avatar);
        response.setEmail(email);
        response.setPhone(phone);
        response.setRole("USER");
        return response;
    }
}
