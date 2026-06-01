package com.ssafy.culturepick.bookmark.service;

import com.ssafy.culturepick.bookmark.domain.Bookmark;
import com.ssafy.culturepick.bookmark.repository.BookmarkRepository;
import com.ssafy.culturepick.culture.domain.Culture;
import com.ssafy.culturepick.culture.repository.CultureRepository;
import com.ssafy.culturepick.global.exception.code.BookmarkErrorCode;
import com.ssafy.culturepick.global.exception.code.CultureErrorCode;
import com.ssafy.culturepick.global.exception.code.MemberErrorCode;
import com.ssafy.culturepick.global.exception.type.BusinessException;
import com.ssafy.culturepick.member.domain.Member;
import com.ssafy.culturepick.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final CultureRepository cultureRepository;
    private final MemberRepository memberRepository;

    public void addBookmark(Long memberId, Long cultureId) {
        if (bookmarkRepository.existsByMemberIdAndCultureId(memberId, cultureId)) {
            throw new BusinessException(BookmarkErrorCode.ALREADY_BOOKMARKED);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));
        Culture culture = cultureRepository.findById(cultureId)
                .orElseThrow(() -> new BusinessException(CultureErrorCode.CULTURE_NOT_FOUND));

        bookmarkRepository.save(Bookmark.createBookmark(member, culture));
        culture.incrementBookmarkCount();
    }

    public void removeBookmark(Long memberId, Long cultureId) {
        Bookmark bookmark = bookmarkRepository.findByMemberIdAndCultureId(memberId, cultureId)
                .orElseThrow(() -> new BusinessException(BookmarkErrorCode.BOOKMARK_NOT_FOUND));

        bookmarkRepository.delete(bookmark);
        bookmark.getCulture().decrementBookmarkCount();
    }
}
