package com.fourseason.delivery.domain.order.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.stream.Stream;

public enum OrderStatus {
  PENDING,
  ACCEPTED,
  DELIVERING,
  COMPLETED,
  CANCELED;

  public static OrderStatus of(String status) {
    return valueOf(status);
  }

  @JsonCreator
  public static OrderStatus parsing(String inputValue) {
    return Stream.of(OrderStatus.values())
        .filter(orderStatus -> orderStatus.toString().equals(inputValue.toUpperCase()))
        .findFirst()
        .orElse(null);
  }
}
