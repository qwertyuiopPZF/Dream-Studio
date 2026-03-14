package blog.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Auth implements Serializable {
    private Long id;
    private String username;//用户名
    private String password;//加密后密码
    private String githubId;//githubId
    private String githubName;//github昵称
    private String phone;//手机号
    private String avatar;//头像
    private String role;
    private String email;
    private Integer status;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Integer getInfoComplete() {
        boolean isComplete = (password != null && !password.isEmpty())
                && (phone != null && !phone.isEmpty());
        return isComplete ? 1 : 0;
    }
}
