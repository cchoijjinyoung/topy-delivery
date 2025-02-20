package com.fourseason.delivery.domain.member.dto.response;

import com.fourseason.delivery.domain.member.entity.Address;
import lombok.Builder;

@Builder
public record AddressUpdateResponseDto(
        String address,
        String detailAddress
) {
    public static AddressUpdateResponseDto of(Address address) {
        return AddressUpdateResponseDto.builder()
                .address(address.getAddress())
                .detailAddress(address.getDetailAddress())
                .build();
    }
}
