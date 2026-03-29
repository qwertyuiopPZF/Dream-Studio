package blog.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Data
@Component
@ConfigurationProperties(prefix = "object-storage")
public class ObjectStorageProperties
{
    private String endpoint = "";//endpoint（存储服务地址）
    private String region = "us-east-1";//region（区域）
    private String accessKey = "";//accessKey（账号）
    private String secretKey = "";//secretKey（密码）
    private String bucket = "";//bucket（桶）
    private String publicUrl = "";//publicUrl（访问地址）
    private String keyPrefix = "blog";//keyPrefix（路径前缀）
    private boolean pathStyleAccess = true;

    public boolean isConfigured()
    {
        return StringUtils.hasText(endpoint)
                && StringUtils.hasText(accessKey)
                && StringUtils.hasText(secretKey)
                && StringUtils.hasText(bucket);
    }
}
