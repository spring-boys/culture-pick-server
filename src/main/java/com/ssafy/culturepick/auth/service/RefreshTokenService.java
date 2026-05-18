package com.ssafy.culturepick.auth.service;

import com.ssafy.culturepick.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public void save(Long memberId, String refreshToken) {

    }
}
