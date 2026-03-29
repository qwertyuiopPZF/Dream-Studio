package blog.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserVO
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

    private Boolean status;

    private LocalDateTime createTime;

    private LocalDateTime lastLoginTime;
}
