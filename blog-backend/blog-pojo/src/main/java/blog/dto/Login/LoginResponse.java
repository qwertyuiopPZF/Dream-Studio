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
<<<<<<< HEAD

    public LoginResponse(String accessToken, String refreshToken, String username, Boolean needsPasswordSetup)
=======
    private String role;

    public LoginResponse(String accessToken,
                         String refreshToken,
                         String username,
                         Boolean needsPasswordSetup,
                         String role)
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
    {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.username = username;
        this.needsPasswordSetup = needsPasswordSetup;
        this.needsRegistration = false;
<<<<<<< HEAD
=======
        this.role = role;
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
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
<<<<<<< HEAD
=======
        response.setRole("USER");
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
        return response;
    }
}
