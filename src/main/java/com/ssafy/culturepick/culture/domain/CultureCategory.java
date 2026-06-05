package com.ssafy.culturepick.culture.domain;

import com.ssafy.culturepick.global.exception.code.CultureErrorCode;
import com.ssafy.culturepick.global.exception.type.BusinessException;
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
                .orElseThrow(() -> new BusinessException(CultureErrorCode.UNSUPPORTED_CATEGORY));
    }
}
