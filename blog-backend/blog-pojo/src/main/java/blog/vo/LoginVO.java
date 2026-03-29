package blog.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO implements Serializable {

    /**
     * token
     */
    private String token;

    /**
     * 过期时间（秒）
     */
    private Integer expiresIn;

    /**
     * token类型
     */
    private String tokenType;

    /**
     * 用户信息
     */
    private UserInfoVO userInfo;
}