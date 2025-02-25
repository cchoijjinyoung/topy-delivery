package com.fourseason.delivery.global.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class FilterRequestDto {

    private final String category;

    private final String region;

    @Builder
    private FilterRequestDto(String category, String region) {
        this.category = category;
        this.region = region;
    }

    public static FilterRequestDto of(String category, String region) {
        return FilterRequestDto.builder()
            .category(category)
            .region(region)
            .build();
    }
}
