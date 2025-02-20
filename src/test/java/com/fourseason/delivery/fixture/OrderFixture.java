package com.fourseason.delivery.fixture;

import static com.fourseason.delivery.domain.member.entity.Role.*;

import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.order.entity.Order;
import com.fourseason.delivery.domain.order.entity.OrderMenu;
import com.fourseason.delivery.domain.order.entity.OrderStatus;
import com.fourseason.delivery.domain.order.entity.OrderType;
import com.fourseason.delivery.domain.shop.entity.Shop;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.test.util.ReflectionTestUtils;

public class OrderFixture {

  private static final AtomicLong counter = new AtomicLong(1);

  public static UUID nextUUID() {
    long count = counter.getAndIncrement();
    return UUID.nameUUIDFromBytes(String.valueOf(count).getBytes(StandardCharsets.UTF_8));
  }

  public static Order createOrder(Member member, Shop shop, OrderStatus status, OrderType type, List<OrderMenu> orderMenuList) {
    int totalPrice = 0;
    for (OrderMenu orderMenu : orderMenuList) {
      totalPrice += orderMenu.getPrice();
    }

    Order order = Order.builder()
        .shop(shop)
        .member(member)
        .orderMenuList(orderMenuList)
        .orderStatus(status)
        .orderType(type)
        .address("서울 어딘가")
        .instruction("주문 요청 사항입니다.")
        .totalPrice(totalPrice)
        .build();

    ReflectionTestUtils.setField(order, "id", nextUUID());
    return order;
  }
}
