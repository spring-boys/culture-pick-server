package com.ssafy.culturepick.auth.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class VerifyCodeRequest {

    private String email;
    private String code;
}
