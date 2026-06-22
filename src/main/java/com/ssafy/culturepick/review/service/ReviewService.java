package com.ssafy.culturepick.review.service;

import com.ssafy.culturepick.culture.repository.CultureRepository;
import com.ssafy.culturepick.global.exception.code.CultureErrorCode;
import com.ssafy.culturepick.global.exception.code.MemberErrorCode;
import com.ssafy.culturepick.global.exception.code.ReviewErrorCode;
import com.ssafy.culturepick.global.exception.type.BusinessException;
import com.ssafy.culturepick.member.repository.MemberRepository;
import com.ssafy.culturepick.review.dto.request.ReviewRequest;
import com.ssafy.culturepick.review.dto.response.ReviewResponse;
import com.ssafy.culturepick.review.mapper.ReviewCreateCommand;
import com.ssafy.culturepick.review.mapper.ReviewMapper;
import com.ssafy.culturepick.review.mapper.ReviewMapperResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewMapper reviewMapper;
    private final CultureRepository cultureRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public ReviewResponse createReview(Long cultureId, Long memberId, ReviewRequest request) {
        validateCultureExists(cultureId);
        validateMemberExists(memberId);

        ReviewCreateCommand command = new ReviewCreateCommand(cultureId, memberId, request.getContent());
        reviewMapper.insert(command);

        return ReviewResponse.from(getReview(command.getId()), memberId);
    }

    public List<ReviewResponse> getReviews(Long cultureId, Long memberId) {
        validateCultureExists(cultureId);

        return reviewMapper.findAllByCultureId(cultureId).stream()
                .map(review -> ReviewResponse.from(review, memberId))
                .toList();
    }

    @Transactional
    public ReviewResponse updateReview(Long reviewId, Long memberId, ReviewRequest request) {
        ReviewMapperResult review = getReview(reviewId);
        validateOwner(review, memberId);

        reviewMapper.updateContent(reviewId, request.getContent());
        return ReviewResponse.from(getReview(reviewId), memberId);
    }

    @Transactional
    public void deleteReview(Long reviewId, Long memberId) {
        ReviewMapperResult review = getReview(reviewId);
        validateOwner(review, memberId);

        reviewMapper.deleteById(reviewId);
    }

    private void validateCultureExists(Long cultureId) {
        if (!cultureRepository.existsById(cultureId)) {
            throw new BusinessException(CultureErrorCode.CULTURE_NOT_FOUND);
        }
    }

    private void validateMemberExists(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND);
        }
    }

    private ReviewMapperResult getReview(Long reviewId) {
        ReviewMapperResult review = reviewMapper.findById(reviewId);
        if (review == null) {
            throw new BusinessException(ReviewErrorCode.REVIEW_NOT_FOUND);
        }
        return review;
    }

    private void validateOwner(ReviewMapperResult review, Long memberId) {
        if (!review.getMemberId().equals(memberId)) {
            throw new BusinessException(ReviewErrorCode.NOT_REVIEW_OWNER);
        }
    }
}
