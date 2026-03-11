package blog.controller.user;

import blog.dto.Login.LoginRequest;
import blog.dto.Login.LoginResponse;
import blog.dto.LoginDTO;
import blog.result.Result;
import blog.service.AuthService;
import blog.vo.AuthVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 处理登录、登出等认证相关操作
 *
 * @author Eleven
 * @version 1.0
 */
@RestController
@RequestMapping("/user/auth")
@CrossOrigin(origins = "")
@Slf4j
public class AuthController {


    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /**
      github登录
     */
    @PostMapping("/github/login")
    public Result<AuthVO> githubLogin(@RequestBody String code) {

    }
}
