package com.fourseason.delivery.global.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
// pageRequestDto도 record로?
public class PageRequestDto {

    private final int page;

    private final int size;

    private final String order;

    @Builder
    private PageRequestDto(int page, int size, String order) {
        this.page = page;
        this.size = size;
        this.order = order;
    }

    public static PageRequestDto of(int page, int size, String order) {
        return PageRequestDto.builder()
                .page(page)
                .size(size)
                .order(order)
                .build();
    }

    public static PageRequestDto of(int page, int size) {
        return PageRequestDto.builder()
                .page(page)
                .size(size)
                .build();
    }

    public long getFirstIndex() {
        return (long) this.page * this.size;
    }
}
