package com.ssafy.culturepick.auth;

import com.ssafy.culturepick.auth.jwt.TokenProvider;
import com.ssafy.culturepick.auth.repository.RefreshTokenRepository;
import com.ssafy.culturepick.member.domain.Member;
import com.ssafy.culturepick.member.repository.MemberRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Duration;

import static com.ssafy.culturepick.auth.jwt.JwtConstants.REFRESH_TOKEN_COOKIE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest //토큰 재발급 통합 테스트
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthTokenReissueIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenProvider tokenProvider;

    private Long savedMemberId;

    @AfterEach // 각 테스트가 끝난 뒤 Redis와 DB에 남은 테스트 데이터를 정리한다.
    void tearDown() {
        if (savedMemberId != null) {
            refreshTokenRepository.deleteByMemberId(savedMemberId);
        }
        memberRepository.deleteAll();
        savedMemberId = null;
    }

    @Test
    @DisplayName("유효한 refreshToken으로 토큰을 재발급하면 accessToken과 새로운 refreshToken이 발급되고, 기존 refreshToken은 무효화된다")
    void regenerate_success_whenRefreshTokenIsValid_thenRotateRefreshToken() throws Exception {
        // given
        String email = "reissue-success@example.com";
        String rawPassword = "password123";

        Member member = Member.createLocalMember(
                email,
                passwordEncoder.encode(rawPassword),
                "재발급회원"
        );
        savedMemberId = memberRepository.save(member).getId(); //테스트 회원 생성

        String oldRefreshToken = tokenProvider.generateToken(member, Duration.ofDays(13));
        refreshTokenRepository.save(savedMemberId, oldRefreshToken);

        Cookie oldRefreshTokenCookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, oldRefreshToken); // 재발급 요청에 사용할 기존 refreshToken을 직접 생성한다.

        // when & then
        MvcResult result = mockMvc.perform(post("/api/v1/auth/regenerate")
                        .cookie(oldRefreshTokenCookie)) // 기존 refreshToken 쿠키를 담아 재발급 API를 호출한다.
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists()) // 응답 body에 새 accessToken이 존재하는지 검증한다.
                .andExpect(cookie().exists(REFRESH_TOKEN_COOKIE_NAME)) // 응답 쿠키에 새 refreshToken이 내려오는지 검증한다.
                .andReturn();

        Cookie newRefreshTokenCookie = result.getResponse().getCookie(REFRESH_TOKEN_COOKIE_NAME);
        assertThat(newRefreshTokenCookie).isNotNull();
        assertThat(newRefreshTokenCookie.getValue()).isNotBlank();

        String newRefreshToken = newRefreshTokenCookie.getValue();
        assertThat(newRefreshToken).isNotEqualTo(oldRefreshToken); // 재발급 후 refreshToken이 기존 값과 다른 새 값으로 교체되었는지 확인한다.

        assertThat(refreshTokenRepository.findMemberIdByRefreshToken(oldRefreshToken)).isEmpty(); // 기존 refreshToken이 저장소에서 더 이상 조회되지 않는지 확인한다.
        assertThat(refreshTokenRepository.findMemberIdByRefreshToken(newRefreshToken)) // 새 refreshToken이 저장소에 저장되었는지 확인한다.
                .contains(savedMemberId);
    }

    @Test
    @DisplayName("refreshToken 쿠키 값이 비어 있으면 토큰 재발급에 실패한다")
    void regenerate_fail_whenRefreshTokenCookieIsBlank() throws Exception {
        // given
        Cookie blankRefreshTokenCookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, "");

        // when & then
        mockMvc.perform(post("/api/v1/auth/regenerate")
                        .cookie(blankRefreshTokenCookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andExpect(cookie().doesNotExist(REFRESH_TOKEN_COOKIE_NAME));
    }

}
