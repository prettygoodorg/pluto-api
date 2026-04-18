package com.prettygoodorg.pluto_api.api.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OAuthLoginResponse {
    boolean isNewUser;
    String tempKey;
    TokenResponse tokens;
}
