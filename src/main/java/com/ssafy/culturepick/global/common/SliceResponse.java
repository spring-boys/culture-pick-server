package com.ssafy.culturepick.global.common;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Slice;

import java.util.List;

@Getter
@NoArgsConstructor
public class SliceResponse<T> {

    private Integer currentPage;
    private Boolean hasNext;
    private List<T> content;

    @Builder
    private SliceResponse(Integer currentPage, Boolean hasNext, List<T> content) {
        this.currentPage = currentPage;
        this.hasNext = hasNext;
        this.content = content;
    }

    public static <T> SliceResponse<T> of(Slice<?> slice, List<T> content) {
        return SliceResponse.<T>builder()
                .currentPage(slice.getNumber())
                .hasNext(slice.hasNext())
                .content(content)
                .build();
    }

    public static <T> SliceResponse<T> empty() {
        return SliceResponse.<T>builder()
                .currentPage(0)
                .hasNext(false)
                .content(List.of())
                .build();
    }
}
