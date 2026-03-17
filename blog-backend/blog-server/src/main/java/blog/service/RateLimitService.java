package blog.service;

public interface RateLimitService
{
    boolean tryAcquire(String action, String identity, int maxRequests, long windowSeconds);
}
