package com.prettygoodorg.pluto_api.api.auth.oauth2;

import com.prettygoodorg.pluto_api.api.auth.dto.response.OAuthLoginResponse;
import com.prettygoodorg.pluto_api.api.auth.dto.response.TokenResponse;
import com.prettygoodorg.pluto_api.api.auth.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final OAuth2UserInfoMapper oAuth2UserInfoMapper;

    @Value("${spring.security.oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        OAuth2User principal = token.getPrincipal();
        OAuthUserInfo userInfo = oAuth2UserInfoMapper.map(token.getAuthorizedClientRegistrationId(), principal.getAttributes());
        OAuthLoginResponse loginResponse = authService.oauthLogin(userInfo);

        Map<String, String> params = new LinkedHashMap<>();
        params.put("provider", userInfo.getProvider());

        if (loginResponse.isNewUser()) {
            params.put("status", "signup");
            params.put("tempKey", loginResponse.getTempKey());
        } else {
            TokenResponse tokens = loginResponse.getTokens();
            params.put("status", "login");
            params.put("accessToken", tokens.getAccessToken());
            params.put("refreshToken", tokens.getRefreshToken());
            params.put("accessTokenExpiresIn", String.valueOf(tokens.getAccessTokenExpiresIn()));
        }

        response.sendRedirect(redirectUri + "#" + toFragment(params));
    }

    private String toFragment(Map<String, String> params) {
        return params.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }
}
