package com.ssafy.culturepick.member.service;

import com.ssafy.culturepick.bookmark.domain.Bookmark;
import com.ssafy.culturepick.bookmark.repository.BookmarkRepository;
import com.ssafy.culturepick.global.common.PageResponse;
import com.ssafy.culturepick.global.exception.code.MemberErrorCode;
import com.ssafy.culturepick.global.exception.type.BusinessException;
import com.ssafy.culturepick.member.domain.Member;
import com.ssafy.culturepick.member.dto.request.UpdateMemberInfoRequest;
import com.ssafy.culturepick.member.dto.response.BookmarkedCultureResponse;
import com.ssafy.culturepick.member.dto.response.MyPageResponse;
import com.ssafy.culturepick.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private final MemberRepository memberRepository;
    private final BookmarkRepository bookmarkRepository;

    public MyPageResponse getMyPage(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        return MyPageResponse.from(member);
    }

    // TODO 닉네임 수정
    @Transactional
    public void updateInfo(Long memberId, UpdateMemberInfoRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        member.updateNickname(request.getNickname());
    }

    public PageResponse<BookmarkedCultureResponse> getBookmarkedCultures(Long memberId, int page) {
        PageRequest pageRequest = PageRequest.of(page, 6);
        Page<Bookmark> bookmarkPage = bookmarkRepository.findCulturesByMemberId(memberId, pageRequest);
        List<BookmarkedCultureResponse> content = bookmarkPage.getContent().stream()
                .map(b -> BookmarkedCultureResponse.of(b.getCulture()))
                .toList();
        return PageResponse.of(bookmarkPage, content);
    }
}
