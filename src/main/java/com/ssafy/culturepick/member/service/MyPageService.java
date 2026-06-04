package com.ssafy.culturepick.member.service;

import com.ssafy.culturepick.global.exception.code.MemberErrorCode;
import com.ssafy.culturepick.global.exception.type.BusinessException;
import com.ssafy.culturepick.member.domain.Member;
import com.ssafy.culturepick.member.dto.response.MyPageResponse;
import com.ssafy.culturepick.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private final MemberRepository memberRepository;

    public MyPageResponse getMyPage(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        return MyPageResponse.from(member);
    }
}
