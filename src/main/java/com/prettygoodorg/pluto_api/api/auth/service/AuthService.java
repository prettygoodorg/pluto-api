package com.prettygoodorg.pluto_api.api.auth.service;

import com.fasterxml.uuid.Generators;
import com.prettygoodorg.pluto_api.api.auth.dto.request.LogoutRequest;
import com.prettygoodorg.pluto_api.api.auth.dto.request.TermsAgreeRequest;
import com.prettygoodorg.pluto_api.api.auth.dto.request.TokenRefreshRequest;
import com.prettygoodorg.pluto_api.api.auth.dto.response.OAuthLoginResponse;
import com.prettygoodorg.pluto_api.api.auth.dto.response.TokenResponse;
import com.prettygoodorg.pluto_api.api.auth.enums.OAuthProvider;
import com.prettygoodorg.pluto_api.api.user.entity.User;
import com.prettygoodorg.pluto_api.api.user.repository.UserRepository;
import com.prettygoodorg.pluto_api.common.jwt.JwtProperties;
import com.prettygoodorg.pluto_api.common.exception.ErrorCodeImpl;
import com.prettygoodorg.pluto_api.common.exception.BizException;
import com.prettygoodorg.pluto_api.common.jwt.JwtProvider;
import com.prettygoodorg.pluto_api.api.auth.oauth2.OAuthUserInfo;
import com.prettygoodorg.pluto_api.api.auth.entity.PendingUser;
import com.prettygoodorg.pluto_api.api.auth.repository.RedisTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RedisTokenRepository redisTokenRepository;
    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;

    @Value("#{T(java.time.Duration).ofDays(${auth.refresh-token-expiry-days})}")
    private Duration rtTtl;

    @Value("#{T(java.time.Duration).ofMinutes(${auth.pending-user-expiry-minutes})}")
    private Duration pendingTtl;

    public OAuthLoginResponse oauthLogin(OAuthUserInfo userInfo) {
        String provider = userInfo.getProvider().toUpperCase();
        Optional<User> existingUser = userRepository.findByProviderAndProviderId(OAuthProvider.valueOf(provider), userInfo.getProviderId());

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            redisTokenRepository.invalidatePreviousSession(user.getId());
            TokenResponse tokens = issueTokens(user);
            return new OAuthLoginResponse(false, null, tokens);
        }

        String tempKey = Generators.timeBasedEpochRandomGenerator().generate().toString();
        PendingUser pendingUser = PendingUser.builder()
                .id(tempKey)
                .email(userInfo.getEmail())
                .nickname(userInfo.getNickname())
                .profileImg(userInfo.getProfileImg())
                .provider(provider)
                .providerId(userInfo.getProviderId())
                .ttl(pendingTtl.getSeconds())
                .build();
        redisTokenRepository.savePendingUser(pendingUser);
        log.debug("신규 OAuth 사용자 임시 저장: provider={}, tempKey={}", provider, tempKey);

        return new OAuthLoginResponse(true, tempKey, null);
    }

    @Transactional
    public TokenResponse agreeTerms(TermsAgreeRequest request) {
        PendingUser pendingInfo = redisTokenRepository.findPendingUser(request.getTempKey())
                .orElseThrow(() -> new BizException(ErrorCodeImpl.PENDING_USER_NOT_FOUND));

        OAuthUserInfo oAuthUserInfo = new OAuthUserInfo(
                pendingInfo.getProviderId(),
                pendingInfo.getEmail(),
                pendingInfo.getNickname(),
                pendingInfo.getProfileImg(),
                pendingInfo.getProvider()
        );
        User newUser = User.create(oAuthUserInfo, LocalDateTime.now());
        userRepository.save(newUser);

        redisTokenRepository.deletePendingUser(request.getTempKey());
        log.debug("신규 회원 등록 완료: userId={}", newUser.getId());

        return issueTokens(newUser);
    }

    public TokenResponse refreshToken(TokenRefreshRequest request) {
        String rtUUID = request.getRefreshToken();

        UUID userId = redisTokenRepository.findUserIdByRefreshToken(rtUUID)
                .orElseThrow(() -> new BizException(ErrorCodeImpl.INVALID_REFRESH_TOKEN));

        redisTokenRepository.deleteRefreshToken(rtUUID);
        redisTokenRepository.deleteUserCurrentRt(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BizException(ErrorCodeImpl.INVALID_REFRESH_TOKEN));

        return issueTokens(user);
    }

    public void logout(LogoutRequest request) {
        redisTokenRepository.findUserIdByRefreshToken(request.getRefreshToken()).ifPresent((UUID userId) -> {
            redisTokenRepository.deleteRefreshToken(request.getRefreshToken());
            redisTokenRepository.deleteUserCurrentRt(userId);
        });
    }

    private TokenResponse issueTokens(User user) {
        String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getRole().name());
        String rtUUID = UUID.randomUUID().toString();

        redisTokenRepository.saveRefreshToken(rtUUID, user.getId(), rtTtl);
        redisTokenRepository.saveUserCurrentRt(user.getId(), rtUUID, rtTtl);

        return new TokenResponse(accessToken, rtUUID, jwtProperties.getAccessTokenExpiryMillis());
    }

}
