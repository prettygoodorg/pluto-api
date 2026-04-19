package com.prettygoodorg.pluto_api.api.auth.oauth2;

import com.prettygoodorg.pluto_api.common.exception.BizException;
import com.prettygoodorg.pluto_api.common.exception.ErrorCode;
import com.prettygoodorg.pluto_api.common.exception.ErrorCodeImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class OAuth2FailureHandler implements AuthenticationFailureHandler {

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {
        ErrorCode errorCode = resolveErrorCode(exception);
        Map<String, String> params = new LinkedHashMap<>();
        params.put("status", "error");
        params.put("errorCode", errorCode.getCode());
        params.put("message", errorCode.getMessage());
        response.sendRedirect(redirectUri + "#" + toFragment(params));
    }

    private ErrorCode resolveErrorCode(AuthenticationException exception) {
        Throwable cause = exception.getCause();
        if (cause instanceof BizException bizException) {
            return bizException.getErrorCode();
        }
        return ErrorCodeImpl.OAUTH_PROVIDER_ERROR;
    }

    private String toFragment(Map<String, String> params) {
        return params.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }
}
