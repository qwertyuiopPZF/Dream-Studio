package blog.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "github.login")
public class GithubOauthProperties
{
    private String clientId;

    private String clientSecret;

    private String redirectUri;
}
