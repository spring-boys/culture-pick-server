package com.ssafy.culturepick.review.controller;

import com.ssafy.culturepick.auth.security.CustomMemberDetails;
import com.ssafy.culturepick.review.dto.request.ReviewRequest;
import com.ssafy.culturepick.global.common.SliceResponse;
import com.ssafy.culturepick.review.dto.response.ReviewResponse;
import com.ssafy.culturepick.review.dto.response.ReviewSummaryResponse;
import com.ssafy.culturepick.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.CompletableFuture;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/cultures/{cultureId}/reviews")
    public ResponseEntity<ReviewResponse> createReview(
            @PathVariable Long cultureId,
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @Valid @RequestBody ReviewRequest request
    ) {
        ReviewResponse response = reviewService.createReview(cultureId, memberDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/cultures/{cultureId}/reviews/summary")
    public ResponseEntity<ReviewSummaryResponse> getReviewSummary(
            @PathVariable Long cultureId
    ) {
        return ResponseEntity.ok(reviewService.getReviewSummary(cultureId));
    }

    @GetMapping(value = "/cultures/{cultureId}/reviews/summary/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamReviewSummary(@PathVariable Long cultureId) {
        SseEmitter emitter = new SseEmitter(60_000L);
        CompletableFuture.runAsync(() -> reviewService.streamReviewSummary(cultureId, emitter))
                .exceptionally(e -> { emitter.completeWithError(e); return null; });
        return emitter;
    }

    @GetMapping("/cultures/{cultureId}/reviews")
    public ResponseEntity<SliceResponse<ReviewResponse>> getReviews(
            @PathVariable Long cultureId,
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Long memberId = memberDetails != null ? memberDetails.getId() : null;
        return ResponseEntity.ok(reviewService.getReviews(cultureId, memberId, page, size));
    }

    @PatchMapping("/reviews/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @Valid @RequestBody ReviewRequest request
    ) {
        return ResponseEntity.ok(reviewService.updateReview(reviewId, memberDetails.getId(), request));
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal CustomMemberDetails memberDetails
    ) {
        reviewService.deleteReview(reviewId, memberDetails.getId());
        return ResponseEntity.noContent().build();
    }
}
