package com.fourseason.delivery.domain.menu.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateMenuRequestDto(
        @NotBlank(message = "메뉴 이름은 필수 입력 값입니다.")
        String name,

        @NotBlank(message = "메뉴 설명은 필수 입력 값입니다.")
        String description,

        @NotNull(message = "메뉴 가격은 필수 입력 값입니다.")
        int price,

        @NotBlank(message = "가게 id값은 필수 입력 값입니다.")
        String shopId
) {
}
