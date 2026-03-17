package blog.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAccount
{
    private Long id;

    private String username;

    private Long githubId;

    private String githubLogin;

    private String nickname;

    private String avatar;

    private String email;

    private String passwordHash;

    private String bio;

    private String role;

    private Boolean status;

    private LocalDateTime lastLoginTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
