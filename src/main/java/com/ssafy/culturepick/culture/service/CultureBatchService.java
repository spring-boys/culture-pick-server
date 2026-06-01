package com.ssafy.culturepick.culture.service;

import com.ssafy.culturepick.culture.client.CultureApiClient;
import com.ssafy.culturepick.culture.domain.Culture;
import com.ssafy.culturepick.culture.domain.CultureCategory;
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

        for (CultureListResponse.Item item : items) {
            try {
                cultureRepository.findBySeq(item.getSeq())
                        .ifPresentOrElse(
                                culture -> culture.update(
                                        item.getTitle(),
                                        CultureCategory.from(item.getServiceName()),
                                        parseDate(item.getStartDate()),
                                        parseDate(item.getEndDate()),
                                        item.getThumbnail(),
                                        item.getArea(),
                                        item.getSigungu(),
                                        item.getPlace(),
                                        item.getGpsX(),
                                        item.getGpsY()
                                ),
                                () -> cultureRepository.save(Culture.builder()
                                        .seq(item.getSeq())
                                        .title(item.getTitle())
                                        .category(CultureCategory.from(item.getServiceName()))
                                        .startDate(parseDate(item.getStartDate()))
                                        .endDate(parseDate(item.getEndDate()))
                                        .thumbnail(item.getThumbnail())
                                        .area(item.getArea())
                                        .sigungu(item.getSigungu())
                                        .place(item.getPlace())
                                        .gpsX(item.getGpsX())
                                        .gpsY(item.getGpsY())
                                        .build())
                        );
            } catch (Exception e) {
                log.warn("seq={} 처리 중 오류 발생, 스킵합니다. error={}", item.getSeq(), e.getMessage());
            }
        }
    }

    private LocalDate parseDate(String date) {
        if (date == null || date.isBlank()) return null;
        return LocalDate.parse(date, DATE_FORMATTER);
    }
}
