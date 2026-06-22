package com.ssafy.culturepick.review.mapper;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReviewMapperResult {

    private Long id;
    private Long cultureId;
    private Long memberId;
    private String memberNickname;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
