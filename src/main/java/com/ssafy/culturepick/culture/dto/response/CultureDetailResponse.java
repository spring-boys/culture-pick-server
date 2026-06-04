package com.ssafy.culturepick.culture.dto.response;

import com.ssafy.culturepick.culture.domain.Culture;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class CultureDetailResponse {

    private Long id;
    private String title;
    private String category;
    private LocalDate startDate;
    private LocalDate endDate;
    private String thumbnail;
    private String area;
    private String sigungu;
    private String place;
    private String placeAddr;
    private String placeUrl;
    private Double gpsX;
    private Double gpsY;
    private String price;
    private String url;
    private String phone;
    private int bookmarkCount;
    private boolean isBookmarked;

    public static CultureDetailResponse from(Culture culture, boolean isBookmarked) {
        return CultureDetailResponse.builder()
                .id(culture.getId())
                .title(culture.getTitle())
                .category(culture.getCategory() != null ? culture.getCategory().getDescription() : null)
                .startDate(culture.getStartDate())
                .endDate(culture.getEndDate())
                .thumbnail(culture.getThumbnail())
                .area(culture.getArea())
                .sigungu(culture.getSigungu())
                .place(culture.getPlace())
                .placeAddr(culture.getPlaceAddr())
                .placeUrl(culture.getPlaceUrl())
                .gpsX(culture.getGpsX())
                .gpsY(culture.getGpsY())
                .price(culture.getPrice())
                .url(culture.getUrl())
                .phone(culture.getPhone())
                .bookmarkCount(culture.getBookmarkCount())
                .isBookmarked(isBookmarked)
                .build();
    }
}
