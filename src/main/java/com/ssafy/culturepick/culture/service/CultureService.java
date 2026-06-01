package com.ssafy.culturepick.culture.service;

import com.ssafy.culturepick.bookmark.repository.BookmarkRepository;
import com.ssafy.culturepick.culture.domain.Culture;
import com.ssafy.culturepick.culture.domain.CultureCategory;
import com.ssafy.culturepick.culture.dto.response.CultureListResponse;
import com.ssafy.culturepick.culture.dto.response.CultureSummary;
import com.ssafy.culturepick.culture.dto.response.DayResponse;
import com.ssafy.culturepick.culture.repository.CultureRepository;
import com.ssafy.culturepick.global.common.SliceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CultureService {

    private static final int MAX_CULTURES_PER_DAY = 3;

    private final CultureRepository cultureRepository;
    private final BookmarkRepository bookmarkRepository;

    public List<DayResponse> getCalendar(int year, int month, String keyword, String area, CultureCategory category, Long memberId) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate rangeStart = yearMonth.atDay(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        LocalDate rangeEnd = yearMonth.atEndOfMonth().with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));

        List<Culture> cultures = cultureRepository.findCalendar(rangeStart, rangeEnd, keyword, area, category);
        Set<Long> bookmarkedIds = getBookmarkedIds(memberId, cultures);

        List<DayResponse> result = new ArrayList<>();
        LocalDate current = rangeStart;

        while (!current.isAfter(rangeEnd)) {
            LocalDate date = current;

            List<Culture> dayCultures = cultures.stream()
                    .filter(c -> c.getStartDate() != null && c.getEndDate() != null
                            && !c.getStartDate().isAfter(date) && !c.getEndDate().isBefore(date))
                    .sorted(Comparator.comparingInt(Culture::getBookmarkCount).reversed()
                            .thenComparing(Culture::getTitle))
                    .toList();

            List<CultureSummary> top3 = dayCultures.stream()
                    .limit(MAX_CULTURES_PER_DAY)
                    .map(c -> new CultureSummary(c.getId(), c.getTitle(), c.getBookmarkCount(), bookmarkedIds.contains(c.getId())))
                    .toList();

            result.add(DayResponse.of(date, dayCultures.size(), top3));
            current = current.plusDays(1);
        }

        return result;
    }

    public SliceResponse<CultureListResponse> getList(LocalDate date, String keyword, String area, CultureCategory category, Pageable pageable, Long memberId) {
        Slice<Culture> slice = cultureRepository.findList(date, keyword, area, category, pageable);
        Set<Long> bookmarkedIds = getBookmarkedIds(memberId, slice.getContent());

        List<CultureListResponse> content = slice.getContent().stream()
                .map(c -> CultureListResponse.from(c, bookmarkedIds.contains(c.getId())))
                .toList();

        return SliceResponse.of(slice, content);
    }

    private Set<Long> getBookmarkedIds(Long memberId, Collection<Culture> cultures) {
        if (memberId == null || cultures.isEmpty()) return Set.of();
        List<Long> cultureIds = cultures.stream().map(Culture::getId).toList();
        return bookmarkRepository.findBookmarkedCultureIds(memberId, cultureIds);
    }
}
