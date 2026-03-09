package blog.utils;

import blog.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtil
{
    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private StringRedisTemplate redisTemplate;

<<<<<<< HEAD
=======

>>>>>>> d5a7cbf233b4e1842632f054b48bc235a6356241
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

<<<<<<< HEAD
        // 将 Refresh Token 存入 Redis
=======

        // 将 Refresh Token 存入 Redis，Key 为 username，Value 为 token
        // 作用：实现了服务端对登录状态的控制。如果想踢人下线，直接删 Redis 即可。
>>>>>>> d5a7cbf233b4e1842632f054b48bc235a6356241
        redisTemplate.opsForValue().set(
                "REFRESH_TOKEN:" + username,
                refreshToken,
                jwtProperties.getRefreshTokenExpiration(),
                TimeUnit.MILLISECONDS
        );

        return refreshToken;
    }

<<<<<<< HEAD
    // 解析 Token
=======

//    解析 Token
>>>>>>> d5a7cbf233b4e1842632f054b48bc235a6356241
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
        String storedToken = redisTemplate.opsForValue().get("REFRESH_TOKEN:" + username);
        return storedToken != null && storedToken.equals(incomingToken);
    }
<<<<<<< HEAD

    /**
     * 创建访问令牌（用于登录接口）
     * @param id 用户ID（虽然没用到，但保留参数）
     * @param username 用户名
     * @return JWT token字符串
     */
    public String createToken(Long id, String username) {
        // 给个默认角色
        List<String> defaultRoles = List.of("user");
        return createAccessToken(username, defaultRoles);
    }

    /**
     * 创建带自定义角色的访问令牌
     * @param id 用户ID
     * @param username 用户名
     * @param roles 角色列表
     * @return JWT token字符串
     */
    public String createTokenWithRoles(Long id, String username, List<String> roles) {
        return createAccessToken(username, roles);
    }
}
=======
}
>>>>>>> d5a7cbf233b4e1842632f054b48bc235a6356241
