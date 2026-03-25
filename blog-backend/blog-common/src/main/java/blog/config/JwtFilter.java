package blog.config;

import blog.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    // 🔥 第一行就强制设置，保证永远不会报 Missing
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // 1. 强制初始化 userId 为 null（这一行是核心！）
        request.setAttribute("userId", null);

        try {
            String bearerToken = request.getHeader("Authorization");
            if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
                String token = bearerToken.substring(7);
                Claims claims = jwtUtil.parseToken(token);

                // 2. 解析 userId
                Long userId = null;
                try {
                    Object uid = claims.get("userId");
                    if (uid instanceof Number) userId = ((Number) uid).longValue();
                    else if (uid instanceof String) userId = Long.parseLong((String) uid);
                } catch (Exception ignore) {}

                // 3. 存入 request（覆盖之前的 null）
                if (userId != null) {
                    request.setAttribute("userId", userId);
                    log.info("✅ Token 解析成功，userId = {}", userId);
                }

                // 4. 处理 Spring Security 权限
                String username = claims.getSubject();
                List<String> roles = claims.get("roles", List.class);
                List<SimpleGrantedAuthority> authorities = (roles == null) ? List.of() :
                        roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());

                if (StringUtils.hasText(username)) {
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        } catch (Exception e) {
            log.error("❌ JWT 处理异常（已忽略）: {}", e.getMessage());
        }

        // 5. 放行
        chain.doFilter(request, response);
    }
}
