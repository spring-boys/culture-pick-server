package com.ssafy.culturepick.culture.repository;

import com.ssafy.culturepick.culture.domain.Culture;
import com.ssafy.culturepick.culture.domain.CultureCategory;
import com.ssafy.culturepick.global.config.QuerydslConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(QuerydslConfig.class)
class CultureRepositoryImplTest {

    @Autowired
    private CultureRepository cultureRepository;

    @Test
    @DisplayName("날짜별 목록 조회는 조회 날짜에 진행 중인 문화행사만 반환한다")
    void findList_returnsOnlyCulturesActiveOnDate() {
        // given
        LocalDate date = LocalDate.of(2026, 6, 10);
        Culture activeCulture = saveCulture(
                2001L,
                "날짜별 목록 포함 행사",
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 30),
                CultureCategory.EXHIBITION,
                "서울",
                0
        );
        saveCulture(
                2002L,
                "이미 종료된 행사",
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31),
                CultureCategory.EXHIBITION,
                "서울",
                0
        );
        saveCulture(
                2003L,
                "아직 시작하지 않은 행사",
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 31),
                CultureCategory.EXHIBITION,
                "서울",
                0
        );

        // when
        Slice<Culture> result = cultureRepository.findList(date, null, null, null, PageRequest.of(0, 10));

        // then
        assertThat(result.getContent())
                .extracting(Culture::getId)
                .containsExactly(activeCulture.getId());
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    @DisplayName("날짜별 목록 조회는 북마크 수 내림차순, 제목 오름차순으로 정렬한다")
    void findList_ordersByBookmarkCountDescAndTitleAsc() {
        // given
        LocalDate date = LocalDate.of(2026, 6, 10);
        saveCulture(2101L, "B", date.minusDays(1), date.plusDays(1), CultureCategory.EXHIBITION, "서울", 5);
        saveCulture(2102L, "C", date.minusDays(1), date.plusDays(1), CultureCategory.EXHIBITION, "서울", 10);
        saveCulture(2103L, "A", date.minusDays(1), date.plusDays(1), CultureCategory.EXHIBITION, "서울", 5);

        // when
        Slice<Culture> result = cultureRepository.findList(date, null, null, null, PageRequest.of(0, 10));

        // then
        assertThat(result.getContent())
                .extracting(Culture::getTitle)
                .containsExactly("C", "A", "B");
    }

    @Test
    @DisplayName("날짜별 목록 조회는 카테고리 필터에 맞는 문화행사만 반환한다")
    void findList_filtersByCategory() {
        // given
        LocalDate date = LocalDate.of(2026, 6, 10);
        Culture exhibition = saveCulture(
                2201L,
                "전시 행사",
                date.minusDays(1),
                date.plusDays(1),
                CultureCategory.EXHIBITION,
                "서울",
                0
        );
        saveCulture(
                2202L,
                "공연 행사",
                date.minusDays(1),
                date.plusDays(1),
                CultureCategory.PERFORMANCE,
                "서울",
                0
        );

        // when
        Slice<Culture> result = cultureRepository.findList(
                date,
                null,
                null,
                CultureCategory.EXHIBITION,
                PageRequest.of(0, 10)
        );

        // then
        assertThat(result.getContent())
                .extracting(Culture::getId)
                .containsExactly(exhibition.getId());
    }

    @Test
    @DisplayName("날짜별 목록 조회는 요청 크기보다 데이터가 더 있으면 content를 제한하고 hasNext를 true로 반환한다")
    void findList_returnsHasNextWhenMoreThanPageSize() {
        // given
        LocalDate date = LocalDate.of(2026, 6, 10);
        saveCulture(2301L, "Slice A", date.minusDays(1), date.plusDays(1), CultureCategory.EXHIBITION, "서울", 0);
        saveCulture(2302L, "Slice B", date.minusDays(1), date.plusDays(1), CultureCategory.EXHIBITION, "서울", 0);
        saveCulture(2303L, "Slice C", date.minusDays(1), date.plusDays(1), CultureCategory.EXHIBITION, "서울", 0);
        saveCulture(2304L, "Slice D", date.minusDays(1), date.plusDays(1), CultureCategory.EXHIBITION, "서울", 0);

        // when
        Slice<Culture> result = cultureRepository.findList(date, null, null, null, PageRequest.of(0, 3));

        // then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.hasNext()).isTrue();
    }

    private Culture saveCulture(
            Long seq,
            String title,
            LocalDate startDate,
            LocalDate endDate,
            CultureCategory category,
            String area,
            int bookmarkCount
    ) {
        Culture culture = Culture.builder()
                .seq(seq)
                .category(category)
                .title(title)
                .startDate(startDate)
                .endDate(endDate)
                .thumbnail("https://example.com/" + seq + ".jpg")
                .area(area)
                .sigungu("테스트구")
                .place("테스트 장소")
                .gpsX(127.0)
                .gpsY(37.5)
                .build();

        for (int i = 0; i < bookmarkCount; i++) {
            culture.incrementBookmarkCount();
        }

        return cultureRepository.saveAndFlush(culture);
    }
}
