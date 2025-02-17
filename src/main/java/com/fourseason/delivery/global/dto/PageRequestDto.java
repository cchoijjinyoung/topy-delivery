package com.fourseason.delivery.global.dto;

import com.fourseason.delivery.global.dto.exception.PageErrorCode;
import com.fourseason.delivery.global.exception.CustomException;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PageRequestDto {

    private final int page;

    private final int size;

    private final String order;

    @Builder
    public PageRequestDto(int page, int size, String order) {
        validateSize(size);
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

    private void validateSize(int size) {
        if (size % 10 != 0) {
            throw new CustomException(PageErrorCode.SIZE_BAD_REQUEST);
        }
    }
}
