package com.fourseason.delivery.fixture;

import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.shop.entity.Shop;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.test.util.ReflectionTestUtils;

public class ShopFixture {

  private static final AtomicLong counter = new AtomicLong(1);

  public static UUID nextUUID() {
    long count = counter.getAndIncrement();
    return UUID.nameUUIDFromBytes(String.valueOf(count).getBytes(StandardCharsets.UTF_8));
  }

  public static Shop createShop(Member member) {
    Shop shop = Shop.builder()
        .name("가게 이름")
        .description("가게 설명")
        .tel("010-1234-5678")
        .address("가게 주소")
        .detailAddress("가게 상세 주소")
        .member(member)
        .build();

    ReflectionTestUtils.setField(shop, "id", nextUUID());
    return shop;
  }
}
