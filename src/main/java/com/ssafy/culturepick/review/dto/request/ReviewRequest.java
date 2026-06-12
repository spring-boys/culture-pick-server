package com.ssafy.culturepick.review.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReviewRequest {

    @NotBlank(message = "리뷰 내용을 입력해주세요.")
    @Size(max = 500, message = "리뷰 내용은 500자 이하로 입력해주세요.")
    private String content;

    @Builder
    private ReviewRequest(String content) {
        this.content = content;
    }
}
