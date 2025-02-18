package com.fourseason.delivery.domain.menu.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record UpdateMenuRequestDto(
        @NotBlank(message = "메뉴 이름은 필수 입력 값입니다.")
        String name,

        @NotBlank(message = "메뉴 설명은 필수 입력 값입니다.")
        String description,

        @NotNull(message = "메뉴 가격은 필수 입력 값입니다.")
        @PositiveOrZero(message = "메뉴 가격은 음수일 수 없습니다.")
        int price
) {
}
