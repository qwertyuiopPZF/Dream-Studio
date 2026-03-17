package blog.service.impl;

import blog.config.GithubOauthProperties;
import blog.dto.Login.GithubLoginDTO;
import blog.dto.Login.GithubOAuthUserDTO;
import blog.dto.Login.LoginRequest;
import blog.dto.Login.LoginResponse;
import blog.service.AuthService;
import blog.service.UserAccountService;
import blog.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService
{
    private static final String GITHUB_AUTHORIZE_URL = "https://github.com/login/oauth/authorize";
    private static final String GITHUB_ACCESS_TOKEN_URL = "https://github.com/login/oauth/access_token";
    private static final String GITHUB_USER_URL = "https://api.github.com/user";
    private static final String GITHUB_USER_EMAILS_URL = "https://api.github.com/user/emails";
    private static final String GITHUB_STATE_PREFIX = "github:oauth:state:";
    private static final Map<String, Instant> LOCAL_GITHUB_STATE_CACHE = new ConcurrentHashMap<>();

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserAccountService userAccountService;
    @Autowired
    private GithubOauthProperties githubOauthProperties;
    @Autowired
    private StringRedisTemplate redisTemplate;

    private final RestTemplate restTemplate = new RestTemplate();

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
        return new LoginResponse(accessToken, refreshToken, username, false);
    }

    @Override
    public Map<String, String> buildGithubAuthorizePayload()
    {
        ensureGithubConfigured();

        String state = UUID.randomUUID().toString().replace("-", "");
        try {
            redisTemplate.opsForValue().set(GITHUB_STATE_PREFIX + state, "1", Duration.ofMinutes(10));
        } catch (Exception e) {
            LOCAL_GITHUB_STATE_CACHE.put(state, Instant.now().plus(Duration.ofMinutes(10)));
        }

        String authorizeUrl = UriComponentsBuilder.fromHttpUrl(GITHUB_AUTHORIZE_URL)
                .queryParam("client_id", githubOauthProperties.getClientId())
                .queryParam("redirect_uri", githubOauthProperties.getRedirectUri())
                .queryParam("scope", "read:user user:email")
                .queryParam("state", state)
                .build(true)
                .toUriString();

        Map<String, String> result = new HashMap<>();
        result.put("url", authorizeUrl);
        result.put("state", state);
        return result;
    }

    @Override
    public LoginResponse loginByGithub(GithubLoginDTO request)
    {
        ensureGithubConfigured();
        if (request == null || !StringUtils.hasText(request.getCode())) {
            throw new IllegalArgumentException("GitHub 授权码不能为空");
        }
        validateGithubState(request.getState());

        String githubAccessToken = exchangeGithubAccessToken(request.getCode());
        GithubOAuthUserDTO githubUser = fetchGithubUser(githubAccessToken);
        var userAccount = userAccountService.saveOrUpdateGithubUser(githubUser);

        List<String> roles = List.of("ROLE_" + Objects.requireNonNullElse(userAccount.getRole(), "USER").toUpperCase());
        String accessToken = jwtUtil.createAccessToken(userAccount.getUsername(), roles);
        String refreshToken = jwtUtil.createRefreshToken(userAccount.getUsername());
        boolean needsPasswordSetup = !StringUtils.hasText(userAccount.getPasswordHash());
        return new LoginResponse(accessToken, refreshToken, userAccount.getUsername(), needsPasswordSetup);
    }

    private void ensureGithubConfigured()
    {
        if (!StringUtils.hasText(githubOauthProperties.getClientId()) || !StringUtils.hasText(githubOauthProperties.getClientSecret())) {
            throw new IllegalStateException("GitHub OAuth 未配置，请设置 github.oauth.client-id 和 github.oauth.client-secret");
        }
    }

    private void validateGithubState(String state)
    {
        if (!StringUtils.hasText(state)) {
            throw new IllegalArgumentException("缺少 GitHub OAuth state");
        }

        String redisKey = GITHUB_STATE_PREFIX + state;
        try {
            Boolean exists = redisTemplate.hasKey(redisKey);
            if (Boolean.FALSE.equals(exists)) {
                throw new IllegalArgumentException("GitHub OAuth state 无效或已过期");
            }
            redisTemplate.delete(redisKey);
            return;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            Instant expiry = LOCAL_GITHUB_STATE_CACHE.get(state);
            if (expiry == null || expiry.isBefore(Instant.now())) {
                throw new IllegalArgumentException("GitHub OAuth state 无效或已过期");
            }
            LOCAL_GITHUB_STATE_CACHE.remove(state);
        }
    }

    private String exchangeGithubAccessToken(String code)
    {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", githubOauthProperties.getClientId());
        formData.add("client_secret", githubOauthProperties.getClientSecret());
        formData.add("code", code);
        formData.add("redirect_uri", githubOauthProperties.getRedirectUri());

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                GITHUB_ACCESS_TOKEN_URL,
                HttpMethod.POST,
                new HttpEntity<>(formData, headers),
                new ParameterizedTypeReference<>() {
                }
        );

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || !StringUtils.hasText((String) responseBody.get("access_token"))) {
            throw new IllegalStateException("获取 GitHub access token 失败：" + (responseBody != null ? responseBody.get("error_description") : "空响应"));
        }

        return (String) responseBody.get("access_token");
    }

    private GithubOAuthUserDTO fetchGithubUser(String githubAccessToken)
    {
        HttpHeaders headers = buildGithubHeaders(githubAccessToken);

        ResponseEntity<Map<String, Object>> userResponse = restTemplate.exchange(
                GITHUB_USER_URL,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {
                }
        );

        Map<String, Object> userBody = userResponse.getBody();
        if (userBody == null) {
            throw new IllegalStateException("获取 GitHub 用户信息失败");
        }

        Long githubId = Long.valueOf(String.valueOf(userBody.get("id")));
        String githubLogin = Objects.toString(userBody.get("login"), "");
        String nickname = firstNonBlank(
                Objects.toString(userBody.get("name"), null),
                githubLogin
        );
        String avatar = Objects.toString(userBody.get("avatar_url"), null);
        String email = Objects.toString(userBody.get("email"), null);
        String bio = Objects.toString(userBody.get("bio"), null);

        if (!StringUtils.hasText(email)) {
            email = fetchPrimaryGithubEmail(headers);
        }

        return new GithubOAuthUserDTO(githubId, githubLogin, nickname, avatar, email, bio);
    }

    private String fetchPrimaryGithubEmail(HttpHeaders headers)
    {
        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                GITHUB_USER_EMAILS_URL,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {
                }
        );

        List<Map<String, Object>> emails = response.getBody();
        if (emails == null || emails.isEmpty()) {
            return null;
        }

        return emails.stream()
                .filter(item -> Boolean.TRUE.equals(item.get("primary")) || Boolean.TRUE.equals(item.get("verified")))
                .map(item -> Objects.toString(item.get("email"), null))
                .filter(StringUtils::hasText)
                .findFirst()
                .orElseGet(() -> emails.stream()
                        .map(item -> Objects.toString(item.get("email"), null))
                        .filter(StringUtils::hasText)
                        .findFirst()
                        .orElse(null));
    }

    private HttpHeaders buildGithubHeaders(String accessToken)
    {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.add("X-GitHub-Api-Version", "2022-11-28");
        return headers;
    }

    private String firstNonBlank(String first, String second)
    {
        if (StringUtils.hasText(first)) {
            return first;
        }
        return second;
    }
}
