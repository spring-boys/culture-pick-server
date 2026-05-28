package com.ssafy.culturepick.auth.service;

import com.ssafy.culturepick.auth.dto.RegenerateToken;
import com.ssafy.culturepick.auth.dto.request.SignupRequest;
import com.ssafy.culturepick.auth.jwt.TokenProvider;
import com.ssafy.culturepick.auth.repository.EmailVerificationRepository;
import com.ssafy.culturepick.auth.repository.RefreshTokenRepository;
import com.ssafy.culturepick.global.exception.code.AuthErrorCode;
import com.ssafy.culturepick.global.exception.code.MemberErrorCode;
import com.ssafy.culturepick.global.exception.type.BusinessException;
import com.ssafy.culturepick.member.domain.Member;
import com.ssafy.culturepick.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.ssafy.culturepick.auth.jwt.JwtConstants.ACCESS_TOKEN_DURATION;
import static com.ssafy.culturepick.auth.jwt.JwtConstants.REFRESH_TOKEN_DURATION;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationRepository emailVerificationRepository;

    public void signup(SignupRequest request) {
        if (!emailVerificationRepository.isVerified(request.getEmail())) {
            throw new BusinessException(AuthErrorCode.EMAIL_NOT_VERIFIED);
        }

        if (memberRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException(MemberErrorCode.DUPLICATE_EMAIL);
        }

        Member member = Member.createLocalMember(
                request.getEmail(), passwordEncoder.encode(request.getPassword()), request.getNickname());
        memberRepository.save(member);

        emailVerificationRepository.deleteVerified(request.getEmail());
    }

    public RegenerateToken regenerate(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BusinessException(AuthErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        tokenProvider.validateToken(refreshToken);

        Long memberId = refreshTokenRepository.findMemberIdByRefreshToken(refreshToken)
                .orElseThrow(() -> new BusinessException(AuthErrorCode.REFRESH_TOKEN_NOT_FOUND));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        String newAccessToken = tokenProvider.generateToken(member, ACCESS_TOKEN_DURATION);
        String newRefreshToken = tokenProvider.generateToken(member, REFRESH_TOKEN_DURATION);

        refreshTokenRepository.save(memberId, newRefreshToken);

        return RegenerateToken.of(newAccessToken, newRefreshToken);
    }

    public void logout(String refreshToken) {
        if (refreshToken != null) {
            refreshTokenRepository.deleteByRefreshToken(refreshToken);
        }
    }
}
