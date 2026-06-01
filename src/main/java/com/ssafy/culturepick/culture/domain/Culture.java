package com.ssafy.culturepick.culture.domain;

import com.ssafy.culturepick.global.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Culture extends BaseEntity {

    @Id
    @Column(name = "culture_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long seq;

    @Enumerated(EnumType.STRING)
    private CultureCategory category;

    @Column(nullable = false)
    private String title;

    private LocalDate startDate;
    private LocalDate endDate;

    private String thumbnail;

    private String area; // 부산
    private String sigungu; // 사하구
    private String place; // 부산현대미술관
    private String placeUrl; // 부산현대미술관 공식 홈페이지
    private String url; // 부산현대미술관 > 해당 문화 상세 정보 페이지
    private String phone; // 부산현대미술관 전화번호
    private String placeAddr; // 부산현대미술관 주소
    private Long placeSeq; // 부산현대미술관 고유번호

    private String price;

    private Double gpsX;
    private Double gpsY;

    private int bookmarkCount;

    public void incrementBookmarkCount() {
        this.bookmarkCount++;
    }

    public void decrementBookmarkCount() {
        if (this.bookmarkCount > 0) this.bookmarkCount--;
    }

    @Builder
    private Culture(
            Long seq, CultureCategory category, String title, LocalDate startDate, LocalDate endDate,
            String thumbnail, String area, String sigungu, String place, Double gpsX, Double gpsY
    ) {
        this.seq = seq;
        this.category = category;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.thumbnail = thumbnail;
        this.area = area;
        this.sigungu = sigungu;
        this.place = place;
        this.gpsX = gpsX;
        this.gpsY = gpsY;
    }

    public void update(
            String title, CultureCategory category, LocalDate startDate, LocalDate endDate,
            String thumbnail, String area, String sigungu, String place, Double gpsX, Double gpsY
    ) {
        this.title = title;
        this.category = category;
        this.startDate = startDate;
        this.endDate = endDate;
        this.thumbnail = thumbnail;
        this.area = area;
        this.sigungu = sigungu;
        this.place = place;
        this.gpsX = gpsX;
        this.gpsY = gpsY;
    }

    public void updateDetail(
            String price, String url, String phone, String imgUrl,
            String placeUrl, String placeAddr, Long placeSeq
    ) {
        this.price = price;
        this.url = url;
        this.phone = phone;
        if (this.thumbnail == null) this.thumbnail = imgUrl;
        this.placeUrl = placeUrl;
        this.placeAddr = placeAddr;
        this.placeSeq = placeSeq;
    }
}
