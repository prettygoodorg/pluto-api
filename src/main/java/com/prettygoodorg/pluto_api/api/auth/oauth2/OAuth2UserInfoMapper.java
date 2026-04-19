package com.prettygoodorg.pluto_api.api.auth.oauth2;

import com.prettygoodorg.pluto_api.common.exception.BizException;
import com.prettygoodorg.pluto_api.common.exception.ErrorCodeImpl;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class OAuth2UserInfoMapper {

    public OAuthUserInfo map(String registrationId, Map<String, Object> attributes) {
        String provider = registrationId.toUpperCase();
        return switch (provider) {
            case "GOOGLE" -> mapGoogle(attributes);
            case "KAKAO" -> mapKakao(attributes);
            default -> throw new BizException(ErrorCodeImpl.UNSUPPORTED_PROVIDER);
        };
    }

    private OAuthUserInfo mapGoogle(Map<String, Object> attributes) {
        return new OAuthUserInfo(
                getString(attributes, "sub"),
                getString(attributes, "email"),
                getString(attributes, "name"),
                getString(attributes, "picture"),
                "GOOGLE"
        );
    }

    private OAuthUserInfo mapKakao(Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = getMap(attributes, "kakao_account");
        Map<String, Object> profile = getMap(kakaoAccount, "profile");

        return new OAuthUserInfo(
                getString(attributes, "id"),
                getString(kakaoAccount, "email"),
                getString(profile, "nickname"),
                getString(profile, "profile_image_url"),
                "KAKAO"
        );
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getMap(Map<String, Object> attributes, String key) {
        Object value = attributes.get(key);
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }

    private String getString(Map<String, Object> attributes, String key) {
        return Optional.ofNullable(attributes.get(key))
                .map(String::valueOf)
                .orElse("");
    }
}
