package com.fourseason.delivery.domain.order.dto.request;

import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.member.entity.Role;
import com.fourseason.delivery.domain.order.entity.OrderStatus;
import com.fourseason.delivery.domain.order.entity.OrderType;
import com.fourseason.delivery.domain.shop.entity.Shop;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record OrderCreateDto(
    List<OrderMenuCreateDto> orderMenuCreateDtoList,
    Role byRole,
    Shop shop,
    Member customer,
    String address,
    String instruction,
    OrderStatus status,
    OrderType type
) {

  @Builder
  public record OrderMenuCreateDto(
      UUID menuId,
      int quantity
  ) {

  }
}
