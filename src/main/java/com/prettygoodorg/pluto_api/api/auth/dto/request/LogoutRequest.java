package com.prettygoodorg.pluto_api.api.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LogoutRequest {
    @NotBlank(message = "refreshToken은 필수입니다.")
    String refreshToken;
}
