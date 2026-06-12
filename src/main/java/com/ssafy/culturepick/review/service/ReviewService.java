package com.ssafy.culturepick.review.service;

import com.ssafy.culturepick.culture.domain.Culture;
import com.ssafy.culturepick.culture.repository.CultureRepository;
import com.ssafy.culturepick.global.exception.code.CultureErrorCode;
import com.ssafy.culturepick.global.exception.code.MemberErrorCode;
import com.ssafy.culturepick.global.exception.code.ReviewErrorCode;
import com.ssafy.culturepick.global.exception.type.BusinessException;
import com.ssafy.culturepick.member.domain.Member;
import com.ssafy.culturepick.member.repository.MemberRepository;
import com.ssafy.culturepick.review.domain.Review;
import com.ssafy.culturepick.review.dto.request.ReviewRequest;
import com.ssafy.culturepick.review.dto.response.ReviewResponse;
import com.ssafy.culturepick.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final CultureRepository cultureRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public ReviewResponse createReview(Long cultureId, Long memberId, ReviewRequest request) {
        Culture culture = getCulture(cultureId);
        Member member = getMember(memberId);
        Review review = reviewRepository.save(Review.create(culture, member, request.getContent()));

        return ReviewResponse.from(review, memberId);
    }

    public List<ReviewResponse> getReviews(Long cultureId, Long memberId) {
        getCulture(cultureId);

        return reviewRepository.findByCulture_IdOrderByCreatedAtDesc(cultureId).stream()
                .map(review -> ReviewResponse.from(review, memberId))
                .toList();
    }

    @Transactional
    public ReviewResponse updateReview(Long reviewId, Long memberId, ReviewRequest request) {
        Review review = getReview(reviewId);
        validateOwner(review, memberId);

        review.updateContent(request.getContent());
        return ReviewResponse.from(review, memberId);
    }

    @Transactional
    public void deleteReview(Long reviewId, Long memberId) {
        Review review = getReview(reviewId);
        validateOwner(review, memberId);

        reviewRepository.delete(review);
    }

    private Culture getCulture(Long cultureId) {
        return cultureRepository.findById(cultureId)
                .orElseThrow(() -> new BusinessException(CultureErrorCode.CULTURE_NOT_FOUND));
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    private Review getReview(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ReviewErrorCode.REVIEW_NOT_FOUND));
    }

    private void validateOwner(Review review, Long memberId) {
        if (!review.getMember().getId().equals(memberId)) {
            throw new BusinessException(ReviewErrorCode.NOT_REVIEW_OWNER);
        }
    }
}
