package com.ssafy.culturepick.auth.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SendCodeRequest {

    private String email;
}
