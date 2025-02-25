package com.fourseason.delivery.domain.order.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.stream.Stream;

public enum OrderType {
  ONLINE,
  OFFLINE;

  public static OrderType of(String status) {
    return valueOf(status);
  }

  @JsonCreator
  public static OrderType parsing(String inputValue) {
    return Stream.of(OrderType.values())
        .filter(orderType -> orderType.toString().equals(inputValue.toUpperCase()))
        .findFirst()
        .orElse(null);
  }
}
