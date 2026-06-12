package com.ssafy.culturepick.review.controller;

import com.ssafy.culturepick.auth.security.CustomMemberDetails;
import com.ssafy.culturepick.review.dto.request.ReviewRequest;
import com.ssafy.culturepick.review.dto.response.ReviewResponse;
import com.ssafy.culturepick.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @GetMapping("/cultures/{cultureId}/reviews")
    public ResponseEntity<List<ReviewResponse>> getReviews(
            @PathVariable Long cultureId,
            @AuthenticationPrincipal CustomMemberDetails memberDetails
    ) {
        Long memberId = memberDetails != null ? memberDetails.getId() : null;
        return ResponseEntity.ok(reviewService.getReviews(cultureId, memberId));
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
