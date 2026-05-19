package com.ssafy.culturepick.auth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class EmailVerificationRepository {

    private static final String CODE_KEY = "emailCode:";
    private static final String VERIFIED_KEY = "emailVerified:";
    private static final Duration CODE_TTL = Duration.ofMinutes(5);
    private static final Duration VERIFIED_TTL = Duration.ofMinutes(30);

    private final RedisTemplate<String, String> redisTemplate;

    public void saveCode(String email, String code) {
        redisTemplate.opsForValue().set(CODE_KEY + email, code, CODE_TTL);
    }

    public String getCode(String email) {
        return redisTemplate.opsForValue().get(CODE_KEY + email);
    }

    public void deleteCode(String email) {
        redisTemplate.delete(CODE_KEY + email);
    }

    public void saveVerified(String email) {
        redisTemplate.opsForValue().set(VERIFIED_KEY + email, "true", VERIFIED_TTL);
    }

    public boolean isVerified(String email) {
        return Boolean.TRUE.toString().equals(redisTemplate.opsForValue().get(VERIFIED_KEY + email));
    }

    public void deleteVerified(String email) {
        redisTemplate.delete(VERIFIED_KEY + email);
    }
}
