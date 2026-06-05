package com.ssafy.culturepick.culture.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
public class DayResponse {

    private LocalDate date;
    private int totalCount;
    private List<CultureSummary> cultures;

    @Builder
    private DayResponse(LocalDate date, int totalCount, List<CultureSummary> cultures) {
        this.date = date;
        this.totalCount = totalCount;
        this.cultures = cultures;
    }

    public static DayResponse of(LocalDate date, int totalCount, List<CultureSummary> cultures) {
        return DayResponse.builder()
                .date(date)
                .totalCount(totalCount)
                .cultures(cultures)
                .build();
    }
}
