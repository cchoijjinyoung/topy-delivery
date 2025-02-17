package com.fourseason.delivery.domain.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record CreateOrderRequestDto(
    @NotBlank(message = "가게 id는 필수 입력 값입니다.")
    UUID shopId,

    @NotBlank(message = "주소는 필수 입력 값입니다.")
    String address,

    @NotEmpty(message = "주문 상품이 없습니다.")
    List<MenuDTO> menuList,

    String instruction

) {

  @Builder
  public record MenuDTO(
      @NotBlank
      UUID menuId,

      @Positive
      int quantity
  ) {

  }
}
