package blog.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileVO
{
    private Long id;

    private String username;

    private String nickname;

    private String avatar;

    private String email;

    private String phone;

    private String bio;

    private String role;

    private String githubLogin;

    private Boolean passwordInitialized;
}
