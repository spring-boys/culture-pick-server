package com.ssafy.culturepick.auth.dto;

import lombok.Getter;

@Getter
public class RegenerateToken {

    private String accessToken;
    private String refreshToken;

    private RegenerateToken(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public static RegenerateToken of(String accessToken, String refreshToken) {
        return new RegenerateToken(accessToken, refreshToken);
    }
}
