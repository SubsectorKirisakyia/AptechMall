package com.aptech.aptechMall.service.authentication;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {
    private final StringRedisTemplate redisTemplate;

    public void setToken(String token, String value, long ttl, TimeUnit unit){
        try {
            redisTemplate.opsForValue().set(token, value, ttl, unit);
            log.info("Token blacklisted in Redis: {}", token.substring(0, 20) + "...");
        } catch (Exception e) {
            log.error("Failed to blacklist token in Redis: {}", e.getMessage());
        }
    }

    public boolean hasToken(String token){
        try {
            return redisTemplate.hasKey(token);
        } catch (Exception e) {
            log.error("Failed to check token in Redis: {}", e.getMessage());
            return false;
        }
    }
}
