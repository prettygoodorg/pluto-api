package com.prettygoodorg.pluto_api.api.auth.controller;

import com.prettygoodorg.pluto_api.api.auth.dto.request.LogoutRequest;
import com.prettygoodorg.pluto_api.api.auth.dto.request.OAuthLoginRequest;
import com.prettygoodorg.pluto_api.api.auth.dto.request.TermsAgreeRequest;
import com.prettygoodorg.pluto_api.api.auth.dto.request.TokenRefreshRequest;
import com.prettygoodorg.pluto_api.api.auth.dto.response.OAuthLoginResponse;
import com.prettygoodorg.pluto_api.api.auth.dto.response.TokenResponse;
import com.prettygoodorg.pluto_api.api.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("oauth/login")
    public OAuthLoginResponse oauthLogin(@RequestBody @Valid OAuthLoginRequest request) {
        return authService.oauthLogin(request);
    }

    @PostMapping("terms/agree")
    public TokenResponse agreeTerms(@RequestBody @Valid TermsAgreeRequest request) {
        return authService.agreeTerms(request);
    }

    @PostMapping("token/refresh")
    public TokenResponse refreshToken(@RequestBody @Valid TokenRefreshRequest request) {
        return authService.refreshToken(request);
    }

    @PostMapping("logout")
    public void logout(@RequestBody @Valid LogoutRequest request) {
        authService.logout(request);
    }
}
