package com.ssafy.culturepick.review.service;

import com.ssafy.culturepick.culture.repository.CultureRepository;
import com.ssafy.culturepick.global.exception.code.CultureErrorCode;
import com.ssafy.culturepick.global.exception.code.MemberErrorCode;
import com.ssafy.culturepick.global.exception.code.ReviewErrorCode;
import com.ssafy.culturepick.global.exception.type.BusinessException;
import com.ssafy.culturepick.member.repository.MemberRepository;
import com.ssafy.culturepick.review.client.GmsClient;
import com.ssafy.culturepick.review.dto.request.ReviewRequest;
import com.ssafy.culturepick.global.common.SliceResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import com.ssafy.culturepick.review.dto.response.ReviewResponse;
import com.ssafy.culturepick.review.dto.response.ReviewSummaryResponse;
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
    private final GmsClient gmsClient;

    @Transactional
    public ReviewResponse createReview(Long cultureId, Long memberId, ReviewRequest request) {
        validateCultureExists(cultureId);
        validateMemberExists(memberId);

        ReviewCreateCommand command = new ReviewCreateCommand(cultureId, memberId, request.getContent());
        reviewMapper.insert(command);

        return ReviewResponse.from(getReview(command.getId()), memberId);
    }

    public SliceResponse<ReviewResponse> getReviews(Long cultureId, Long memberId, int page, int size) {
        validateCultureExists(cultureId);

        int offset = page * size;
        List<ReviewMapperResult> results = reviewMapper.findPageByCultureId(cultureId, offset, size + 1);

        boolean hasNext = results.size() > size;
        List<ReviewMapperResult> pageContent = hasNext ? results.subList(0, size) : results;

        List<ReviewResponse> content = pageContent.stream()
                .map(review -> ReviewResponse.from(review, memberId))
                .toList();

        return SliceResponse.<ReviewResponse>builder()
                .currentPage(page)
                .hasNext(hasNext)
                .content(content)
                .build();
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

    public ReviewSummaryResponse getReviewSummary(Long cultureId) {
        validateCultureExists(cultureId);

        List<ReviewMapperResult> reviews = reviewMapper.findAllByCultureId(cultureId);
        if (reviews.isEmpty()) {
            return new ReviewSummaryResponse("아직 작성된 리뷰가 없습니다.");
        }

        String summary = gmsClient.generateText(
                "당신은 문화 행사 리뷰 요약 전문가입니다. 사용자 리뷰들을 읽고 핵심 의견과 전반적인 분위기를 3~4문장으로 한국어로 요약해주세요.",
                buildReviewsText(reviews)
        );
        return new ReviewSummaryResponse(summary);
    }

    public void streamReviewSummary(Long cultureId, SseEmitter emitter) {
        validateCultureExists(cultureId);

        List<ReviewMapperResult> reviews = reviewMapper.findAllByCultureId(cultureId);
        if (reviews.isEmpty()) {
            try {
                emitter.send("아직 작성된 리뷰가 없습니다.");
                emitter.complete();
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
            return;
        }

        gmsClient.streamGenerateText(
                "당신은 문화 행사 리뷰 요약 전문가입니다. 사용자 리뷰들을 읽고 핵심 의견과 전반적인 분위기를 3~4문장으로 한국어로 요약해주세요.",
                buildReviewsText(reviews),
                emitter
        );
    }

    private String buildReviewsText(List<ReviewMapperResult> reviews) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < reviews.size(); i++) {
            sb.append(i + 1).append(". ").append(reviews.get(i).getContent()).append("\n");
        }
        return sb.toString();
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
