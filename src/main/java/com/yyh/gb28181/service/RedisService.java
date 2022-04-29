package com.yyh.gb28181.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * @author: yyh
 * @date: 2022-04-19 17:36
 * @description: RedisService
 * 只封装了部分方法，还可以扩展
 **/
@Component
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;
    /**
     * 获取链接工厂
     */
    public RedisConnectionFactory getConnectionFactory() {
        return redisTemplate.getConnectionFactory();
    }

    /**
     * 自增数
     * @param key key
     * @return long
     */
    public long increment(String key) {
        RedisAtomicLong redisAtomicLong = new RedisAtomicLong(key, getConnectionFactory());
        return redisAtomicLong.incrementAndGet();
    }

    /**
     * 自增数（带过期时间）
     * @param key key
     * @param time time
     * @param timeUnit timeUnit
     * @return long
     */
    public long increment(String key, long time, TimeUnit timeUnit) {
        RedisAtomicLong redisAtomicLong = new RedisAtomicLong(key, getConnectionFactory());
        redisAtomicLong.expire(time, timeUnit);
        return redisAtomicLong.incrementAndGet();
    }

    /**
     * 自增数（带过期时间）
     * @param key key
     * @param expireAt expireAt
     * @return long
     */
    public long increment(String key, Instant expireAt) {
        RedisAtomicLong redisAtomicLong = new RedisAtomicLong(key, getConnectionFactory());
        redisAtomicLong.expireAt(expireAt);
        return redisAtomicLong.incrementAndGet();
    }

    /**
     * 自增数（带过期时间和步长）
     * @param key key
     * @param increment increment
     * @param time time
     * @param timeUnit timeUnit
     * @return long
     */
    public long increment(String key, int increment, long time, TimeUnit timeUnit) {
        RedisAtomicLong redisAtomicLong = new RedisAtomicLong(key, getConnectionFactory());
        redisAtomicLong.expire(time, timeUnit);
        return redisAtomicLong.incrementAndGet();
    }
}

