package com.ssafy.culturepick.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.culturepick.auth.dto.LoginRequest;
import com.ssafy.culturepick.auth.jwt.TokenProvider;
import com.ssafy.culturepick.auth.security.CustomMemberDetails;
import com.ssafy.culturepick.auth.service.RefreshTokenService;
import com.ssafy.culturepick.global.common.FilterResponse;
import com.ssafy.culturepick.global.exception.code.AuthErrorCode;
import com.ssafy.culturepick.global.util.CookieUtil;
import com.ssafy.culturepick.member.domain.Member;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Map;

import static com.ssafy.culturepick.auth.jwt.JwtConstants.*;

@Slf4j
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final ObjectMapper objectMapper;

    public LoginFilter(AuthenticationManager authenticationManager, TokenProvider tokenProvider, RefreshTokenService refreshTokenService, ObjectMapper objectMapper) {
        setFilterProcessesUrl("/api/v1/auth/login");
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.refreshTokenService = refreshTokenService;
        this.objectMapper = objectMapper;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            LoginRequest loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);

            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());
            return authenticationManager.authenticate(token);

        } catch (IOException e) {
            throw new AuthenticationServiceException("로그인 요청 파싱 실패", e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
        CustomMemberDetails memberDetails = (CustomMemberDetails) authentication.getPrincipal();
        Member member = memberDetails.getMember();

        String refreshToken = tokenProvider.generateToken(member, REFRESH_TOKEN_DURATION);
        refreshTokenService.save(member.getId(), refreshToken);
        addRefreshTokenToCookie(response, refreshToken);

        String accessToken = tokenProvider.generateToken(member, ACCESS_TOKEN_DURATION);
        addAccessTokenToBody(response, accessToken);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        log.warn("로그인 실패: {}", failed.getMessage());
        FilterResponse.fail(response, AuthErrorCode.INVALID_USERNAME_PASSWORD);
    }

    private void addAccessTokenToBody(HttpServletResponse res, String accessToken) throws IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write(objectMapper.writeValueAsString(Map.of("accessToken", accessToken)));
    }

    private void addRefreshTokenToCookie(HttpServletResponse response, String refreshToken) {
        int cookieMaxAge = (int) REFRESH_TOKEN_DURATION.toSeconds();
        CookieUtil.addCookie(response, REFRESH_TOKEN_COOKIE_NAME, refreshToken, cookieMaxAge);
    }
}
