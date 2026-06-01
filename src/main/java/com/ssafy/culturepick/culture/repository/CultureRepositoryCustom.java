package com.ssafy.culturepick.culture.repository;

import com.ssafy.culturepick.culture.domain.Culture;
import com.ssafy.culturepick.culture.domain.CultureCategory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.time.LocalDate;
import java.util.List;

public interface CultureRepositoryCustom {

    List<Culture> findCalendar(LocalDate rangeStart, LocalDate rangeEnd, String keyword, String area, CultureCategory category);

    Slice<Culture> findList(LocalDate date, String keyword, String area, CultureCategory category, Pageable pageable);
}
