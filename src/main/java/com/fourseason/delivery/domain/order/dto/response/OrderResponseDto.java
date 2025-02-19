package com.fourseason.delivery.domain.order.dto.response;

import static java.util.stream.Collectors.toList;

import com.fourseason.delivery.domain.order.entity.Order;
import com.fourseason.delivery.domain.order.entity.OrderMenu;
import com.fourseason.delivery.domain.order.entity.OrderStatus;
import com.fourseason.delivery.domain.order.entity.OrderType;
import java.util.List;
import lombok.Builder;

@Builder
public record OrderResponseDto(
    String shopName,
    String address,
    String instruction,
    int totalPrice,
    OrderStatus status,
    OrderType type,
    List<MenuDto> menuList
) {

  public static OrderResponseDto of(Order order) {
    return OrderResponseDto.builder()
        .shopName(order.getShop().getName())
        .address(order.getAddress())
        .instruction(order.getInstruction())
        .totalPrice(order.getTotalPrice())
        .status(order.getOrderStatus())
        .type(order.getOrderType())
        .menuList(order.getOrderMenuList().stream().map(MenuDto::of).collect(toList()))
        .build();
  }

  @Builder
  public record MenuDto(
      String name,
      int price,
      int quantity,
      int totalPrice
  ) {

    public static MenuDto of(OrderMenu orderMenu) {
      return MenuDto.builder()
          .name(orderMenu.getName())
          .price(orderMenu.getPrice())
          .quantity(orderMenu.getQuantity())
          .totalPrice(orderMenu.getPrice() * orderMenu.getQuantity())
          .build();
    }
  }
}
