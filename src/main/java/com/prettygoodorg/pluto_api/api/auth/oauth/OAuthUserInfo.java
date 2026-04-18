package com.prettygoodorg.pluto_api.api.auth.oauth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OAuthUserInfo {
    String providerId;
    String email;
    String nickname;
    String profileImg;
    String provider;
}
