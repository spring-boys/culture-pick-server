package com.ssafy.culturepick.global.common;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@NoArgsConstructor
public class PageResponse<T> {

    private Integer totalPages;
    private Integer currentPage;
    private List<T> content;

    @Builder
    private PageResponse(Integer totalPages, Integer currentPage, List<T> content) {
        this.totalPages = totalPages;
        this.currentPage = currentPage;
        this.content = content;
    }

    public static <T> PageResponse<T> of(Page<?> page, List<T> content) {
        return PageResponse.<T>builder()
                .totalPages(page.getTotalPages())
                .currentPage(page.getNumber())
                .content(content)
                .build();
    }

    public static <T> PageResponse<T> empty() {
        return PageResponse.<T>builder()
                .totalPages(0)
                .currentPage(0)
                .content(List.of())
                .build();
    }
}
