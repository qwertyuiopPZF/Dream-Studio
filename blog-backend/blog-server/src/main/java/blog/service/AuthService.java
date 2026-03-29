package blog.service;

import blog.dto.Login.GithubLoginDTO;
import blog.dto.Login.GithubRegistrationRequest;
import blog.dto.Login.LoginRequest;
import blog.dto.Login.LoginResponse;
import blog.dto.Login.RefreshTokenRequest;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface AuthService
{

    LoginResponse login(LoginRequest request);

    Map<String, String> buildGithubAuthorizePayload();

    LoginResponse loginByGithub(GithubLoginDTO request);

    LoginResponse registerByGithub(GithubRegistrationRequest request);

    LoginResponse refreshToken(RefreshTokenRequest request);
}
