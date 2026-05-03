package com.prettygoodorg.pluto_api.api.auth.oauth2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prettygoodorg.pluto_api.common.exception.BizException;
import com.prettygoodorg.pluto_api.common.exception.ErrorCodeImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class RedisAuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final String PREFIX = "auth:oauth2:req:";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("#{T(java.time.Duration).ofMinutes(${spring.security.oauth2.authorization-request-expiry-minutes})}")
    private Duration ttl;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        String state = request.getParameter("state");
        if (state == null || state.isBlank()) {
            return null;
        }

        String payload = redisTemplate.opsForValue().get(key(state));
        if (payload == null) {
            return null;
        }

        try {
            StoredAuthorizationRequest stored = objectMapper.readValue(payload, StoredAuthorizationRequest.class);
            return OAuth2AuthorizationRequest.authorizationCode()
                    .authorizationUri(stored.authorizationUri())
                    .clientId(stored.clientId())
                    .redirectUri(stored.redirectUri())
                    .scopes(stored.scopes())
                    .state(stored.state())
                    .additionalParameters(stored.additionalParameters())
                    .attributes(stored.attributes())
                    .authorizationRequestUri(stored.authorizationRequestUri())
                    .build();
        } catch (JsonProcessingException e) {
            throw new BizException(ErrorCodeImpl.OAUTH_PROVIDER_ERROR);
        }
    }

    @Override
    public void saveAuthorizationRequest(
            OAuth2AuthorizationRequest authorizationRequest,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (authorizationRequest == null) {
            String state = request.getParameter("state");
            if (state != null && !state.isBlank()) {
                redisTemplate.delete(key(state));
            }
            return;
        }

        StoredAuthorizationRequest stored = new StoredAuthorizationRequest(
                authorizationRequest.getAuthorizationUri(),
                authorizationRequest.getClientId(),
                authorizationRequest.getRedirectUri(),
                authorizationRequest.getScopes(),
                authorizationRequest.getState(),
                authorizationRequest.getAdditionalParameters(),
                authorizationRequest.getAttributes(),
                authorizationRequest.getAuthorizationRequestUri()
        );

        try {
            redisTemplate.opsForValue().set(key(authorizationRequest.getState()), objectMapper.writeValueAsString(stored), ttl);
        } catch (JsonProcessingException e) {
            throw new BizException(ErrorCodeImpl.OAUTH_PROVIDER_ERROR);
        }
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        OAuth2AuthorizationRequest authorizationRequest = loadAuthorizationRequest(request);
        String state = request.getParameter("state");
        if (state != null && !state.isBlank()) {
            redisTemplate.delete(key(state));
        }
        return authorizationRequest;
    }

    private String key(String state) {
        return PREFIX + state;
    }

    private record StoredAuthorizationRequest(
            String authorizationUri,
            String clientId,
            String redirectUri,
            Set<String> scopes,
            String state,
            Map<String, Object> additionalParameters,
            Map<String, Object> attributes,
            String authorizationRequestUri
    ) {}
}
