package com.fourseason.delivery.fixture;

import com.fourseason.delivery.domain.order.entity.OrderMenu;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.test.util.ReflectionTestUtils;

public class OrderMenuFixture {

  private static final AtomicLong counter = new AtomicLong(1);

  public static UUID nextUUID() {
    long count = counter.getAndIncrement();
    return UUID.nameUUIDFromBytes(String.valueOf(count).getBytes(StandardCharsets.UTF_8));
  }

  public static OrderMenu createOrderMenuWithQuantity(String name, int price, int quantity) {
    OrderMenu orderMenu =
        OrderMenu.builder().name(name).price(price).quantity(quantity).build();

    ReflectionTestUtils.setField(orderMenu, "id", nextUUID());
    return orderMenu;
  }

  public static List<OrderMenu> createOrderMenuList(OrderMenu... orderMenus) {
    return List.of(orderMenus);
  }
}
