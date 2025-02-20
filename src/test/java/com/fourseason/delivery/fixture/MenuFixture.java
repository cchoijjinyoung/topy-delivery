package com.fourseason.delivery.fixture;

import com.fourseason.delivery.domain.menu.entity.Menu;
import com.fourseason.delivery.domain.shop.entity.Shop;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.test.util.ReflectionTestUtils;

public class MenuFixture {

  private static final AtomicLong counter = new AtomicLong(1);

  public static UUID nextUUID() {
    long count = counter.getAndIncrement();
    return UUID.nameUUIDFromBytes(String.valueOf(count).getBytes(StandardCharsets.UTF_8));
  }

  public static Menu createMenu(Shop shop, String name, int price) {
    Menu menu =
        Menu.builder()
            .shop(shop)
            .name(name).price(price).build();

    ReflectionTestUtils.setField(menu, "id", nextUUID());
    return menu;
  }

  public static List<Menu> createMenuList(Menu... menus) {
    return List.of(menus);
  }

}
