package blog.service.impl;

import blog.service.RateLimitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RateLimitServiceImpl implements RateLimitService
{
    private static final String RATE_LIMIT_PREFIX = "rate-limit:";

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public boolean tryAcquire(String action, String identity, int maxRequests, long windowSeconds)
    {
        String limiterIdentity = (identity == null || identity.trim().isEmpty()) ? "unknown" : identity.trim();
        String key = RATE_LIMIT_PREFIX + action + ":" + limiterIdentity;

        try {
            Long currentCount = redisTemplate.opsForValue().increment(key);
            if (currentCount != null && currentCount == 1L) {
                redisTemplate.expire(key, windowSeconds, TimeUnit.SECONDS);
            }
            return currentCount != null && currentCount <= maxRequests;
        } catch (Exception e) {
            log.warn("Redis 不可用，跳过限流逻辑：{}", e.getMessage());
            return true;
        }
    }
}
