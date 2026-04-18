package com.prettygoodorg.pluto_api.api.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenResponse {
    String accessToken;
    String refreshToken;
    long accessTokenExpiresIn;
}
