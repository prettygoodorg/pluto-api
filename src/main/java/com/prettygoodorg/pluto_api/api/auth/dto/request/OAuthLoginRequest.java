package com.prettygoodorg.pluto_api.api.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OAuthLoginRequest {
    @NotBlank(message = "provider는 필수입니다.")
    String provider;

    @NotBlank(message = "code는 필수입니다.")
    String code;

    @NotBlank(message = "redirectUri는 필수입니다.")
    String redirectUri;
}
