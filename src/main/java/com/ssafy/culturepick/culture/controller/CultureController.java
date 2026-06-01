package com.ssafy.culturepick.culture.controller;

import com.ssafy.culturepick.auth.security.CustomMemberDetails;
import com.ssafy.culturepick.culture.domain.CultureCategory;
import com.ssafy.culturepick.culture.dto.response.CultureListResponse;
import com.ssafy.culturepick.culture.dto.response.DayResponse;
import com.ssafy.culturepick.culture.service.CultureService;
import com.ssafy.culturepick.global.common.SliceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cultures")
public class CultureController {

    private final CultureService cultureService;

    @GetMapping
    public ResponseEntity<List<DayResponse>> getCalendar(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) CultureCategory category,
            @AuthenticationPrincipal CustomMemberDetails memberDetails
    ) {
        LocalDate now = LocalDate.now();
        int y = year != null ? year : now.getYear();
        int m = month != null ? month : now.getMonthValue();
        Long memberId = memberDetails != null ? memberDetails.getId() : null;
        return ResponseEntity.ok(cultureService.getCalendar(y, m, keyword, area, category, memberId));
    }

    @GetMapping("/{date}")
    public ResponseEntity<SliceResponse<CultureListResponse>> getList(
            @PathVariable LocalDate date,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) CultureCategory category,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal CustomMemberDetails memberDetails
    ) {
        Long memberId = memberDetails != null ? memberDetails.getId() : null;
        return ResponseEntity.ok(cultureService.getList(date, keyword, area, category, pageable, memberId));
    }
}
