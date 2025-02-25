package com.fourseason.delivery.domain.order.dto.response.impl;

import static java.util.stream.Collectors.toList;

import com.fourseason.delivery.domain.order.dto.response.OrderDetailsResponseDto;
import com.fourseason.delivery.domain.order.entity.Order;
import com.fourseason.delivery.domain.order.entity.OrderMenu;
import com.fourseason.delivery.domain.order.entity.OrderStatus;
import com.fourseason.delivery.domain.order.entity.OrderType;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

public record CustomerOrderDetailsResponseDto(
    String shopName,
    String address,
    String instruction,
    Integer totalPrice,
    OrderStatus status,
    OrderType type,
    List<MenuDto> menuList,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String updatedBy
) implements OrderDetailsResponseDto {

  public CustomerOrderDetailsResponseDto(Order order) {
    this(order.getShop().getName(),
        order.getAddress(),
        order.getInstruction(),
        order.getTotalPrice(),
        order.getOrderStatus(),
        order.getOrderType(),
        order.getOrderMenuList().stream().map(MenuDto::of).collect(toList()),
        order.getCreatedAt(),
        order.getUpdatedAt(),
        order.getUpdatedBy()
    );
  }

  public static CustomerOrderDetailsResponseDto of(Order order) {
    return new CustomerOrderDetailsResponseDto(
        order.getShop().getName(),
        order.getAddress(),
        order.getInstruction(),
        order.getTotalPrice(),
        order.getOrderStatus(),
        order.getOrderType(),
        order.getOrderMenuList().stream().map(MenuDto::of).toList(),
        order.getCreatedAt(),
        order.getUpdatedAt(),
        order.getUpdatedBy()
    );
  }

  @Builder
  public record MenuDto(
      String name,
      Integer price,
      Integer quantity,
      Integer totalPrice
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
