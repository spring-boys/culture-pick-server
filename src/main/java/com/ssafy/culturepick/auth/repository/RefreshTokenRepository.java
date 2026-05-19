package com.ssafy.culturepick.auth.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private static final String REFRESH_TOKEN_KEY = "refreshToken:";
    private static final String MEMBER_KEY = "refreshTokenMember:";
    private static final Duration TTL = Duration.ofDays(14);

    private final RedisTemplate<String, String> redisTemplate;

    public void save(Long memberId, String refreshToken) {
        String oldRefreshToken = redisTemplate.opsForValue().get(REFRESH_TOKEN_KEY + memberId);
        if (oldRefreshToken != null) {
            redisTemplate.delete(MEMBER_KEY + oldRefreshToken);
        }

        redisTemplate.opsForValue().set(REFRESH_TOKEN_KEY + memberId, refreshToken, TTL);
        redisTemplate.opsForValue().set(MEMBER_KEY + refreshToken, String.valueOf(memberId), TTL);
    }

    public Optional<Long> findMemberIdByRefreshToken(String refreshToken) {
        String memberId = redisTemplate.opsForValue().get(MEMBER_KEY + refreshToken);
        return Optional.ofNullable(memberId).map(Long::valueOf);
    }

    public void deleteByRefreshToken(String refreshToken) {
        String memberId = redisTemplate.opsForValue().get(MEMBER_KEY + refreshToken);
        if (memberId != null) {
            redisTemplate.delete(REFRESH_TOKEN_KEY + memberId);
        }
        redisTemplate.delete(MEMBER_KEY + refreshToken);
    }

    public void deleteByMemberId(Long memberId) {
        String refreshToken = redisTemplate.opsForValue().get(REFRESH_TOKEN_KEY + memberId);
        if (refreshToken != null) {
            redisTemplate.delete(MEMBER_KEY + refreshToken);
        }
        redisTemplate.delete(REFRESH_TOKEN_KEY + memberId);
    }
}
