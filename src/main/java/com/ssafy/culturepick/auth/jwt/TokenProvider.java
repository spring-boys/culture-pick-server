package com.ssafy.culturepick.auth.jwt;

import com.ssafy.culturepick.auth.security.CustomMemberDetails;
import com.ssafy.culturepick.global.exception.code.AuthErrorCode;
import com.ssafy.culturepick.global.exception.type.BusinessException;
import com.ssafy.culturepick.member.domain.Member;
import com.ssafy.culturepick.member.domain.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;

@Slf4j
@Component
public class TokenProvider {

    private final SecretKey key;

    public TokenProvider(@Value("${jwt.secret-key}") String secretKey) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    public String generateToken(Member member, Duration expiredAt) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiredAt.toMillis());

        return Jwts.builder()
                .setIssuedAt(now)
                .setExpiration(expiry)
                .setSubject(String.valueOf(member.getId()))
                .claim("role", member.getRole().name())
                .signWith(key)
                .compact();
    }

    public void validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
        } catch (ExpiredJwtException e) {
            log.error("만료된 토큰: {}", e.getMessage());
            throw new BusinessException(AuthErrorCode.EXPIRED_TOKEN);
        } catch (JwtException e) {
            log.error("유효하지 않은 토큰: {}", e.getMessage());
            throw new BusinessException(AuthErrorCode.INVALID_TOKEN);
        }
    }

    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);
        String role = claims.get("role", String.class);

        Member member = Member.createMemberByToken(Long.valueOf(claims.getSubject()), Role.valueOf(role));

        CustomMemberDetails memberDetails = new CustomMemberDetails(member);
        return new UsernamePasswordAuthenticationToken(memberDetails, null, memberDetails.getAuthorities());
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}