package com.ssafy.culturepick.auth.jwt;

import java.time.Duration;

public class JwtConstants {

    public static final Duration ACCESS_TOKEN_DURATION = Duration.ofHours(2);
    public static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(14);
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
}
