package com.ssafy.culturepick.global.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CultureErrorCode implements ErrorCode {

    UNSUPPORTED_CATEGORY(HttpStatus.INTERNAL_SERVER_ERROR, "CULTURE_001", "지원하지 않는 카테고리입니다."),
    CULTURE_NOT_FOUND(HttpStatus.NOT_FOUND, "CULTURE_002", "문화 행사를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
