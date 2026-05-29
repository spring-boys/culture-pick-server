package com.ssafy.culturepick.culture.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum CultureCategory {

    EXHIBITION("전시"),
    PERFORMANCE("공연"),
    EDUCATION("교육/체험"),
    FESTIVAL("행사/축제");

    private final String description;

    public static CultureCategory from(String koreanName) {
        return Arrays.stream(values())
                .filter(c -> c.description.equals(koreanName))
                .findFirst()
                // TODO CultureErrorCode 처리
                .orElseThrow(() -> new IllegalArgumentException("알 수 없는 카테고리: " + koreanName));
    }
}
