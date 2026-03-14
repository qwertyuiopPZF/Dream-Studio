package blog.service;

import blog.dto.Login.LoginRequest;
import blog.dto.Login.LoginResponse;
import blog.result.Result;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface AuthService
{

    LoginResponse login(LoginRequest request);

    Map<String, Object> handleGithubLogin(String code);

    Map<String, Object> getGithubUserInfo(String accessToken);

    Result<Map<String, Object>> completeInfo(String token, String phone, String password);
}
