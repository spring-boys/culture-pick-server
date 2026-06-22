package com.ssafy.culturepick.member.dto.response;

import com.ssafy.culturepick.culture.domain.Culture;
import com.ssafy.culturepick.culture.domain.CultureCategory;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BookmarkedCultureResponse {

    private Long id;
    private Long seq;
    private CultureCategory category;
    private String title;
    private String thumbnail;
    private String area;
    private String sigungu;
    private String place;
    private int bookmarkCount;

    @Builder
    private BookmarkedCultureResponse(Long id, Long seq, CultureCategory category, String title, String thumbnail, String area, String sigungu, String place, int bookmarkCount) {
        this.id = id;
        this.seq = seq;
        this.category = category;
        this.title = title;
        this.thumbnail = thumbnail;
        this.area = area;
        this.sigungu = sigungu;
        this.place = place;
        this.bookmarkCount = bookmarkCount;
    }

    public static BookmarkedCultureResponse of(Culture culture) {
        return BookmarkedCultureResponse.builder()
                .id(culture.getId())
                .seq(culture.getSeq())
                .category(culture.getCategory())
                .title(culture.getTitle())
                .thumbnail(culture.getThumbnail())
                .area(culture.getArea())
                .sigungu(culture.getSigungu())
                .place(culture.getPlace())
                .bookmarkCount(culture.getBookmarkCount())
                .build();
    }
}
