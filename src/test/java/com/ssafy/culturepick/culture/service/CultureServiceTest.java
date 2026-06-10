package com.ssafy.culturepick.culture.service;

import com.ssafy.culturepick.culture.client.CultureApiClient;
import com.ssafy.culturepick.culture.domain.Culture;
import com.ssafy.culturepick.culture.domain.CultureCategory;
import com.ssafy.culturepick.culture.dto.client.CultureDetailApiResponse;
import com.ssafy.culturepick.culture.dto.response.CultureSummary;
import com.ssafy.culturepick.culture.dto.response.DayResponse;
import com.ssafy.culturepick.culture.repository.CultureRepository;
import com.ssafy.culturepick.global.exception.code.CultureErrorCode;
import com.ssafy.culturepick.global.exception.type.BusinessException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CultureServiceTest {

    @Autowired
    private CultureService cultureService;

    @Autowired
    private CultureRepository cultureRepository;

    @Autowired
    private EntityManager entityManager;

    @MockitoBean
    private CultureApiClient cultureApiClient;

    @Test
    @DisplayName("월 캘린더 조회는 해당 월에 진행 중인 문화행사를 날짜별 응답에 포함한다")
    void getCalendar_includesCulturesActiveInMonth() {
        // given
        Culture culture = saveCulture(
                3001L,
                "6월 진행 행사",
                LocalDate.of(2026, 6, 5),
                LocalDate.of(2026, 6, 10),
                CultureCategory.EXHIBITION,
                0,
                "서울",
                "https://example.com/3001.jpg"
        );

        // when
        List<DayResponse> result = cultureService.getCalendar(2026, 6, null, null, null, null);

        // then
        DayResponse activeDay = findDay(result, LocalDate.of(2026, 6, 10));
        assertThat(activeDay.getCultures())
                .extracting(CultureSummary::getId)
                .contains(culture.getId());

        DayResponse beforeStart = findDay(result, LocalDate.of(2026, 6, 4));
        assertThat(beforeStart.getCultures())
                .extracting(CultureSummary::getId)
                .doesNotContain(culture.getId());
    }

    @Test
    @DisplayName("월 캘린더 조회는 하루에 여러 행사가 있으면 대표 행사 3개만 반환한다")
    void getCalendar_limitsDailyCulturesToTop3() {
        // given
        LocalDate date = LocalDate.of(2026, 6, 10);
        saveCulture(3101L, "대표 행사 1", date.minusDays(1), date.plusDays(1), CultureCategory.EXHIBITION, 4, "서울", "https://example.com/3101.jpg");
        saveCulture(3102L, "대표 행사 2", date.minusDays(1), date.plusDays(1), CultureCategory.EXHIBITION, 3, "서울", "https://example.com/3102.jpg");
        saveCulture(3103L, "대표 행사 3", date.minusDays(1), date.plusDays(1), CultureCategory.EXHIBITION, 2, "서울", "https://example.com/3103.jpg");
        saveCulture(3104L, "제외 행사", date.minusDays(1), date.plusDays(1), CultureCategory.EXHIBITION, 1, "서울", "https://example.com/3104.jpg");

        // when
        List<DayResponse> result = cultureService.getCalendar(2026, 6, null, null, null, null);

        // then
        DayResponse day = findDay(result, date);
        assertThat(day.getTotalCount()).isEqualTo(4);
        assertThat(day.getCultures()).hasSize(3);
    }

    @Test
    @DisplayName("월 캘린더 조회의 하루 대표 행사는 북마크 수 내림차순, 제목 오름차순으로 정렬된다")
    void getCalendar_ordersDailyTop3ByBookmarkCountDescAndTitleAsc() {
        // given
        LocalDate date = LocalDate.of(2026, 6, 10);
        saveCulture(3201L, "C", date.minusDays(1), date.plusDays(1), CultureCategory.EXHIBITION, 10, "서울", "https://example.com/3201.jpg");
        saveCulture(3202L, "A", date.minusDays(1), date.plusDays(1), CultureCategory.EXHIBITION, 5, "서울", "https://example.com/3202.jpg");
        saveCulture(3203L, "B", date.minusDays(1), date.plusDays(1), CultureCategory.EXHIBITION, 5, "서울", "https://example.com/3203.jpg");
        saveCulture(3204L, "D", date.minusDays(1), date.plusDays(1), CultureCategory.EXHIBITION, 1, "서울", "https://example.com/3204.jpg");

        // when
        List<DayResponse> result = cultureService.getCalendar(2026, 6, null, null, null, null);

        // then
        DayResponse day = findDay(result, date);
        assertThat(day.getCultures())
                .extracting(CultureSummary::getTitle)
                .containsExactly("C", "A", "B");
    }

    @Test
    @DisplayName("존재하지 않는 문화행사 상세 조회는 CULTURE_NOT_FOUND 예외가 발생한다")
    void getDetail_throwsExceptionWhenCultureNotFound() {
        // when & then
        assertThatThrownBy(() -> cultureService.getDetail(999_999L, null))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(CultureErrorCode.CULTURE_NOT_FOUND));
    }

    @Test
    @DisplayName("상세 정보가 이미 있으면 기존 상세 정보가 유지된다")
    void getDetail_keepsExistingDetailWhenUrlAlreadyExists() {
        // given
        Culture culture = saveCulture(
                3301L,
                "상세 정보 보유 행사",
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 30),
                CultureCategory.EXHIBITION,
                0,
                "서울",
                "https://example.com/original-thumbnail.jpg"
        );
        culture.updateDetail(
                "기존 가격",
                "https://example.com/original-url",
                "051-111-1111",
                "https://example.com/ignored-image.jpg",
                "https://example.com/original-place",
                "기존 주소",
                111L
        );
        flushAndClear();

        // when
        cultureService.getDetail(culture.getId(), null);
        flushAndClear();

        // then
        Culture foundCulture = cultureRepository.findById(culture.getId()).orElseThrow();
        assertThat(foundCulture.getPrice()).isEqualTo("기존 가격");
        assertThat(foundCulture.getUrl()).isEqualTo("https://example.com/original-url");
        assertThat(foundCulture.getPhone()).isEqualTo("051-111-1111");
        assertThat(foundCulture.getPlaceUrl()).isEqualTo("https://example.com/original-place");
        assertThat(foundCulture.getPlaceAddr()).isEqualTo("기존 주소");
        assertThat(foundCulture.getPlaceSeq()).isEqualTo(111L);
        assertThat(foundCulture.getThumbnail()).isEqualTo("https://example.com/original-thumbnail.jpg");
    }

    @Test
    @DisplayName("상세 정보가 부족하면 외부 상세 API 응답으로 Culture 상세 필드가 보강된다")
    void getDetail_updatesMissingDetailFromApiResponse() {
        // given
        Culture culture = saveCulture(
                3401L,
                "상세 정보 부족 행사",
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 30),
                CultureCategory.EXHIBITION,
                0,
                "서울",
                "https://example.com/original-thumbnail.jpg"
        );
        when(cultureApiClient.getDetail(culture.getSeq()))
                .thenReturn(createDetailApiResponse(
                        culture.getSeq(),
                        "API 가격",
                        "https://example.com/api-url",
                        "051-222-2222",
                        "https://example.com/api-image.jpg",
                        "https://example.com/api-place",
                        "API 주소",
                        222L
                ));
        flushAndClear();

        // when
        cultureService.getDetail(culture.getId(), null);
        flushAndClear();

        // then
        Culture foundCulture = cultureRepository.findById(culture.getId()).orElseThrow();
        assertThat(foundCulture.getPrice()).isEqualTo("API 가격");
        assertThat(foundCulture.getUrl()).isEqualTo("https://example.com/api-url");
        assertThat(foundCulture.getPhone()).isEqualTo("051-222-2222");
        assertThat(foundCulture.getPlaceUrl()).isEqualTo("https://example.com/api-place");
        assertThat(foundCulture.getPlaceAddr()).isEqualTo("API 주소");
        assertThat(foundCulture.getPlaceSeq()).isEqualTo(222L);
    }

    @Test
    @DisplayName("상세 조회 시 thumbnail이 없으면 외부 상세 이미지로 보완된다")
    void getDetail_fillsThumbnailWhenThumbnailIsNull() {
        // given
        Culture culture = saveCulture(
                3501L,
                "썸네일 보완 행사",
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 30),
                CultureCategory.EXHIBITION,
                0,
                "서울",
                null
        );
        when(cultureApiClient.getDetail(culture.getSeq()))
                .thenReturn(createDetailApiResponse(
                        culture.getSeq(),
                        "무료",
                        "https://example.com/detail-url",
                        "051-333-3333",
                        "https://example.com/api-thumbnail.jpg",
                        "https://example.com/place",
                        "상세 주소",
                        333L
                ));
        flushAndClear();

        // when
        cultureService.getDetail(culture.getId(), null);
        flushAndClear();

        // then
        Culture foundCulture = cultureRepository.findById(culture.getId()).orElseThrow();
        assertThat(foundCulture.getThumbnail()).isEqualTo("https://example.com/api-thumbnail.jpg");
    }

    @Test
    @DisplayName("상세 조회 시 기존 thumbnail이 있으면 외부 상세 이미지로 덮어쓰지 않는다")
    void getDetail_keepsExistingThumbnailWhenThumbnailAlreadyExists() {
        // given
        Culture culture = saveCulture(
                3601L,
                "썸네일 유지 행사",
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 30),
                CultureCategory.EXHIBITION,
                0,
                "서울",
                "https://example.com/original-thumbnail.jpg"
        );
        when(cultureApiClient.getDetail(culture.getSeq()))
                .thenReturn(createDetailApiResponse(
                        culture.getSeq(),
                        "무료",
                        "https://example.com/detail-url",
                        "051-444-4444",
                        "https://example.com/api-thumbnail.jpg",
                        "https://example.com/place",
                        "상세 주소",
                        444L
                ));
        flushAndClear();

        // when
        cultureService.getDetail(culture.getId(), null);
        flushAndClear();

        // then
        Culture foundCulture = cultureRepository.findById(culture.getId()).orElseThrow();
        assertThat(foundCulture.getThumbnail()).isEqualTo("https://example.com/original-thumbnail.jpg");
    }

    private Culture saveCulture(
            Long seq,
            String title,
            LocalDate startDate,
            LocalDate endDate,
            CultureCategory category,
            int bookmarkCount,
            String area,
            String thumbnail
    ) {
        Culture culture = Culture.builder()
                .seq(seq)
                .category(category)
                .title(title)
                .startDate(startDate)
                .endDate(endDate)
                .thumbnail(thumbnail)
                .area(area)
                .sigungu("테스트구")
                .place("테스트 장소")
                .gpsX(127.0)
                .gpsY(37.5)
                .build();

        for (int i = 0; i < bookmarkCount; i++) {
            culture.incrementBookmarkCount();
        }

        return cultureRepository.save(culture);
    }

    private DayResponse findDay(List<DayResponse> responses, LocalDate date) {
        return responses.stream()
                .filter(response -> response.getDate().equals(date))
                .findFirst()
                .orElseThrow();
    }

    private CultureDetailApiResponse createDetailApiResponse(
            Long seq,
            String price,
            String url,
            String phone,
            String imgUrl,
            String placeUrl,
            String placeAddr,
            Long placeSeq
    ) {
        CultureDetailApiResponse response = new CultureDetailApiResponse();
        CultureDetailApiResponse.Body body = new CultureDetailApiResponse.Body();
        CultureDetailApiResponse.Item item = new CultureDetailApiResponse.Item();

        ReflectionTestUtils.setField(item, "seq", seq);
        ReflectionTestUtils.setField(item, "price", price);
        ReflectionTestUtils.setField(item, "url", url);
        ReflectionTestUtils.setField(item, "phone", phone);
        ReflectionTestUtils.setField(item, "imgUrl", imgUrl);
        ReflectionTestUtils.setField(item, "placeUrl", placeUrl);
        ReflectionTestUtils.setField(item, "placeAddr", placeAddr);
        ReflectionTestUtils.setField(item, "placeSeq", placeSeq);
        ReflectionTestUtils.setField(body, "items", List.of(item));
        ReflectionTestUtils.setField(response, "body", body);

        return response;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
