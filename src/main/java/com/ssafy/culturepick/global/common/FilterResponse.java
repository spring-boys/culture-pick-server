package com.ssafy.culturepick.global.common;

import tools.jackson.databind.ObjectMapper;
import com.ssafy.culturepick.global.exception.code.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class FilterResponse {

    public static void fail(HttpServletResponse response, ErrorCode errorCode, ObjectMapper objectMapper) throws IOException {
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(ErrorResponse.of(errorCode)));
    }
}