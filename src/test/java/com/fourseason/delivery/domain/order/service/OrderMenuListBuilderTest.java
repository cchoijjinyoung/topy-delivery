package com.fourseason.delivery.domain.order.service;

import static com.fourseason.delivery.domain.member.entity.Role.CUSTOMER;
import static com.fourseason.delivery.domain.member.entity.Role.OWNER;
import static com.fourseason.delivery.domain.menu.entity.MenuStatus.SHOW;
import static com.fourseason.delivery.domain.order.entity.OrderStatus.PENDING;
import static com.fourseason.delivery.domain.order.entity.OrderType.ONLINE;
import static com.fourseason.delivery.fixture.MemberFixture.createMember;
import static com.fourseason.delivery.fixture.MenuFixture.createMenu;
import static com.fourseason.delivery.fixture.ShopFixture.createShop;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.menu.entity.Menu;
import com.fourseason.delivery.domain.menu.repository.MenuRepository;
import com.fourseason.delivery.domain.order.dto.request.OrderCreateDto;
import com.fourseason.delivery.domain.order.dto.request.OrderCreateDto.OrderMenuCreateDto;
import com.fourseason.delivery.domain.order.entity.OrderMenu;
import com.fourseason.delivery.domain.shop.entity.Shop;
import com.fourseason.delivery.global.exception.CustomException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderMenuListBuilderTest {

  @Mock
  private MenuRepository menuRepository;

  @InjectMocks
  private OrderMenuListBuilder orderMenuListBuilder;

  @Nested
  class Create {

    @Test
    @DisplayName("주문 메뉴 리스트 생성 시, 존재하지 않는 메뉴면 실패한다.")
    void not_found_menu() {
      // given
      Member customer = createMember(CUSTOMER);

      Member owner = createMember(OWNER);
      Shop shop = createShop(owner);

      // 메뉴를 하나 주문 했을 때,
      OrderCreateDto dto = OrderCreateDto.builder()
          .orderMenuCreateDtoList(
              List.of(
                  OrderMenuCreateDto.builder()
                      .menuId(UUID.randomUUID())
                      .build()
              )
          )
          .shop(shop)
          .type(ONLINE)
          .byRole(CUSTOMER)
          .status(PENDING)
          .customer(customer)
          .instruction("요청사항 입니다.")
          .address("배달 주소")
          .build();

      // DB에서 찾아온 메뉴 리스트가 비어있을 시,
      when(menuRepository.findByIdInAndMenuStatusAndShopAndDeletedAtIsNull(
          dto.orderMenuCreateDtoList().stream()
              .map(OrderMenuCreateDto::menuId).toList(), SHOW, shop)).thenReturn(List.of());

      // when
      // then
      assertThatThrownBy(
          () -> orderMenuListBuilder.create(dto))
          .isInstanceOf(CustomException.class)
          .hasMessage("해당 메뉴를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("주문 메뉴 리스트 생성 성공")
    void success() {
      // given
      Member customer = createMember(CUSTOMER);

      Member owner = createMember(OWNER);
      Shop shop = createShop(owner);

      Menu menu1 = createMenu(shop, "치킨", 10000);
      Menu menu2 = createMenu(shop, "피자", 10000);
      Menu menu3 = createMenu(shop, "족발", 10000);

      OrderCreateDto dto = OrderCreateDto.builder()
          .orderMenuCreateDtoList(
              List.of(
                  OrderMenuCreateDto.builder()
                      .menuId(menu1.getId())
                      .quantity(1)
                      .build(),
                  OrderMenuCreateDto.builder()
                      .menuId(menu2.getId())
                      .quantity(2)
                      .build(),
                  OrderMenuCreateDto.builder()
                      .menuId(menu3.getId())
                      .quantity(3)
                      .build()
              )
          )
          .shop(shop)
          .type(ONLINE)
          .byRole(CUSTOMER)
          .status(PENDING)
          .customer(customer)
          .instruction("요청사항 입니다.")
          .address("배달 주소")
          .build();

      when(menuRepository.findByIdInAndMenuStatusAndShopAndDeletedAtIsNull(
          dto.orderMenuCreateDtoList().stream()
              .map(OrderMenuCreateDto::menuId).toList(), SHOW, shop))
          .thenReturn(List.of(menu1, menu2, menu3));

      List<OrderMenu> orderMenuList = orderMenuListBuilder.create(dto);
      assertThat(orderMenuList.get(0).getName()).isEqualTo("치킨");
      assertThat(orderMenuList.get(0).getTotalPrice()).isEqualTo(10000);
      assertThat(orderMenuList.get(0).getQuantity()).isEqualTo(1);

      assertThat(orderMenuList.get(1).getName()).isEqualTo("피자");
      assertThat(orderMenuList.get(1).getTotalPrice()).isEqualTo(20000);
      assertThat(orderMenuList.get(1).getQuantity()).isEqualTo(2);

      assertThat(orderMenuList.get(2).getName()).isEqualTo("족발");
      assertThat(orderMenuList.get(2).getTotalPrice()).isEqualTo(30000);
      assertThat(orderMenuList.get(2).getQuantity()).isEqualTo(3);
    }
  }
}