package com.ssafy.culturepick.auth.oauth2;

import com.ssafy.culturepick.global.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

@Component
public class Oauth2AuthorizationRequestCookieRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    @Value("${cookie.secure}")
    private boolean cookieSecure;

    private static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE = "oauth2_auth_request";
    private static final int COOKIE_EXPIRE_SECONDS = 180;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        String value = CookieUtil.getCookieValue(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE);
        if (value == null) return null;

        return CookieUtil.deserialize(value, OAuth2AuthorizationRequest.class);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) {
            CookieUtil.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE);
            return;
        }

        CookieUtil.addCookie(response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE, CookieUtil.serialize(authorizationRequest), COOKIE_EXPIRE_SECONDS, cookieSecure);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        OAuth2AuthorizationRequest authorizationRequest = loadAuthorizationRequest(request);
        CookieUtil.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE);
        return authorizationRequest;
    }
}
