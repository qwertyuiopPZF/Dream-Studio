package blog.service.impl;

import blog.dto.Login.LoginRequest;
import blog.dto.Login.LoginResponse;
import blog.entity.Auth;
import blog.mapper.AuthMapper;
import blog.result.Result;
import blog.service.AuthService;
import blog.utils.JwtUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService
{

    @Value("${github.login.client-id}")
    private String clientId;
    @Value("${github.login.client-secret}")
    private String clientSecret;
    @Value("${github.login.redirect-uri}")
    private String redirectUri;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private AuthMapper authMapper;

    public LoginResponse login(LoginRequest request) {
        // 1. 调用 Spring Security 进行认证
        // 如果认证失败（密码错误），这里会直接抛出 AuthenticationException
        Authentication authentication = authenticationManager.authenticate(

                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // 2. 认证通过，获取用户信息
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        // 3. 生成双 Token
        String accessToken = jwtUtil.createAccessToken(username, roles);
        String refreshToken = jwtUtil.createRefreshToken(username);

        // 4. 返回结果
        return new LoginResponse(accessToken, refreshToken);
    }

    public Map<String,Object> handleGithubLogin(String code){
        String accessToken = getGithubAccessToken(code);
        Map<String, Object> githubUser = getGithubUserInfo(accessToken);
        String githubId = githubUser.get("id").toString();
        String githubName = (String) githubUser.get("login");
        String avatar = (String) githubUser.get("avatar_url");
        // 3. 查询/创建用户
        Auth auth = authMapper.selectByGithubId(githubId);
        if(auth == null){
            auth = new Auth();
            auth.setGithubId(githubId);
            auth.setGithubName(githubName);
            auth.setAvatar(avatar);
            auth.setUsername(githubName);
            auth.setRole("user");
            auth.setStatus(1);

            auth.setPassword("");
            auth.setEmail("");
            auth.setPhone("");

            authMapper.insertAuth(auth);
        }


        // 4. 生成双Token
        String accessTokenJwt = jwtUtil.createGithubAccessToken(githubId, auth.getInfoComplete());
        String refreshTokenJwt = jwtUtil.createGithubRefreshToken(githubId);

        // 5. 返回业务数据（控制器封装为统一结果）
        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", accessTokenJwt);
        result.put("refreshToken", refreshTokenJwt);
        result.put("infoComplete", auth.getInfoComplete());
        return result;
    }

    private String getGithubAccessToken(String code){
        Map<String,Object> params = new HashMap<>();
        params.put("client_id", clientId);
        params.put("client_secret", clientSecret);
        params.put("code", code);
        params.put("redirect_uri", redirectUri);
        String tokenResp = HttpUtil.post("https://github.com/login/oauth/access_token", params);
        String[] paramsArr = tokenResp.split("&");
        for (String param : paramsArr) {
            if (param.startsWith("access_token=")) {
                return param.split("=")[1];
            }
        }
        return null;
    }

    public Map<String, Object> getGithubUserInfo(String accessToken) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "token " + accessToken);
        String userInfoResp = HttpUtil.createGet("https://api.github.com/user")
                .addHeaders(headers)
                .execute()
                .body();
        return JSONUtil.toBean(userInfoResp, HashMap.class);
    }

    public  Result<Map<String, Object>> completeInfo(String token, String phone, String password) {
        // 1. 解析 Token 获取 githubId
        String githubId = jwtUtil.getGithubIdFromToken(token.replace("Bearer ", ""));

        // 2. 查询用户
        Auth auth = authMapper.selectByGithubId(githubId);
        if (auth == null) {
            throw new RuntimeException("用户不存在");
        }

        // 3. 更新手机号和密码（密码建议加密，比如用 MD5 或 BCrypt）
        auth.setPhone(phone);
        auth.setPassword(DigestUtils.md5DigestAsHex(password.getBytes()));
//        if (!password.equals(employee.getPassword())) {
//            //密码错误
//            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
//        }
//
//        if (employee.getStatus() == StatusConstant.DISABLE) {
//            //账号被锁定
//            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
//        }
        // 4. 更新数据库
        authMapper.updateAuth(auth);

        // 5. 生成新的 Access Token（可选）
        String newAccessToken = jwtUtil.createGithubAccessToken(githubId, 1);

        // 6. 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", newAccessToken);
        result.put("msg", "信息完善成功");
        return Result.success(result);
    }
}
