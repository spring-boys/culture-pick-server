package com.ssafy.culturepick.review.mapper;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewCreateCommand {

    private Long id;
    private Long cultureId;
    private Long memberId;
    private String content;

    public ReviewCreateCommand(Long cultureId, Long memberId, String content) {
        this.cultureId = cultureId;
        this.memberId = memberId;
        this.content = content;
    }
}
