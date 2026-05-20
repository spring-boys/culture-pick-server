package com.ssafy.culturepick.auth.oauth2;

import com.ssafy.culturepick.auth.jwt.TokenProvider;
import com.ssafy.culturepick.auth.security.CustomMemberDetails;
import com.ssafy.culturepick.auth.service.RefreshTokenService;
import com.ssafy.culturepick.global.util.CookieUtil;
import com.ssafy.culturepick.member.domain.Member;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.ssafy.culturepick.auth.jwt.JwtConstants.*;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Value("${oauth2.redirect-uri}")
    private String redirectUri;

    @Value("${cookie.secure}")
    private boolean cookieSecure;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        CustomMemberDetails memberDetails = (CustomMemberDetails) authentication.getPrincipal();
        Member member = memberDetails.getMember();

        String accessToken = tokenProvider.generateToken(member, ACCESS_TOKEN_DURATION);
        String refreshToken = tokenProvider.generateToken(member, REFRESH_TOKEN_DURATION);

        refreshTokenService.save(member.getId(), refreshToken);

        int cookieMaxAge = (int) REFRESH_TOKEN_DURATION.toSeconds();
        CookieUtil.addCookie(response, REFRESH_TOKEN_COOKIE_NAME, refreshToken, cookieMaxAge, cookieSecure);

        getRedirectStrategy().sendRedirect(request, response, redirectUri + "?token=" + accessToken);
    }
}
