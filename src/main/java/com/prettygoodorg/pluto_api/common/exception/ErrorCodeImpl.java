package com.prettygoodorg.pluto_api.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCodeImpl implements ErrorCode {
    // Auth
    INVALID_ACCESS_TOKEN("AUTH_000", "유효하지 않은 액세스 토큰입니다.", HttpStatus.UNAUTHORIZED),
    INVALID_REFRESH_TOKEN("AUTH_001", "유효하지 않은 리프레시 토큰입니다.", HttpStatus.UNAUTHORIZED),
    PENDING_USER_NOT_FOUND("AUTH_002", "임시 사용자 정보를 찾을 수 없습니다. 다시 로그인해 주세요.", HttpStatus.BAD_REQUEST),
    OAUTH_PROVIDER_ERROR("AUTH_003", "OAuth 제공자와 통신 중 오류가 발생했습니다.", HttpStatus.BAD_GATEWAY),
    UNSUPPORTED_PROVIDER("AUTH_004", "지원하지 않는 OAuth 제공자입니다.", HttpStatus.BAD_REQUEST),
    // User
    USER_NOT_FOUND("USER_001", "사용자 정보를 찾을 수 없습니다.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
