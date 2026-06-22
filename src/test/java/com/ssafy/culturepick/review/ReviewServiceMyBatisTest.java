package com.ssafy.culturepick.review;

import com.ssafy.culturepick.culture.domain.Culture;
import com.ssafy.culturepick.culture.domain.CultureCategory;
import com.ssafy.culturepick.culture.repository.CultureRepository;
import com.ssafy.culturepick.member.domain.Member;
import com.ssafy.culturepick.member.repository.MemberRepository;
import com.ssafy.culturepick.review.dto.request.ReviewRequest;
import com.ssafy.culturepick.review.dto.response.ReviewResponse;
import com.ssafy.culturepick.review.service.ReviewService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReviewServiceMyBatisTest {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CultureRepository cultureRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("ReviewService CRUD works through MyBatis")
    void reviewCrud_success_withMyBatis() {
        // given
        Member member = saveMember("review-mybatis@example.com", "reviewer");
        Culture culture = saveCulture(9101L, "MyBatis review culture");
        flushAndClear();

        // when
        ReviewResponse created = reviewService.createReview(
                culture.getId(),
                member.getId(),
                createReviewRequest("first review")
        );

        // then
        assertThat(created.getId()).isNotNull();
        assertThat(created.getCultureId()).isEqualTo(culture.getId());
        assertThat(created.getMemberId()).isEqualTo(member.getId());
        assertThat(created.getMemberNickname()).isEqualTo("reviewer");
        assertThat(created.getContent()).isEqualTo("first review");
        assertThat(created.isMine()).isTrue();

        // when
        // then
        assertThat(reviewService.getReviews(culture.getId(), member.getId()))
                .hasSize(1)
                .first()
                .satisfies(review -> {
                    assertThat(review.getId()).isEqualTo(created.getId());
                    assertThat(review.getContent()).isEqualTo("first review");
                });

        // when
        ReviewResponse updated = reviewService.updateReview(
                created.getId(),
                member.getId(),
                createReviewRequest("updated review")
        );

        // then
        assertThat(updated.getContent()).isEqualTo("updated review");

        // when
        reviewService.deleteReview(created.getId(), member.getId());

        // then
        assertThat(reviewService.getReviews(culture.getId(), member.getId())).isEmpty();
    }

    private Member saveMember(String email, String nickname) {
        return memberRepository.save(Member.createLocalMember(email, "encoded-password", nickname));
    }

    private Culture saveCulture(Long seq, String title) {
        return cultureRepository.save(Culture.builder()
                .seq(seq)
                .category(CultureCategory.EXHIBITION)
                .title(title)
                .startDate(LocalDate.of(2026, 6, 1))
                .endDate(LocalDate.of(2026, 6, 30))
                .thumbnail("https://example.com/review-thumbnail.jpg")
                .area("Seoul")
                .sigungu("Gangnam")
                .place("Test hall")
                .gpsX(127.0)
                .gpsY(37.5)
                .build());
    }

    private ReviewRequest createReviewRequest(String content) {
        ReviewRequest request = new ReviewRequest();
        ReflectionTestUtils.setField(request, "content", content);
        return request;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
