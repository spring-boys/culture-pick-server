package com.ssafy.culturepick.global.exception.code;

import org.springframework.http.HttpStatus;

public interface ErrorCode {

    HttpStatus getHttpStatus();
    String getCode();
    String getMessage();
}