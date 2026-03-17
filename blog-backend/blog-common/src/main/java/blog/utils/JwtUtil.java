package blog.utils;

import blog.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class JwtUtil
{
    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private StringRedisTemplate redisTemplate;


    private SecretKey getSigningKey(){
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(String username, List<String> roles)
    {
        return Jwts.builder()
                .setSubject(username)
                .claim("roles",roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getAccessTokenExpiration()))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(String username)
    {
        String refreshToken = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getRefreshTokenExpiration()))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();


        // 将 Refresh Token 存入 Redis，Key 为 username，Value 为 token
        // 作用：实现了服务端对登录状态的控制。如果想踢人下线，直接删 Redis 即可。
        try {
            redisTemplate.opsForValue().set(
                    "REFRESH_TOKEN:" + username,
                    refreshToken,
                    jwtProperties.getRefreshTokenExpiration(),
                    TimeUnit.MILLISECONDS
            );
        } catch (Exception e) {
            log.warn("Redis 不可用，Refresh Token 将仅基于 JWT 本身校验：{}", e.getMessage());
        }

        return refreshToken;
    }


//    解析 Token
    public Claims parseToken(String token)
    {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validataRefreshToken(String username,String incomingToken)
    {
        try {
            String storedToken = redisTemplate.opsForValue().get("REFRESH_TOKEN:" + username);
            if (storedToken != null) {
                return storedToken.equals(incomingToken);
            }
        } catch (Exception e) {
            log.warn("Redis 不可用，跳过 Refresh Token 存储校验：{}", e.getMessage());
        }

        try {
            Claims claims = parseToken(incomingToken);
            return username.equals(claims.getSubject());
        } catch (Exception e) {
            return false;
        }
    }
}
