package com.fourseason.delivery.domain.menu.dto.response;

import com.fourseason.delivery.domain.menu.entity.Menu;
import lombok.Builder;

import java.util.List;

@Builder
public record MenuResponseDto(
        String id,
        String name,
        String description,
        int price,
        List<String> images
) {

    public static MenuResponseDto of(Menu menu, List<String> images) {
        return MenuResponseDto.builder()
            .id(menu.getId().toString())
            .name(menu.getName())
            .description(menu.getDescription())
            .price(menu.getPrice())
            .images(images)
            .build();
    }
}
