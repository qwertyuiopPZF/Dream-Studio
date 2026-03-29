package blog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig
{
    private final JwtFilter jwtFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    public SecurityConfig(JwtFilter jwtFilter,
                          JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint)
    {
        this.jwtFilter = jwtFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   UserDetailsService userDetailsService,
                                                   PasswordEncoder passwordEncoder) throws Exception
    {
        DaoAuthenticationProvider authenticationProvider = buildAuthenticationProvider(userDetailsService, passwordEncoder);

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .authenticationProvider(authenticationProvider)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/admin",
                                "/admin/articlemgmt",
                                "/admin/commentmgmt",
                                "/admin/momentsmgmt",
                                "/admin/content",
                                "/admin/writearticle",
                                "/admin/article/edit/*",
                                "/admin/writemoment",
                                "/admin/categorisemgmt",
                                "/admin/tagsmgmt",
                                "/admin/forum-entry",
                                "/admin/site",
                                "/admin/usermgmt"
                        ).permitAll()
                        .requestMatchers(
                                "/error",
                                "/favicon.ico",
                                "/static/**",
                                "/images/**",
                                "/upload/**",
                                "/webjars/**",
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/v3/api-docs/**",
                                "/actuator/health",
                                "/v1/auth/**",
                                "/api/auth/**"
                        ).permitAll()
                        .requestMatchers("/api/user/**", "/api/archive/**", "/admin/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/post", "/api/comments/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/comments/**").authenticated()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private DaoAuthenticationProvider buildAuthenticationProvider(UserDetailsService userDetailsService,
                                                                  PasswordEncoder passwordEncoder)
    {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
                                                       PasswordEncoder passwordEncoder)
    {
        return new ProviderManager(buildAuthenticationProvider(userDetailsService, passwordEncoder));
    }

    @Bean
    public PasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder();
    }
}
