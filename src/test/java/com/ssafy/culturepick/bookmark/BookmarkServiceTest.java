package com.ssafy.culturepick.bookmark;

import com.ssafy.culturepick.bookmark.repository.BookmarkRepository;
import com.ssafy.culturepick.bookmark.service.BookmarkService;
import com.ssafy.culturepick.culture.domain.Culture;
import com.ssafy.culturepick.culture.domain.CultureCategory;
import com.ssafy.culturepick.culture.repository.CultureRepository;
import com.ssafy.culturepick.global.exception.code.BookmarkErrorCode;
import com.ssafy.culturepick.global.exception.type.BusinessException;
import com.ssafy.culturepick.member.domain.Member;
import com.ssafy.culturepick.member.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "culture.api.service-key=test")
@ActiveProfiles("test")
@Transactional
class BookmarkServiceTest {

    @Autowired
    private BookmarkService bookmarkService;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CultureRepository cultureRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("북마크 추가에 성공하면 Bookmark가 저장되고 문화행사의 북마크 수가 1 증가한다")
    void addBookmark_success() {
        // given
        Member member = saveMember("bookmark-add@example.com");
        Culture culture = saveCulture(1001L, "북마크 추가 행사");

        // when
        bookmarkService.addBookmark(member.getId(), culture.getId());
        flushAndClear();

        // then
        assertThat(bookmarkRepository.existsByMemberIdAndCultureId(member.getId(), culture.getId()))
                .isTrue();

        Culture foundCulture = cultureRepository.findById(culture.getId()).orElseThrow();
        assertThat(foundCulture.getBookmarkCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("이미 북마크한 문화행사를 다시 북마크하면 예외가 발생하고 중복 저장과 카운트 증가가 일어나지 않는다")
    void addBookmark_fail_whenAlreadyBookmarked() {
        // given
        Member member = saveMember("bookmark-duplicate@example.com");
        Culture culture = saveCulture(1002L, "중복 북마크 행사");
        bookmarkService.addBookmark(member.getId(), culture.getId());
        flushAndClear();

        // when & then
        assertThatThrownBy(() -> bookmarkService.addBookmark(member.getId(), culture.getId()))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(BookmarkErrorCode.ALREADY_BOOKMARKED));
        flushAndClear();

        assertThat(countBookmarks(member.getId(), culture.getId())).isEqualTo(1);

        Culture foundCulture = cultureRepository.findById(culture.getId()).orElseThrow();
        assertThat(foundCulture.getBookmarkCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("북마크 삭제에 성공하면 Bookmark가 삭제되고 문화행사의 북마크 수가 1 감소한다")
    void removeBookmark_success() {
        // given
        Member member = saveMember("bookmark-remove@example.com");
        Culture culture = saveCulture(1003L, "북마크 삭제 행사");
        bookmarkService.addBookmark(member.getId(), culture.getId());
        flushAndClear();

        // when
        bookmarkService.removeBookmark(member.getId(), culture.getId());
        flushAndClear();

        // then
        assertThat(bookmarkRepository.existsByMemberIdAndCultureId(member.getId(), culture.getId()))
                .isFalse();

        Culture foundCulture = cultureRepository.findById(culture.getId()).orElseThrow();
        assertThat(foundCulture.getBookmarkCount()).isZero();
    }

    @Test
    @DisplayName("북마크하지 않은 문화행사를 삭제하면 예외가 발생하고 문화행사의 북마크 수는 변하지 않는다")
    void removeBookmark_fail_whenBookmarkNotFound() {
        // given
        Member member = saveMember("bookmark-not-found@example.com");
        Culture culture = saveCulture(1004L, "미북마크 삭제 시도 행사");
        flushAndClear();

        // when & then
        assertThatThrownBy(() -> bookmarkService.removeBookmark(member.getId(), culture.getId()))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(BookmarkErrorCode.BOOKMARK_NOT_FOUND));
        flushAndClear();

        assertThat(bookmarkRepository.existsByMemberIdAndCultureId(member.getId(), culture.getId()))
                .isFalse();

        Culture foundCulture = cultureRepository.findById(culture.getId()).orElseThrow();
        assertThat(foundCulture.getBookmarkCount()).isZero();
    }

    private Member saveMember(String email) {
        return memberRepository.save(Member.createLocalMember(email, "encoded-password", "테스트회원"));
    }

    private Culture saveCulture(Long seq, String title) {
        Culture culture = Culture.builder()
                .seq(seq)
                .category(CultureCategory.EXHIBITION)
                .title(title)
                .startDate(LocalDate.of(2026, 6, 1))
                .endDate(LocalDate.of(2026, 6, 30))
                .thumbnail("https://example.com/thumbnail.jpg")
                .area("서울")
                .sigungu("강남구")
                .place("테스트 공연장")
                .gpsX(127.0)
                .gpsY(37.5)
                .build();
        return cultureRepository.save(culture);
    }

    private long countBookmarks(Long memberId, Long cultureId) {
        return bookmarkRepository.findAll().stream()
                .filter(bookmark -> bookmark.getMember().getId().equals(memberId))
                .filter(bookmark -> bookmark.getCulture().getId().equals(cultureId))
                .count();
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
