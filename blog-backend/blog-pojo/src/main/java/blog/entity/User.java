package blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;  // 数据库中存储的是加密后的密码

    private String nickname;

    private String avatar;

    private String email;

    private String phone;

    private String gender;

    private LocalDateTime birthday;

    private String signature;


    private String role;  // user/admin

    private Integer status;  // 0-禁用 1-正常

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private LocalDateTime lastLoginTime;
}
