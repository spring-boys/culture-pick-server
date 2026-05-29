package com.ssafy.culturepick.culture.service;

import com.ssafy.culturepick.culture.client.CultureApiClient;
import com.ssafy.culturepick.culture.domain.Culture;
import com.ssafy.culturepick.culture.domain.CultureCategory;
import com.ssafy.culturepick.culture.dto.client.CultureDetailResponse;
import com.ssafy.culturepick.culture.dto.client.CultureListResponse;
import com.ssafy.culturepick.culture.repository.CultureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CultureBatchService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final CultureApiClient cultureApiClient;
    private final CultureRepository cultureRepository;

    @Transactional
    public void fetchAndSaveAll() {
        CultureListResponse listResponse = cultureApiClient.getList();
        List<CultureListResponse.Item> items = listResponse.getBody().getItems();

        if (items == null) {
            log.warn("문화 목록 조회 결과가 없습니다.");
            return;
        }

        for (CultureListResponse.Item listItem : items) {
            try {
                Thread.sleep(100);

                CultureDetailResponse.Item detailItem = cultureApiClient.getDetail(listItem.getSeq()).getBody().getItems().getItem();

                String thumbnail = listItem.getThumbnail() != null
                        ? listItem.getThumbnail()
                        : detailItem.getImgUrl();

                cultureRepository.findBySeq(listItem.getSeq())
                        .ifPresentOrElse(
                                culture -> culture.update(
                                        listItem.getTitle(),
                                        CultureCategory.from(listItem.getServiceName()),
                                        parseDate(listItem.getStartDate()),
                                        parseDate(listItem.getEndDate()),
                                        thumbnail,
                                        listItem.getArea(),
                                        listItem.getSigungu(),
                                        listItem.getPlace(),
                                        detailItem.getPlaceUrl(),
                                        detailItem.getUrl(),
                                        detailItem.getPhone(),
                                        detailItem.getPlaceAddr(),
                                        detailItem.getPlaceSeq(),
                                        detailItem.getPrice(),
                                        listItem.getGpsX(),
                                        listItem.getGpsY()
                                ),
                                () -> cultureRepository.save(Culture.builder()
                                        .seq(listItem.getSeq())
                                        .title(listItem.getTitle())
                                        .category(CultureCategory.from(listItem.getServiceName()))
                                        .startDate(parseDate(listItem.getStartDate()))
                                        .endDate(parseDate(listItem.getEndDate()))
                                        .thumbnail(thumbnail)
                                        .area(listItem.getArea())
                                        .sigungu(listItem.getSigungu())
                                        .place(listItem.getPlace())
                                        .placeUrl(detailItem.getPlaceUrl())
                                        .url(detailItem.getUrl())
                                        .phone(detailItem.getPhone())
                                        .placeAddr(detailItem.getPlaceAddr())
                                        .placeSeq(detailItem.getPlaceSeq())
                                        .price(detailItem.getPrice())
                                        .gpsX(listItem.getGpsX())
                                        .gpsY(listItem.getGpsY())
                                        .build())
                        );
            } catch (Exception e) {
                log.warn("seq={} 처리 중 오류 발생, 스킵합니다. error={}", listItem.getSeq(), e.getMessage());
            }
        }
    }

    private LocalDate parseDate(String date) {
        if (date == null || date.isBlank()) return null;
        return LocalDate.parse(date, DATE_FORMATTER);
    }
}
