package blog.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil
{
    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_TOKEN_TYPE = "tokenType";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    public String createAccessToken(String username, List<String> roles)
    {
        return buildToken(username, null, roles, "access", accessTokenExpiration);
    }

    public String createRefreshToken(String username)
    {
        return buildToken(username, null, null, "refresh", refreshTokenExpiration);
    }

    public String createToken(Long userId, String username)
    {
        return buildToken(username, userId, null, "access", accessTokenExpiration);
    }

    public Claims parseToken(String token)
    {
        if (!StringUtils.hasText(token)) {
            throw new IllegalArgumentException("JWT token 不能为空");
        }

        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token)
    {
        return parseToken(token).getSubject();
    }

    public Long extractUserId(String token)
    {
        Object userId = parseToken(token).get(CLAIM_USER_ID);
        if (userId == null) {
            return null;
        }
        return Long.valueOf(String.valueOf(userId));
    }

    public List<String> extractRoles(String token)
    {
        return parseToken(token).get(CLAIM_ROLES, List.class);
    }

    public String extractTokenType(String token)
    {
        return parseToken(token).get(CLAIM_TOKEN_TYPE, String.class);
    }

    public boolean isTokenExpired(String token)
    {
        Date expiration = parseToken(token).getExpiration();
        return expiration != null && expiration.before(new Date());
    }

    public boolean validateToken(String token, String username)
    {
        Claims claims = parseToken(token);
        return StringUtils.hasText(username)
                && username.equals(claims.getSubject())
                && !isTokenExpired(token);
    }

    private String buildToken(String username, Long userId, List<String> roles, String tokenType, long expireMillis)
    {
        if (!StringUtils.hasText(username)) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (!StringUtils.hasText(secret)) {
            throw new IllegalStateException("jwt.secret 未配置");
        }

        Instant now = Instant.now();
        var builder = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(expireMillis)))
                .claim(CLAIM_TOKEN_TYPE, tokenType)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256);

        if (userId != null) {
            builder.claim(CLAIM_USER_ID, userId);
        }
        if (roles != null && !roles.isEmpty()) {
            builder.claim(CLAIM_ROLES, roles);
        }

        return builder.compact();
    }

    private SecretKey getSigningKey()
    {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
