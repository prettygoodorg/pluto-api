package com.prettygoodorg.pluto_api.api.auth.repository;

import com.prettygoodorg.pluto_api.api.auth.entity.PendingUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RedisTokenRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private final PendingUserRepository pendingUserRepository;

    private static final String RT_PREFIX = "auth:rt:";
    private static final String USER_RT_PREFIX = "auth:user:rt:";

    public void savePendingUser(PendingUser pendingUser) {
        pendingUserRepository.save(pendingUser);
    }

    public Optional<PendingUser> findPendingUser(String tempKey) {
        return pendingUserRepository.findById(tempKey);
    }

    public void deletePendingUser(String tempKey) {
        pendingUserRepository.deleteById(tempKey);
    }

    public void saveRefreshToken(String rtUUID, UUID userId, Duration ttl) {
        redisTemplate.opsForValue().set(RT_PREFIX + rtUUID, userId.toString(), ttl);
    }

    public Optional<UUID> findUserIdByRefreshToken(String rtUUID) {
        String userId = redisTemplate.opsForValue().get(RT_PREFIX + rtUUID);
        if (userId == null) return Optional.empty();
        return Optional.of(UUID.fromString(userId));
    }

    public void deleteRefreshToken(String rtUUID) {
        redisTemplate.delete(RT_PREFIX + rtUUID);
    }

    public void saveUserCurrentRt(UUID userId, String rtUUID, Duration ttl) {
        redisTemplate.opsForValue().set(USER_RT_PREFIX + userId, rtUUID, ttl);
    }

    public Optional<String> findCurrentRtByUserId(UUID userId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(USER_RT_PREFIX + userId));
    }

    public void deleteUserCurrentRt(UUID userId) {
        redisTemplate.delete(USER_RT_PREFIX + userId);
    }

    public void invalidatePreviousSession(UUID userId) {
        findCurrentRtByUserId(userId).ifPresent(oldRtUUID -> {
            deleteRefreshToken(oldRtUUID);
            deleteUserCurrentRt(userId);
        });
    }
}
