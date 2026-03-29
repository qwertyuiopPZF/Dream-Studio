package blog.service.impl;

import blog.dto.Login.GithubRegistrationRequest;
import blog.dto.Login.GithubRegistrationSession;
import blog.config.GithubOauthProperties;
import blog.dto.Login.GithubLoginDTO;
import blog.dto.Login.GithubOAuthUserDTO;
import blog.dto.Login.LoginRequest;
import blog.dto.Login.LoginResponse;
import blog.dto.Login.RefreshTokenRequest;
import blog.service.AuthService;
import blog.service.UserAccountService;
import blog.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import blog.vo.UserProfileVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
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
    private static final String GITHUB_REGISTRATION_PREFIX = "github:oauth:register:";
    private static final Map<String, Instant> LOCAL_GITHUB_STATE_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, GithubRegistrationSession> LOCAL_GITHUB_REGISTRATION_CACHE = new ConcurrentHashMap<>();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
        String role = roles.stream().anyMatch(item -> "ROLE_ADMIN".equalsIgnoreCase(item)) ? "ADMIN" : "USER";
        return new LoginResponse(accessToken, refreshToken, username, false, role);
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
                .build()
                .encode()
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
        var existingUser = userAccountService.findByGithubIdentity(githubUser.getGithubId(), githubUser.getGithubLogin());

        if (existingUser == null || !StringUtils.hasText(existingUser.getPasswordHash())) {
            return buildPendingGithubRegistration(existingUser, githubUser);
        }

        var userAccount = userAccountService.saveOrUpdateGithubUser(githubUser);
        return buildAuthenticatedResponse(userAccount.getUsername(), userAccount.getRole(), false);
    }

    @Override
    public LoginResponse registerByGithub(GithubRegistrationRequest request)
    {
        if (request == null || !StringUtils.hasText(request.getRegistrationToken())) {
            throw new IllegalArgumentException("注册令牌不能为空，请重新发起 GitHub 授权");
        }

        GithubRegistrationSession registrationSession = readGithubRegistrationSession(request.getRegistrationToken());
        if (registrationSession == null) {
            throw new IllegalArgumentException("GitHub 注册会话已失效，请重新发起授权");
        }

        var userAccount = userAccountService.completeGithubRegistration(
                registrationSession,
                request.getUsername(),
                request.getPhone(),
                request.getPassword()
        );
        clearGithubRegistrationSession(request.getRegistrationToken());

        return buildAuthenticatedResponse(userAccount.getUsername(), userAccount.getRole(), false);
    }

    private LoginResponse buildPendingGithubRegistration(blog.entity.UserAccount existingUser,
                                                         GithubOAuthUserDTO githubUser)
    {
        String suggestedUsername = existingUser != null && StringUtils.hasText(existingUser.getUsername())
                ? existingUser.getUsername()
                : generateSuggestedUsername(githubUser.getGithubLogin(), githubUser.getGithubId());

        GithubRegistrationSession registrationSession = new GithubRegistrationSession(
                existingUser != null ? existingUser.getId() : null,
                githubUser.getGithubId(),
                githubUser.getGithubLogin(),
                firstNonBlank(githubUser.getNickname(), existingUser != null ? existingUser.getNickname() : null),
                firstNonBlank(githubUser.getAvatar(), existingUser != null ? existingUser.getAvatar() : null),
                firstNonBlank(githubUser.getEmail(), existingUser != null ? existingUser.getEmail() : null),
                existingUser != null ? existingUser.getPhone() : null,
                firstNonBlank(githubUser.getBio(), existingUser != null ? existingUser.getBio() : null),
                suggestedUsername
        );
        String registrationToken = storeGithubRegistrationSession(registrationSession);

        return LoginResponse.githubRegistrationPending(
                registrationToken,
                suggestedUsername,
                registrationSession.getGithubLogin(),
                registrationSession.getNickname(),
                registrationSession.getAvatar(),
                registrationSession.getEmail(),
                registrationSession.getPhone()
        );
    }

    private LoginResponse buildAuthenticatedResponse(String username, String role, boolean needsPasswordSetup)
    {
        String resolvedRole = StringUtils.hasText(role) ? role.toUpperCase() : "USER";
        List<String> roles = List.of("ROLE_" + resolvedRole);
        String accessToken = jwtUtil.createAccessToken(username, roles);
        String refreshToken = jwtUtil.createRefreshToken(username);
        return new LoginResponse(accessToken, refreshToken, username, needsPasswordSetup, resolvedRole);
    }

    private String generateSuggestedUsername(String githubLogin, Long githubId)
    {
        String base = StringUtils.hasText(githubLogin) ? githubLogin.trim() : "github_user";
        String sanitized = base.replaceAll("[^A-Za-z0-9_-]", "_");
        if (!StringUtils.hasText(sanitized)) {
            sanitized = "github_user";
        }
        if (sanitized.length() > 30) {
            sanitized = sanitized.substring(0, 30);
        }
        if (sanitized.length() < 3 && githubId != null) {
            sanitized = (sanitized + "_" + githubId).substring(0, Math.min(30, (sanitized + "_" + githubId).length()));
        }
        return sanitized;
    }

    @Override
    public LoginResponse refreshToken(RefreshTokenRequest request)
    {
        if (request == null || !StringUtils.hasText(request.getRefreshToken())) {
            throw new BadCredentialsException("刷新令牌不能为空");
        }

        String username;
        try {
            var claims = jwtUtil.parseToken(request.getRefreshToken());
            String tokenType = claims.get("tokenType", String.class);
            if (StringUtils.hasText(tokenType) && !"REFRESH".equalsIgnoreCase(tokenType)) {
                throw new BadCredentialsException("刷新令牌类型无效");
            }
            username = claims.getSubject();
        } catch (BadCredentialsException e) {
            throw e;
        } catch (Exception e) {
            throw new BadCredentialsException("刷新令牌已失效，请重新登录", e);
        }

        if (!StringUtils.hasText(username)) {
            throw new BadCredentialsException("刷新令牌无效，请重新登录");
        }

        UserProfileVO profile = userAccountService.getProfileByUsername(username);
        if (profile == null) {
            throw new BadCredentialsException("用户不存在或已失效，请重新登录");
        }

        String role = StringUtils.hasText(profile.getRole()) ? profile.getRole().toUpperCase() : "USER";
        List<String> roles = List.of("ROLE_" + role);
        String accessToken = jwtUtil.createAccessToken(username, roles);
        String refreshToken = jwtUtil.createRefreshToken(username);
        boolean needsPasswordSetup = !Boolean.TRUE.equals(profile.getPasswordInitialized());
        return new LoginResponse(accessToken, refreshToken, username, needsPasswordSetup, role);
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

    private String storeGithubRegistrationSession(GithubRegistrationSession registrationSession)
    {
        String registrationToken = UUID.randomUUID().toString().replace("-", "");
        try {
            String payload = OBJECT_MAPPER.writeValueAsString(registrationSession);
            redisTemplate.opsForValue().set(GITHUB_REGISTRATION_PREFIX + registrationToken, payload, Duration.ofMinutes(10));
        } catch (Exception e) {
            LOCAL_GITHUB_REGISTRATION_CACHE.put(registrationToken, registrationSession);
        }
        return registrationToken;
    }

    private GithubRegistrationSession readGithubRegistrationSession(String registrationToken)
    {
        try {
            String payload = redisTemplate.opsForValue().get(GITHUB_REGISTRATION_PREFIX + registrationToken);
            if (StringUtils.hasText(payload)) {
                return OBJECT_MAPPER.readValue(payload, GithubRegistrationSession.class);
            }
        } catch (Exception e) {
            // ignore and fallback to local cache
        }

        return LOCAL_GITHUB_REGISTRATION_CACHE.get(registrationToken);
    }

    private void clearGithubRegistrationSession(String registrationToken)
    {
        try {
            redisTemplate.delete(GITHUB_REGISTRATION_PREFIX + registrationToken);
        } catch (Exception e) {
            // ignore and cleanup local cache
        }
        LOCAL_GITHUB_REGISTRATION_CACHE.remove(registrationToken);
    }

    private String firstNonBlank(String first, String second)
    {
        if (StringUtils.hasText(first)) {
            return first;
        }
        return second;
    }
}
