package com.prettygoodorg.pluto_api.api.auth.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.prettygoodorg.pluto_api.common.exception.ErrorCodeImpl;
import com.prettygoodorg.pluto_api.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class KakaoOAuthClient implements OAuthClient {

    @Value("${oauth.kakao.client-id}")
    private String clientId;

    @Value("${oauth.kakao.client-secret}")
    private String clientSecret;

    @Value("${oauth.kakao.token-uri}")
    private String tokenUri;

    @Value("${oauth.kakao.user-info-uri}")
    private String userInfoUri;

    private final RestClient restClient;

    @Override
    public String getProvider() {
        return "KAKAO";
    }

    @Override
    public OAuthUserInfo getUserInfo(String authorizationCode, String redirectUri) {
        KakaoTokenResponse tokenResponse = exchangeCode(authorizationCode, redirectUri);
        KakaoUserInfoResponse userInfo = fetchUserInfo(tokenResponse.accessToken());

        String nickname = Optional.ofNullable(userInfo.kakaoAccount())
                .map(KakaoAccount::profile)
                .map(KakaoProfile::nickname)
                .orElse("");

        String profileImg = Optional.ofNullable(userInfo.kakaoAccount())
                .map(KakaoAccount::profile)
                .map(KakaoProfile::profileImageUrl)
                .orElse("");

        String email = Optional.ofNullable(userInfo.kakaoAccount())
                .map(KakaoAccount::email)
                .orElse("");

        return new OAuthUserInfo(
                String.valueOf(userInfo.id()),
                email,
                nickname,
                profileImg,
                getProvider()
        );
    }

    private KakaoTokenResponse exchangeCode(String code, String redirectUri) {
        String body = "grant_type=authorization_code"
                + "&client_id=" + clientId
                + "&client_secret=" + clientSecret
                + "&redirect_uri=" + redirectUri
                + "&code=" + code;

        return restClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    // StreamUtils를 이용해 바디를 byte[] -> String으로 변환
                    String errorBody = StreamUtils.copyToString(res.getBody(), StandardCharsets.UTF_8);

                    log.error("OAuth Provider Error: status={}, body={}", res.getStatusCode(), errorBody);
                    throw new BizException(ErrorCodeImpl.OAUTH_PROVIDER_ERROR);
                })
                .body(KakaoTokenResponse.class);
    }

    private KakaoUserInfoResponse fetchUserInfo(String accessToken) {
        return restClient.get()
                .uri(userInfoUri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new BizException(ErrorCodeImpl.OAUTH_PROVIDER_ERROR);
                })
                .body(KakaoUserInfoResponse.class);
    }

    private record KakaoTokenResponse(
            @JsonProperty("access_token") String accessToken
    ) {}

    private record KakaoUserInfoResponse(
            Long id,
            @JsonProperty("kakao_account") KakaoAccount kakaoAccount
    ) {}

    private record KakaoAccount(
            String email,
            KakaoProfile profile
    ) {}

    private record KakaoProfile(
            String nickname,
            @JsonProperty("profile_image_url") String profileImageUrl
    ) {}
}

