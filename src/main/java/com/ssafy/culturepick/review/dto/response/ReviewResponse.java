package com.ssafy.culturepick.review.dto.response;

import com.ssafy.culturepick.review.domain.Review;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReviewResponse {

    private Long id;
    private Long cultureId;
    private Long memberId;
    private String memberNickname;
    private String content;
    private boolean mine;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private ReviewResponse(Long id, Long cultureId, Long memberId, String memberNickname,
                           String content, boolean mine, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.cultureId = cultureId;
        this.memberId = memberId;
        this.memberNickname = memberNickname;
        this.content = content;
        this.mine = mine;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ReviewResponse from(Review review, Long currentMemberId) {
        Long writerId = review.getMember().getId();
        return new ReviewResponse(
                review.getId(),
                review.getCulture().getId(),
                writerId,
                review.getMember().getNickname(),
                review.getContent(),
                currentMemberId != null && currentMemberId.equals(writerId),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
