package com.ssafy.culturepick.global.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.culturepick.global.exception.code.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class FilterResponse {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void fail(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(ErrorResponse.of(errorCode)));
    }
}