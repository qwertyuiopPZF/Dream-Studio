package blog.service.impl;

import blog.service.ViewCountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ViewCountServiceImpl implements ViewCountService
{
    @Autowired
    private StringRedisTemplate redisTemplate;

    private  static final String VIEW_COUNT_KEY = "article:view_count";

    /**
     * 1. 增加浏览量 (每次调用+1)
     */
    public void incrementViewCount(Long articleId,String ip)
    {
        try {
            String ipRecordKey = "article:view:record:" + articleId + ":" + ip;
            Boolean isFirstVisit = redisTemplate.opsForValue()
                    .setIfAbsent(ipRecordKey, "1", 10, TimeUnit.MINUTES);

            if (Boolean.TRUE.equals(isFirstVisit))
            {
                redisTemplate.opsForHash().increment(VIEW_COUNT_KEY, String.valueOf(articleId), 1);
            }
        } catch (Exception e) {
            log.warn("Redis 不可用，跳过浏览量去重统计：{}", e.getMessage());
        }
    }

    public Integer getViewCount (Long articleId)
    {
        try {
            Object count = redisTemplate.opsForHash().get(VIEW_COUNT_KEY,String.valueOf((articleId)));
            if(count != null)
            {
                return Integer.parseInt((String) count);
            }
        } catch (Exception e) {
            log.warn("Redis 不可用，浏览量读取回退为 0：{}", e.getMessage());
        }
        return 0;
    }


    public Map<Object,Object> getAllViewCounts()
    {
        try {
            return redisTemplate.opsForHash().entries(VIEW_COUNT_KEY);
        } catch (Exception e) {
            log.warn("Redis 不可用，返回空浏览量集合：{}", e.getMessage());
            return Map.of();
        }
    }
}
