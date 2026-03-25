package blog.service.impl;

import blog.dto.UserDTO;
import blog.entity.User;  // 需要创建这个实体类
import blog.mapper.UserMapper;  // 需要创建这个Mapper
import blog.result.Result;
import blog.service.UserService;
import blog.utils.JwtUtil;
import blog.vo.UserInfoVO;
import blog.vo.LoginVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Result<LoginVO> login(UserDTO userDTO) {
        log.info("用户登录：{}", userDTO.getUsername());

        // 1. 参数校验（虽然DTO已经有校验，这里再加一层业务校验）
        if (userDTO.getUsername() == null || userDTO.getUsername().trim().isEmpty()) {
            return Result.error(400, "用户名不能为空");
        }
        if (userDTO.getPassword() == null || userDTO.getPassword().trim().isEmpty()) {
            return Result.error(400, "密码不能为空");
        }

        // 2. 根据用户名查询用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, userDTO.getUsername());
        User user = userMapper.selectOne(queryWrapper);

        // 3. 判断用户是否存在
        if (user == null) {
            log.warn("登录失败：用户不存在 - {}", userDTO.getUsername());
            return Result.error(404, "用户名或密码错误");
        }

        // 4. 密码加密比对（假设数据库中的密码是MD5加密的）
        String encryptedPassword = DigestUtils.md5DigestAsHex(userDTO.getPassword().getBytes());
        if (!encryptedPassword.equals(user.getPassword())) {
            log.warn("登录失败：密码错误 - {}", userDTO.getUsername());
            return Result.error(404, "用户名或密码错误");
        }

        // 5. 检查用户状态（可选）
        if (user.getStatus() != null && user.getStatus() == 0) {
            log.warn("登录失败：账号被禁用 - {}", userDTO.getUsername());
            return Result.error(403, "账号已被禁用，请联系管理员");
        }

        // 6. 生成JWT token
        String token = jwtUtil.createToken(user.getId(), user.getUsername());

        // 7. 构建返回的用户信息（精简版，只返回必要字段）
        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setId(String.valueOf(user.getId()));
        userInfoVO.setUsername(user.getUsername());
        userInfoVO.setNickname(user.getNickname());
        userInfoVO.setAvatar(user.getAvatar());
        userInfoVO.setRole(user.getRole());
        userInfoVO.setRoleText(getRoleText(user.getRole())); // 设置角色文本

        // 8. 构建登录返回结果
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setExpiresIn(7200); // token过期时间（秒）
        loginVO.setTokenType("Bearer");
        loginVO.setUserInfo(userInfoVO);

        // 9. 更新最后登录时间（可选）
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("登录成功：{}", user.getUsername());
        return Result.success(loginVO);
    }

    /**
     * 根据角色获取角色文本
     */
    private String getRoleText(String role) {
        if (role == null) return "";
        switch (role) {
            case "admin": return "管理员";
            case "user": return "普通用户";
            default: return "未知角色";
        }
    }
}