package com.fourseason.delivery.domain.shop.dto.response;

import com.fourseason.delivery.domain.shop.entity.Shop;
import lombok.Builder;

import java.util.List;

@Builder
public record ShopResponseDto(

        String id,
        String name,

        String description,

        String tel,

        String address,

        String detailAddress,

        List<String> images,

        double reviewAvg
) {
    public static ShopResponseDto of(Shop shop, List<String> images, double reviewAvg) {
        return ShopResponseDto.builder()
            .id(shop.getId().toString())
            .name(shop.getName())
            .description(shop.getDescription())
            .tel(shop.getTel())
            .address(shop.getAddress())
            .detailAddress(shop.getDetailAddress())
            .images(images)
            .reviewAvg(reviewAvg)
            .build();
    }
}
