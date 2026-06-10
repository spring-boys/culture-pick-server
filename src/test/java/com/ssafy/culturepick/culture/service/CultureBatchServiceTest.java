package com.ssafy.culturepick.culture.service;

import com.ssafy.culturepick.culture.client.CultureApiClient;
import com.ssafy.culturepick.culture.domain.Culture;
import com.ssafy.culturepick.culture.domain.CultureCategory;
import com.ssafy.culturepick.culture.dto.client.CultureListApiResponse;
import com.ssafy.culturepick.culture.repository.CultureRepository;
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
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CultureBatchServiceTest {

    @Autowired
    private CultureBatchService cultureBatchService;

    @Autowired
    private CultureRepository cultureRepository;

    @Autowired
    private EntityManager entityManager;

    @MockitoBean
    private CultureApiClient cultureApiClient;

    @Test
    @DisplayName("신규 seq면 Culture가 새로 저장된다")
    void fetchAndSaveAll_savesNewCultureWhenSeqDoesNotExist() {
        // given
        Long seq = 4001L;
        when(cultureApiClient.getList())
                .thenReturn(createListApiResponse(List.of(createItem(
                        seq,
                        "전시",
                        "신규 문화행사",
                        "20260601",
                        "20260630",
                        "부산",
                        "해운대구",
                        "테스트 전시장",
                        "https://example.com/4001.jpg",
                        129.1604,
                        35.1631
                ))));

        // when
        cultureBatchService.fetchAndSaveAll();
        flushAndClear();

        // then
        Culture foundCulture = cultureRepository.findBySeq(seq).orElseThrow();
        assertThat(foundCulture.getTitle()).isEqualTo("신규 문화행사");
        assertThat(foundCulture.getCategory()).isEqualTo(CultureCategory.EXHIBITION);
        assertThat(foundCulture.getArea()).isEqualTo("부산");
        assertThat(foundCulture.getSigungu()).isEqualTo("해운대구");
        assertThat(foundCulture.getPlace()).isEqualTo("테스트 전시장");
        assertThat(foundCulture.getThumbnail()).isEqualTo("https://example.com/4001.jpg");
        assertThat(foundCulture.getGpsX()).isEqualTo(129.1604);
        assertThat(foundCulture.getGpsY()).isEqualTo(35.1631);
    }

    @Test
    @DisplayName("기존 seq면 중복 저장 없이 기존 Culture가 업데이트된다")
    void fetchAndSaveAll_updatesExistingCultureWhenSeqAlreadyExists() {
        // given
        Long seq = 4002L;
        Culture existingCulture = saveCulture(
                seq,
                "기존 제목",
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31),
                CultureCategory.PERFORMANCE,
                "서울",
                "강남구",
                "기존 공연장",
                "https://example.com/original.jpg"
        );
        Long existingId = existingCulture.getId();

        when(cultureApiClient.getList())
                .thenReturn(createListApiResponse(List.of(createItem(
                        seq,
                        "행사/축제",
                        "수정된 제목",
                        "20260701",
                        "20260731",
                        "대구",
                        "중구",
                        "수정된 장소",
                        "https://example.com/updated.jpg",
                        128.6014,
                        35.8714
                ))));
        flushAndClear();

        // when
        cultureBatchService.fetchAndSaveAll();
        flushAndClear();

        // then
        Culture foundCulture = cultureRepository.findBySeq(seq).orElseThrow();
        assertThat(foundCulture.getId()).isEqualTo(existingId);
        assertThat(cultureRepository.findAll())
                .filteredOn(culture -> culture.getSeq().equals(seq))
                .hasSize(1);
        assertThat(foundCulture.getTitle()).isEqualTo("수정된 제목");
        assertThat(foundCulture.getCategory()).isEqualTo(CultureCategory.FESTIVAL);
        assertThat(foundCulture.getStartDate()).isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(foundCulture.getEndDate()).isEqualTo(LocalDate.of(2026, 7, 31));
        assertThat(foundCulture.getArea()).isEqualTo("대구");
        assertThat(foundCulture.getSigungu()).isEqualTo("중구");
        assertThat(foundCulture.getPlace()).isEqualTo("수정된 장소");
        assertThat(foundCulture.getThumbnail()).isEqualTo("https://example.com/updated.jpg");
    }

    @Test
    @DisplayName("yyyyMMdd 문자열은 LocalDate로 변환되어 저장된다")
    void fetchAndSaveAll_convertsDateStringToLocalDate() {
        // given
        Long seq = 4003L;
        when(cultureApiClient.getList())
                .thenReturn(createListApiResponse(List.of(createItem(
                        seq,
                        "공연",
                        "날짜 변환 행사",
                        "20260601",
                        "20260630",
                        "서울",
                        "종로구",
                        "테스트 공연장",
                        "https://example.com/4003.jpg",
                        126.9780,
                        37.5665
                ))));

        // when
        cultureBatchService.fetchAndSaveAll();
        flushAndClear();

        // then
        Culture foundCulture = cultureRepository.findBySeq(seq).orElseThrow();
        assertThat(foundCulture.getStartDate()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(foundCulture.getEndDate()).isEqualTo(LocalDate.of(2026, 6, 30));
    }

    @Test
    @DisplayName("외부 API items가 null이면 저장 없이 종료된다")
    void fetchAndSaveAll_returnsWithoutSavingWhenItemsIsNull() {
        // given
        long countBefore = cultureRepository.count();
        when(cultureApiClient.getList()).thenReturn(createListApiResponse(null));

        // when
        cultureBatchService.fetchAndSaveAll();
        flushAndClear();

        // then
        assertThat(cultureRepository.count()).isEqualTo(countBefore);
    }

    @Test
    @DisplayName("빈 날짜 문자열은 null로 저장된다")
    void fetchAndSaveAll_savesBlankDateAsNull() {
        // given
        Long seq = 4005L;
        when(cultureApiClient.getList())
                .thenReturn(createListApiResponse(List.of(createItem(
                        seq,
                        "교육/체험",
                        "빈 날짜 행사",
                        "",
                        " ",
                        "광주",
                        "동구",
                        "테스트 체험관",
                        "https://example.com/4005.jpg",
                        126.9236,
                        35.1595
                ))));

        // when
        cultureBatchService.fetchAndSaveAll();
        flushAndClear();

        // then
        Culture foundCulture = cultureRepository.findBySeq(seq).orElseThrow();
        assertThat(foundCulture.getStartDate()).isNull();
        assertThat(foundCulture.getEndDate()).isNull();
    }

    @Test
    @DisplayName("일부 item 처리 중 예외가 발생해도 다음 item 처리가 계속된다")
    void fetchAndSaveAll_continuesWhenAnItemFails() {
        // given
        Long failedSeq = 4006L;
        Long validSeq = 4007L;
        when(cultureApiClient.getList())
                .thenReturn(createListApiResponse(List.of(
                        createItem(
                                failedSeq,
                                "지원하지 않는 카테고리",
                                "저장 실패 행사",
                                "20260601",
                                "20260630",
                                "서울",
                                "마포구",
                                "실패 장소",
                                "https://example.com/4006.jpg",
                                126.9015,
                                37.5663
                        ),
                        createItem(
                                validSeq,
                                "전시",
                                "정상 저장 행사",
                                "20260801",
                                "20260831",
                                "인천",
                                "연수구",
                                "정상 장소",
                                "https://example.com/4007.jpg",
                                126.7052,
                                37.4563
                        )
                )));

        // when
        cultureBatchService.fetchAndSaveAll();
        flushAndClear();

        // then
        assertThat(cultureRepository.findBySeq(failedSeq)).isEmpty();
        Culture foundCulture = cultureRepository.findBySeq(validSeq).orElseThrow();
        assertThat(foundCulture.getTitle()).isEqualTo("정상 저장 행사");
        assertThat(foundCulture.getCategory()).isEqualTo(CultureCategory.EXHIBITION);
    }

    private Culture saveCulture(
            Long seq,
            String title,
            LocalDate startDate,
            LocalDate endDate,
            CultureCategory category,
            String area,
            String sigungu,
            String place,
            String thumbnail
    ) {
        Culture culture = Culture.builder()
                .seq(seq)
                .title(title)
                .startDate(startDate)
                .endDate(endDate)
                .category(category)
                .area(area)
                .sigungu(sigungu)
                .place(place)
                .thumbnail(thumbnail)
                .gpsX(127.0)
                .gpsY(37.5)
                .build();

        return cultureRepository.save(culture);
    }

    private CultureListApiResponse createListApiResponse(List<CultureListApiResponse.Item> items) {
        CultureListApiResponse response = new CultureListApiResponse();
        CultureListApiResponse.Body body = new CultureListApiResponse.Body();

        ReflectionTestUtils.setField(body, "items", items);
        ReflectionTestUtils.setField(response, "body", body);

        return response;
    }

    private CultureListApiResponse.Item createItem(
            Long seq,
            String serviceName,
            String title,
            String startDate,
            String endDate,
            String area,
            String sigungu,
            String place,
            String thumbnail,
            Double gpsX,
            Double gpsY
    ) {
        CultureListApiResponse.Item item = new CultureListApiResponse.Item();

        ReflectionTestUtils.setField(item, "seq", seq);
        ReflectionTestUtils.setField(item, "serviceName", serviceName);
        ReflectionTestUtils.setField(item, "title", title);
        ReflectionTestUtils.setField(item, "startDate", startDate);
        ReflectionTestUtils.setField(item, "endDate", endDate);
        ReflectionTestUtils.setField(item, "area", area);
        ReflectionTestUtils.setField(item, "sigungu", sigungu);
        ReflectionTestUtils.setField(item, "place", place);
        ReflectionTestUtils.setField(item, "thumbnail", thumbnail);
        ReflectionTestUtils.setField(item, "gpsX", gpsX);
        ReflectionTestUtils.setField(item, "gpsY", gpsY);

        return item;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
