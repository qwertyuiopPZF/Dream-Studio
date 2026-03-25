package blog.controller.user;

import blog.dto.Login.GithubLoginDTO;
import blog.dto.Login.GithubRegistrationRequest;
import blog.dto.Login.LoginRequest;
import blog.dto.Login.LoginResponse;
import blog.dto.Login.RefreshTokenRequest;
import blog.result.Result;
import blog.service.AuthService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController("userAuthController")
@RequestMapping("/api/auth")
@Slf4j
public class AuthController
{
    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    @ApiOperation("用户名密码登录")
    public Result<LoginResponse> login(@RequestBody LoginRequest request)
    {
        return Result.success(authService.login(request));
    }

    @GetMapping("/github/url")
    @ApiOperation("获取 GitHub 登录地址")
    public Result<Map<String, String>> getGithubLoginUrl()
    {
        return Result.success(authService.buildGithubAuthorizePayload());
    }

    @PostMapping("/github/login")
    @ApiOperation("通过 GitHub code 登录")
    public Result<LoginResponse> githubLogin(@RequestBody GithubLoginDTO request)
    {
        return Result.success(authService.loginByGithub(request));
    }

    @PostMapping("/github/register")
    @ApiOperation("通过 GitHub 完成注册")
    public Result<LoginResponse> registerByGithub(@RequestBody GithubRegistrationRequest request)
    {
        return Result.success(authService.registerByGithub(request));
    }

    @PostMapping("/refresh")
    @ApiOperation("刷新访问令牌")
    public Result<LoginResponse> refresh(@RequestBody RefreshTokenRequest request)
    {
        return Result.success(authService.refreshToken(request));
    }

    @GetMapping("/github/callback")
    @ApiOperation("GitHub OAuth 回调")
    public Result<LoginResponse> githubCallback(@RequestParam String code,
                                                @RequestParam(required = false) String state)
    {
        GithubLoginDTO request = new GithubLoginDTO();
        request.setCode(code);
        request.setState(state);
        return Result.success(authService.loginByGithub(request));
    }
}
