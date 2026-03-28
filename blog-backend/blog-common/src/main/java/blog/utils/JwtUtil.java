package blog.utils;

import blog.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JwtUtil {

    private static final long DEFAULT_ACCESS_TOKEN_EXPIRATION = 2 * 60 * 60 * 1000L;
    private static final long DEFAULT_REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000L;
    private static final String DEFAULT_SECRET = "dream-studio-default-secret-key-please-change";
    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS_TOKEN_TYPE = "ACCESS";
    private static final String REFRESH_TOKEN_TYPE = "REFRESH";

    private final JwtProperties jwtProperties;

    public JwtUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String createToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        return buildToken(username, claims, resolveAccessTokenExpiration());
    }

<<<<<<< HEAD
    public String createAccessToken(String username, Long userId, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles == null ? Collections.emptyList() : roles);
        claims.put(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE);
        if (userId != null) {
            claims.put("userId", userId);
        }
=======
    public String createAccessToken(String username, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles == null ? Collections.emptyList() : roles);
        claims.put(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE);
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
        return buildToken(username, claims, resolveAccessTokenExpiration());
    }

    public String createRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE);
        return buildToken(username, claims, resolveRefreshTokenExpiration());
    }

    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private String buildToken(String subject, Map<String, Object> claims, long expiration) {
        Date issuedAt = new Date();
        Date expiresAt = new Date(issuedAt.getTime() + expiration);

        return Jwts.builder()
                .setClaims(new HashMap<>(claims))
                .setSubject(subject)
                .setIssuedAt(issuedAt)
                .setExpiration(expiresAt)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private SecretKey getSigningKey() {
        String secret = jwtProperties.getSecret();
        if (!StringUtils.hasText(secret)) {
            secret = DEFAULT_SECRET;
        }

        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            keyBytes = Arrays.copyOf(keyBytes, 32);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private long resolveAccessTokenExpiration() {
        long configured = jwtProperties.getAccessTokenExpiration();
        return configured > 0 ? configured : DEFAULT_ACCESS_TOKEN_EXPIRATION;
    }

    private long resolveRefreshTokenExpiration() {
        long configured = jwtProperties.getRefreshTokenExpiration();
        return configured > 0 ? configured : DEFAULT_REFRESH_TOKEN_EXPIRATION;
    }
}
