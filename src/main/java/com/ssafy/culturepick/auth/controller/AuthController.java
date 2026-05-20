package com.ssafy.culturepick.auth.controller;

import com.ssafy.culturepick.auth.dto.RegenerateToken;
import com.ssafy.culturepick.auth.dto.request.SendCodeRequest;
import com.ssafy.culturepick.auth.dto.request.SignupRequest;
import com.ssafy.culturepick.auth.dto.request.VerifyCodeRequest;
import com.ssafy.culturepick.auth.dto.response.RegenerateAccessTokenResponse;
import com.ssafy.culturepick.auth.service.AuthService;
import com.ssafy.culturepick.auth.service.EmailVerificationService;
import com.ssafy.culturepick.global.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.ssafy.culturepick.auth.jwt.JwtConstants.REFRESH_TOKEN_COOKIE_NAME;
import static com.ssafy.culturepick.auth.jwt.JwtConstants.REFRESH_TOKEN_DURATION;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;

    @Value("${cookie.secure}")
    private boolean cookieSecure;

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/regenerate")
    public ResponseEntity<RegenerateAccessTokenResponse> regenerate(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = CookieUtil.getCookieValue(request, REFRESH_TOKEN_COOKIE_NAME);
        RegenerateToken regenerateToken = authService.regenerate(refreshToken);

        CookieUtil.addCookie(response, REFRESH_TOKEN_COOKIE_NAME, regenerateToken.getRefreshToken(), (int) REFRESH_TOKEN_DURATION.toSeconds(), cookieSecure);

        return ResponseEntity.ok(RegenerateAccessTokenResponse.of(regenerateToken.getAccessToken()));
    }

    @DeleteMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = CookieUtil.getCookieValue(request, REFRESH_TOKEN_COOKIE_NAME);
        authService.logout(refreshToken);

        CookieUtil.deleteCookie(request, response, REFRESH_TOKEN_COOKIE_NAME);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/email/code")
    public ResponseEntity<Void> sendCode(@Valid @RequestBody SendCodeRequest request) {
        emailVerificationService.sendCode(request.getEmail());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/email/verification")
    public ResponseEntity<Void> verifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        emailVerificationService.verifyCode(request.getEmail(), request.getCode());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/email/verification")
    public ResponseEntity<Void> deleteVerified(@RequestParam String email) {
        emailVerificationService.deleteVerified(email);
        return ResponseEntity.noContent().build();
    }
}
