package com.mystikos.common.cache;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 固定窗口限流：同一个 key 在 window 时间窗口内最多放行 limit 次。
 * 用 INCR + 首次命中时补一个 EXPIRE 实现，不是严格意义的滑动窗口
 * （窗口边界附近有小的突发容忍），但对"防撞库/防验证码轰炸"这类场景足够，换严格算法没必要。
 */
@Component
public class RateLimiter {

    private final StringRedisTemplate redisTemplate;

    public RateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * @return true 表示本次放行，false 表示已达到本窗口内的次数上限
     */
    public boolean tryAcquire(String key, int limit, Duration window) {
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, window);
        }
        return count != null && count <= limit;
    }
}
