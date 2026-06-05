package com.ssafy.culturepick.culture.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CultureSummary {

    private Long id;
    private String title;
    private int bookmarkCount;
    private boolean isBookmarked;
}
