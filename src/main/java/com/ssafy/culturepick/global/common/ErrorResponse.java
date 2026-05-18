package com.ssafy.culturepick.global.common;

import com.ssafy.culturepick.global.exception.code.ErrorCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ErrorResponse {

    private String code;
    private String message;
    private String field;

    private ErrorResponse(String code, String message, String field) {
        this.code = code;
        this.message = message;
        this.field = field;
    }

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), null);
    }

    public static ErrorResponse of(ErrorCode errorCode, String field) {
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), field);
    }

    public static ErrorResponse of(ErrorCode errorCode, String message, String field) {
        return new ErrorResponse(errorCode.getCode(), message, field);
    }
}
