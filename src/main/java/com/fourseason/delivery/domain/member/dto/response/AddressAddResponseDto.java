package com.fourseason.delivery.domain.member.dto.response;

import com.fourseason.delivery.domain.member.entity.Address;
import lombok.Builder;

@Builder
public record AddressAddResponseDto(
        String address,
        String detailAddress
) {
    public static AddressAddResponseDto of(Address address) {
        return AddressAddResponseDto.builder()
                .address(address.getAddress())
                .detailAddress(address.getDetailAddress())
                .build();
    }
}
