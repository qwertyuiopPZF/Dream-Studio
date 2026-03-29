package blog.vo;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class UserVO implements Serializable {

    /**
     * 用户ID
     */
    private String id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 性别（male-男，female-女，other-其他）
     */
    private String gender;

    /**
     * 生日
     */
    private String birthday;

    /**
     * 个性签名
     */
    private String signature;

    /**
     * 角色（user-普通用户，admin-管理员）
     */
    private String role;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 性别文本（男、女、其他）
     */
    private String genderText;

    /**
     * 角色文本（普通用户、管理员）
     */
    private String roleText;
}