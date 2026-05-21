package com.ssafy.culturepick.global.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    INVALID_USERNAME_PASSWORD(HttpStatus.UNAUTHORIZED, "AUTH_001", "아이디 또는 비밀번호가 올바르지 않습니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_002", "토큰이 만료되었습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_003", "유효하지 않은 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH_004", "리프레시 토큰이 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_005", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH_006", "접근 권한이 없습니다."),
    EXPIRED_EMAIL_CODE(HttpStatus.BAD_REQUEST, "AUTH_007", "이메일 인증 코드가 만료되었습니다."),
    MISMATCHED_EMAIL_CODE(HttpStatus.BAD_REQUEST, "AUTH_008", "이메일 인증 코드가 일치하지 않습니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "AUTH_009", "이메일 인증이 필요합니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
