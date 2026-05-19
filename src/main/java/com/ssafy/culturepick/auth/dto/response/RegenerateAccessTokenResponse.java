package com.ssafy.culturepick.auth.dto.response;

import lombok.Getter;

@Getter
public class RegenerateAccessTokenResponse {

    private String accessToken;

    private RegenerateAccessTokenResponse(String accessToken) {
        this.accessToken = accessToken;
    }

    public static RegenerateAccessTokenResponse of(String accessToken) {
        return new RegenerateAccessTokenResponse(accessToken);
    }
}
