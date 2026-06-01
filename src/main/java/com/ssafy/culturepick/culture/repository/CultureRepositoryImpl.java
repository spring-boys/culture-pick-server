package com.ssafy.culturepick.culture.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ssafy.culturepick.culture.domain.Culture;
import com.ssafy.culturepick.culture.domain.CultureCategory;
import com.ssafy.culturepick.culture.domain.QCulture;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
public class CultureRepositoryImpl implements CultureRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private static final QCulture culture = QCulture.culture;

    @Override
    public List<Culture> findCalendar(LocalDate rangeStart, LocalDate rangeEnd, String keyword, String area, CultureCategory category) {
        return queryFactory.selectFrom(culture)
                .where(
                        culture.startDate.loe(rangeEnd),
                        culture.endDate.goe(rangeStart),
                        keywordContains(keyword),
                        areaEq(area),
                        categoryEq(category)
                )
                .fetch();
    }

    @Override
    public Slice<Culture> findList(LocalDate date, String keyword, String area, CultureCategory category, Pageable pageable) {
        List<Culture> results = queryFactory.selectFrom(culture)
                .where(
                        dateContains(date),
                        keywordContains(keyword),
                        areaEq(area),
                        categoryEq(category)
                )
                .orderBy(culture.bookmarkCount.desc(), culture.title.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = results.size() > pageable.getPageSize();
        if (hasNext) results.remove(results.size() - 1);

        return new SliceImpl<>(results, pageable, hasNext);
    }

    private BooleanExpression dateContains(LocalDate date) {
        return date != null ? culture.startDate.loe(date).and(culture.endDate.goe(date)) : null;
    }

    private BooleanExpression keywordContains(String keyword) {
        return keyword != null && !keyword.isBlank() ? culture.title.containsIgnoreCase(keyword) : null;
    }

    private BooleanExpression areaEq(String area) {
        return area != null && !area.isBlank() ? culture.area.eq(area) : null;
    }

    private BooleanExpression categoryEq(CultureCategory category) {
        return category != null ? culture.category.eq(category) : null;
    }
}
