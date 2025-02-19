package com.fourseason.delivery.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddressRequestDto (

        @NotBlank(message = "주소는 필수 입력 값입니다.")
        String address,

        @NotBlank(message = "상세 주소는 필수 입력 값입니다.")
        String detailAddress
) {
}
