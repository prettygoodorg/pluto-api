package com.prettygoodorg.pluto_api.api.auth.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.prettygoodorg.pluto_api.common.exception.ErrorCodeImpl;
import com.prettygoodorg.pluto_api.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class GoogleOAuthClient implements OAuthClient {

    @Value("${oauth.google.client-id}")
    private String clientId;

    @Value("${oauth.google.client-secret}")
    private String clientSecret;

    @Value("${oauth.google.token-uri}")
    private String tokenUri;

    @Value("${oauth.google.user-info-uri}")
    private String userInfoUri;

    private final RestClient restClient;

    @Override
    public String getProvider() {
        return "GOOGLE";
    }

    @Override
    public OAuthUserInfo getUserInfo(String authorizationCode, String redirectUri) {
        GoogleTokenResponse tokenResponse = exchangeCode(authorizationCode, redirectUri);
        GoogleUserInfoResponse userInfo = fetchUserInfo(tokenResponse.accessToken());

        return new OAuthUserInfo(
                userInfo.sub(),
                userInfo.email(),
                userInfo.name(),
                userInfo.picture(),
                getProvider()
        );
    }

    private GoogleTokenResponse exchangeCode(String code, String redirectUri) {
        String body = "code=" + code
                + "&client_id=" + clientId
                + "&client_secret=" + clientSecret
                + "&redirect_uri=" + redirectUri
                + "&grant_type=authorization_code";

        return restClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new BizException(ErrorCodeImpl.OAUTH_PROVIDER_ERROR);
                })
                .body(GoogleTokenResponse.class);
    }

    private GoogleUserInfoResponse fetchUserInfo(String accessToken) {
        return restClient.get()
                .uri(userInfoUri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new BizException(ErrorCodeImpl.OAUTH_PROVIDER_ERROR);
                })
                .body(GoogleUserInfoResponse.class);
    }

    private record GoogleTokenResponse(
            @JsonProperty("access_token") String accessToken
    ) {}

    private record GoogleUserInfoResponse(
            String sub,
            String email,
            String name,
            String picture
    ) {}
}

