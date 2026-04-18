package com.prettygoodorg.pluto_api.api.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@RedisHash("auth:pending")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingUser {
    @Id
    private String id;
    private String email;
    private String nickname;
    private String profileImg;
    private String provider;
    private String providerId;
    @TimeToLive
    private Long ttl;
}
