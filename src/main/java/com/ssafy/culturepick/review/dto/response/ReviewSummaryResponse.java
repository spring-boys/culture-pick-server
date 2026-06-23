package com.ssafy.culturepick.review.dto.response;

import lombok.Getter;

@Getter
public class ReviewSummaryResponse {

    private final String summary;

    public ReviewSummaryResponse(String summary) {
        this.summary = summary;
    }
}
