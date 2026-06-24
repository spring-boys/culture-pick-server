package com.ssafy.culturepick.auth;

import com.ssafy.culturepick.auth.jwt.TokenProvider;
import com.ssafy.culturepick.auth.repository.RefreshTokenRepository;
import com.ssafy.culturepick.member.domain.Member;
import com.ssafy.culturepick.member.repository.MemberRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.ssafy.culturepick.config.RedisTestContainerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Duration;

import static com.ssafy.culturepick.auth.jwt.JwtConstants.REFRESH_TOKEN_COOKIE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest // Spring Context를 실제로 띄워 로그아웃 API 흐름을 검증하는 통합 테스트
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(RedisTestContainerConfig.class)
class AuthLogoutIntegrationTest {

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

    @AfterEach
    void tearDown() { // Spring Context를 실제로 띄워 로그아웃 API 흐름을 검증하는 통합 테스트
        if (savedMemberId != null) {
            refreshTokenRepository.deleteByMemberId(savedMemberId);
        }
        memberRepository.deleteAll();
        savedMemberId = null;
    }

    @Test
    @DisplayName("로그아웃하면 refreshToken이 저장소에서 삭제되고, refreshToken 쿠키가 만료된다")
    void logout_success_thenDeleteRefreshTokenAndExpireCookie() throws Exception {
        // given 테스트 회원을 저장하고, 해당 회원의 refreshToken을 생성해 저장소에 미리 저장한다.
        String refreshToken = saveMemberAndRefreshToken("logout-success@example.com");
        Cookie refreshTokenCookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken);

        // when
        MvcResult result = mockMvc.perform(delete("/api/v1/auth/logout")
                        .cookie(refreshTokenCookie))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge(REFRESH_TOKEN_COOKIE_NAME, 0))
                .andReturn();

        // then 로그아웃 후 기존 refreshToken이 저장소에서 삭제되었는지 확인한다.
        assertThat(refreshTokenRepository.findMemberIdByRefreshToken(refreshToken)).isEmpty();

        Cookie expiredCookie = result.getResponse().getCookie(REFRESH_TOKEN_COOKIE_NAME);
        assertThat(expiredCookie).isNotNull();
        assertThat(expiredCookie.getValue()).isEmpty(); // 쿠키가 존재하고, 값은 비어 있으며, maxAge가 0인지 확인한다.
        assertThat(expiredCookie.getMaxAge()).isZero();
    }

    @Test // 로그아웃으로 삭제된 refreshToken은 더 이상 재발급에 사용할 수 없어야 한다.
    @DisplayName("로그아웃 후 삭제된 refreshToken으로 재발급을 요청하면 실패한다")
    void regenerate_fail_afterLogoutWithDeletedRefreshToken() throws Exception {
        // given
        String refreshToken = saveMemberAndRefreshToken("logout-reissue-fail@example.com");
        Cookie refreshTokenCookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken);

        mockMvc.perform(delete("/api/v1/auth/logout")
                        .cookie(refreshTokenCookie))
                .andExpect(status().isNoContent());

        // when & then
        // 같은 refreshToken으로 재발급 요청을 보냈을 때 401 응답이 나오는지 검증한다.
        mockMvc.perform(post("/api/v1/auth/regenerate")
                        .cookie(new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken)))
                .andExpect(status().isUnauthorized());

        assertThat(refreshTokenRepository.findMemberIdByRefreshToken(refreshToken)).isEmpty();
    }

    private String saveMemberAndRefreshToken(String email) {
        Member member = Member.createLocalMember(
                email,
                passwordEncoder.encode("password123"),
                "로그아웃회원"
        );
        Member savedMember = memberRepository.save(member);
        savedMemberId = savedMember.getId();

        String refreshToken = tokenProvider.generateToken(savedMember, Duration.ofDays(13));
        refreshTokenRepository.save(savedMemberId, refreshToken);

        return refreshToken;
    }
}
