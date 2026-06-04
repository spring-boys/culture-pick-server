package com.ssafy.culturepick.member.dto.response;

import com.ssafy.culturepick.member.domain.Member;
import lombok.Getter;

@Getter
public class MyPageResponse {

    private Long memberId;
    private String email;
    private String nickname;

    private MyPageResponse(Long memberId, String email, String nickname) {
        this.memberId = memberId;
        this.email = email;
        this.nickname = nickname;
    }

    public static MyPageResponse from(Member member) {
        return new MyPageResponse(
                member.getId(),
                member.getEmail(),
                member.getNickname()
        );
    }
}
