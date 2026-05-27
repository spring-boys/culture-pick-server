package com.ssafy.culturepick.auth;

import com.ssafy.culturepick.auth.repository.EmailVerificationRepository;
import com.ssafy.culturepick.member.domain.Member;
import com.ssafy.culturepick.member.domain.Provider;
import com.ssafy.culturepick.member.domain.Role;
import com.ssafy.culturepick.member.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest //회원가입용 통합 테스트
@AutoConfigureMockMvc
@ActiveProfiles("test") // 실제 실행 시 application.yaml 대신 application-test.yaml 적용
class AuthSignupIntegrationTest {

    @Autowired
    private MockMvc mockMvc; //실제 서버를 띄우지 않고 HTTP 요청처럼 테스트

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void tearDown() {
        memberRepository.deleteAll();
        emailVerificationRepository.deleteVerified("signup-success@example.com");
        emailVerificationRepository.deleteVerified("not-verified@example.com");
    }

    @Test //정상 회원가입 성공 케이스
    @DisplayName("이메일 인증이 완료된 사용자는 회원가입에 성공하고, 비밀번호는 암호화되어 저장된다")
    void signup_success_whenEmailVerified() throws Exception {
        // given
        String email = "signup-success@example.com";
        String rawPassword = "password123";
        String nickname = "테스터";
        emailVerificationRepository.saveVerified(email); //이메일은 이미 인증 완료된 상태를 미리 만들어 둠

        String requestBody = """
                {
                  "email": "%s",
                  "password": "%s",
                  "nickname": "%s"
                }
                """.formatted(email, rawPassword, nickname);

        // when
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated()); //응답 상태가 201인지 확인하기

        // then
        Member savedMember = memberRepository.findByEmail(email).orElseThrow();
        assertThat(savedMember.getEmail()).isEqualTo(email);
        assertThat(savedMember.getNickname()).isEqualTo(nickname);
        assertThat(savedMember.getRole()).isEqualTo(Role.ROLE_MEMBER);
        assertThat(savedMember.getProvider()).isEqualTo(Provider.LOCAL);
        assertThat(savedMember.getPassword()).isNotEqualTo(rawPassword); //평문을 저장하지 않았는지 검증
        assertThat(passwordEncoder.matches(rawPassword, savedMember.getPassword())).isTrue(); //입력한 비민번호와 암호화된 비밀번호 매칭되는가?
        assertThat(emailVerificationRepository.isVerified(email)).isFalse(); // 이메일 인증상태 정리 여부 확인
    }

    @Test
    @DisplayName("이메일 인증이 완료되지 않은 사용자는 회원가입에 실패하고, 회원 정보가 저장되지 않는다")
    void signup_fail_whenEmailNotVerified() throws Exception {
        // given
        String email = "not-verified@example.com";
        String requestBody = """
                {
                  "email": "%s",
                  "password": "password123",
                  "nickname": "미인증"
                }
                """.formatted(email);

        // when & then
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        Optional<Member> savedMember = memberRepository.findByEmail(email);
        assertThat(savedMember).isEmpty();
    }
}
