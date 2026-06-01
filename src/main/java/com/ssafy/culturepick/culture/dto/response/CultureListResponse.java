package com.ssafy.culturepick.culture.dto.response;

import com.ssafy.culturepick.culture.domain.Culture;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class CultureListResponse {

    private Long id;
    private String title;
    private String category;
    private String area;
    private String sigungu;
    private String thumbnail;
    private LocalDate startDate;
    private LocalDate endDate;
    private int bookmarkCount;
    private String place;
    private boolean isBookmarked;

    @Builder
    private CultureListResponse(Long id, String title, String category, String area, String sigungu, String thumbnail, LocalDate startDate, LocalDate endDate, int bookmarkCount, String place, boolean isBookmarked) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.area = area;
        this.sigungu = sigungu;
        this.thumbnail = thumbnail;
        this.startDate = startDate;
        this.endDate = endDate;
        this.bookmarkCount = bookmarkCount;
        this.place = place;
        this.isBookmarked = isBookmarked;
    }

    public static CultureListResponse from(Culture culture, boolean isBookmarked) {
        return CultureListResponse.builder()
                .id(culture.getId())
                .title(culture.getTitle())
                .category(culture.getCategory() != null ? culture.getCategory().getDescription() : null)
                .area(culture.getArea())
                .sigungu(culture.getSigungu())
                .thumbnail(culture.getThumbnail())
                .startDate(culture.getStartDate())
                .endDate(culture.getEndDate())
                .bookmarkCount(culture.getBookmarkCount())
                .place(culture.getPlace())
                .isBookmarked(isBookmarked)
                .build();
    }
}
