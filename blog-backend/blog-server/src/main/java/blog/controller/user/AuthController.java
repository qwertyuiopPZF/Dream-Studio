package blog.controller.user;

import blog.dto.Login.LoginRequest;
import blog.dto.Login.LoginResponse;
import blog.dto.LoginDTO;
import blog.result.Result;
import blog.service.AuthService;
import blog.vo.AuthVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * 处理登录、登出等认证相关操作
 *
 * @author Eleven
 * @version 1.0
 */
@RestController
@RequestMapping("/user/auth")
@CrossOrigin(origins = "http://8.162.7.124:3000")
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
    @ApiOperation(value = "GitHub登录回调", notes = "通过code获取GitHub用户信息并登录")
    public Result<Map<String,Object>> githubLogin(@RequestBody String code) {
        Map<String,Object> loginResult = authService. handleGithubLogin(code);
        return Result.success(loginResult);
    }


    @PostMapping("/complete-info")
    @ApiOperation(value = "信息补全", notes = "补全必要的phone和password")
    public Result<Map<String, Object>> completeInfo(
            @RequestHeader("Authorization") String token,
            @RequestParam String phone,
            @RequestParam String password) {
        Result<Map<String, Object>> result = authService.completeInfo(token, phone, password);
        return result;
    }
}
