package com.fourseason.delivery.domain.member.dto.response;

import com.fourseason.delivery.domain.member.entity.Address;
import lombok.Builder;

@Builder
public record AddressResponseDto (
        String address,
        String detailAddress
) {
    public static AddressResponseDto of(Address address) {
        return AddressResponseDto.builder()
                .address(address.getAddress())
                .detailAddress(address.getDetailAddress())
                .build();
    }
}
