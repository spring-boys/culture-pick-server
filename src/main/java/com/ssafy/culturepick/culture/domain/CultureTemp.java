package com.ssafy.culturepick.culture.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@Table(name = "culture_temp")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CultureTemp {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 200)
    private String place;

    @Column(nullable = false, length = 50)
    private String area;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    private CultureTemp(String title, String place, String area, LocalDate startDate, LocalDate endDate) {
        this.title = title;
        this.place = place;
        this.area = area;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public static CultureTemp create(String title, String place, String area, LocalDate startDate, LocalDate endDate) {
        return new CultureTemp(title, place, area, startDate, endDate);
    }
}
