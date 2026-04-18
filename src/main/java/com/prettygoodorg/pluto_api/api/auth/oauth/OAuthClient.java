package com.prettygoodorg.pluto_api.api.auth.oauth;

public interface OAuthClient {

    String getProvider();

    OAuthUserInfo getUserInfo(String authorizationCode, String redirectUri);

}
