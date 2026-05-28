package com.ssafy.culturepick.auth;

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

import static com.ssafy.culturepick.auth.jwt.JwtConstants.REFRESH_TOKEN_COOKIE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc // MockMvc를 자동 설정해서 실제 서버를 띄우지 않고 HTTP 요청 방식으로 API 테스트
@ActiveProfiles("test")
class AuthLoginIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long savedMemberId;

    @AfterEach
    void tearDown() {
        if (savedMemberId != null) {// 로그인 성공 시 Redis에 refreshToken이 저장될 수 있으므로 memberId 기준으로 삭제한다.
            refreshTokenRepository.deleteByMemberId(savedMemberId);
        }
        memberRepository.deleteAll();
        savedMemberId = null;
    }

    @Test
    @DisplayName("올바른 이메일과 비밀번호로 로그인하면 accessToken과 refreshToken이 발급되고, refreshToken이 저장된다")
    void login_success_whenEmailAndPasswordAreValid() throws Exception {
        // given
        String email = "login-success@example.com";
        String rawPassword = "password123";
        String nickname = "로그인회원";

        Member member = Member.createLocalMember(
                email,
                passwordEncoder.encode(rawPassword),
                nickname
        );
        savedMemberId = memberRepository.save(member).getId();

        String requestBody = """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, rawPassword);

        // when & then
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists()) // 응답 body에 accessToken이 존재하는지 검증한다.
                .andExpect(cookie().exists(REFRESH_TOKEN_COOKIE_NAME)) // refreshToken이 쿠키로 발급되었는지 검증한다.
                .andReturn();

        Cookie refreshTokenCookie = result.getResponse().getCookie(REFRESH_TOKEN_COOKIE_NAME);
        assertThat(refreshTokenCookie).isNotNull();
        assertThat(refreshTokenCookie.getValue()).isNotBlank();

        String refreshToken = refreshTokenCookie.getValue();
        assertThat(refreshTokenRepository.findMemberIdByRefreshToken(refreshToken))
                .contains(savedMemberId); // 발급된 refreshToken이 저장소에 실제로 저장되었는지 검증한다.
    }

    @Test
    @DisplayName("비밀번호가 틀리면 로그인에 실패하고, 토큰이 발급되지 않는다")
    void login_fail_whenPasswordIsInvalid() throws Exception {
        // given
        String email = "login-fail@example.com";
        String rawPassword = "password123";

        Member member = Member.createLocalMember(
                email,
                passwordEncoder.encode(rawPassword),
                "로그인실패회원"
        );
        savedMemberId = memberRepository.save(member).getId();

        String requestBody = """
                {
                  "email": "%s",
                  "password": "wrong-password"
                }
                """.formatted(email);// 틀린 비밀번호로 로그인 요청을 보낸다.

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized()) // 인증 실패이므로 401 Unauthorized 응답을 기대한다.
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andExpect(cookie().doesNotExist(REFRESH_TOKEN_COOKIE_NAME));
    }
}
