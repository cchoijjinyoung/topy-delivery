package com.fourseason.delivery.domain.order.service;

import static com.fourseason.delivery.domain.member.entity.Role.CUSTOMER;
import static com.fourseason.delivery.domain.member.entity.Role.OWNER;
import static com.fourseason.delivery.domain.order.entity.OrderStatus.PENDING;
import static com.fourseason.delivery.domain.order.entity.OrderType.ONLINE;
import static com.fourseason.delivery.domain.order.exception.OrderErrorCode.MEMBER_NOT_FOUND;
import static com.fourseason.delivery.domain.order.exception.OrderErrorCode.ORDER_NOT_FOUND;
import static com.fourseason.delivery.domain.shop.exception.ShopErrorCode.SHOP_NOT_FOUND;
import static com.fourseason.delivery.fixture.MemberFixture.createMember;
import static com.fourseason.delivery.fixture.OrderFixture.createOrder;
import static com.fourseason.delivery.fixture.OrderMenuFixture.createOrderMenuList;
import static com.fourseason.delivery.fixture.OrderMenuFixture.createOrderMenuWithQuantity;
import static com.fourseason.delivery.fixture.ShopFixture.createShop;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.member.repository.MemberRepository;
import com.fourseason.delivery.domain.menu.repository.MenuRepository;
import com.fourseason.delivery.domain.order.dto.response.OrderResponseDto;
import com.fourseason.delivery.domain.order.dto.request.CreateOrderRequestDto;
import com.fourseason.delivery.domain.order.dto.request.CreateOrderRequestDto.MenuDto;
import com.fourseason.delivery.domain.order.entity.Order;
import com.fourseason.delivery.domain.order.entity.OrderMenu;
import com.fourseason.delivery.domain.order.repository.OrderRepository;
import com.fourseason.delivery.domain.shop.entity.Shop;
import com.fourseason.delivery.domain.shop.repository.ShopRepository;
import com.fourseason.delivery.global.exception.CustomException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderCustomerServiceTest {

  @Mock
  OrderRepository orderRepository;

  @Mock
  ShopRepository shopRepository;

  @Mock
  MemberRepository memberRepository;

  @Mock
  MenuRepository menuRepository;

  @InjectMocks
  OrderCustomerService orderCustomerService;

  @Nested
  class createOrderTest {

    MenuDto menuDto1;
    List<MenuDto> menuList;
    CreateOrderRequestDto request;
    Long memberId;

    @BeforeEach
    void setup() {
      menuDto1 = MenuDto.builder()
          .menuId(UUID.randomUUID())
          .quantity(2)
          .build();

      menuList = new ArrayList<>();
      menuList.add(menuDto1);

      request = CreateOrderRequestDto.builder()
          .shopId(UUID.randomUUID())
          .instruction("단무지 안주셔도 돼요!")
          .menuList(menuList)
          .build();

      memberId = 1L;
    }

    @Test
    @DisplayName("주문 요청 시 존재하지 않은 회원이면, 예외가 발생한다.")
    void user_not_found() {
      // given
      when(memberRepository.findById(memberId)).thenThrow(new CustomException(MEMBER_NOT_FOUND));

      // when
      // then
      assertThatThrownBy(
          () -> orderCustomerService.createOrder(request, memberId))
          .isInstanceOf(CustomException.class)
          .hasMessage("해당 회원을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("주문 요청 시 존재하지 않은 가게면, 예외가 발생한다.")
    void shop_not_found() {
      // given
      Member member = createMember(CUSTOMER);

      when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
      when(shopRepository.findById(request.shopId())).thenThrow(
          new CustomException(SHOP_NOT_FOUND));

      // when
      // then
      assertThatThrownBy(
          () -> orderCustomerService.createOrder(request, memberId))
          .isInstanceOf(CustomException.class)
          .hasMessage("해당 가게를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("주문 요청 시 존재하지 않은 메뉴면, 예외가 발생한다.")
    void menu_not_found() {
      // given
      Member member = createMember(CUSTOMER);
      Member owner = createMember(OWNER);
      Shop shop = createShop(owner);

      when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
      when(shopRepository.findById(request.shopId())).thenReturn(
          Optional.of(shop));
      when(menuRepository.findByIdInAndDeletedAtIsNull(List.of(request.menuList().get(0).menuId())))
          .thenReturn(List.of());

      // when
      // then
      assertThatThrownBy(() -> orderCustomerService.createOrder(request, memberId))
          .isInstanceOf(CustomException.class)
          .hasMessage("해당 메뉴를 찾을 수 없습니다.");
    }
  }

  @Nested
  class getOrder {

    UUID orderId = UUID.randomUUID();

    @Test
    @DisplayName("고객 단건 주문 조회 시, 존재하지 않는 주문이면 예외가 발생한다.")
    void order_not_found() {
      // given
      when(orderRepository.findById(orderId)).thenThrow(new CustomException(ORDER_NOT_FOUND));

      // when
      // then
      assertThatThrownBy(() -> orderCustomerService.getOrder(orderId))
          .isInstanceOf(CustomException.class)
          .hasMessage("해당 주문을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("고객 단건 주문 조회 성공")
    void success() {
      // given
      Member member = createMember(CUSTOMER);
      Member owner = createMember(OWNER);
      Shop shop = createShop(owner);

      List<OrderMenu> orderMenuList = createOrderMenuList(
          createOrderMenuWithQuantity("치킨", 5000, 1),
          createOrderMenuWithQuantity("피자", 10000, 2),
          createOrderMenuWithQuantity("족발", 20000, 3)
      );

      Order order = createOrder(member, shop, PENDING, ONLINE, orderMenuList);

      when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

      // when
      OrderResponseDto response = orderCustomerService.getOrder(orderId);

      // then
      assertThat(response.shopName()).isEqualTo(order.getShop().getName());
      assertThat(response.address()).isEqualTo(order.getAddress());
      assertThat(response.instruction()).isEqualTo(order.getInstruction());
      assertThat(response.totalPrice()).isEqualTo(order.getTotalPrice());
      assertThat(response.status()).isEqualTo(PENDING);
      assertThat(response.type()).isEqualTo(ONLINE);
      assertThat(response.menuList().get(0).name()).isEqualTo(
          order.getOrderMenuList().get(0).getName());
      assertThat(response.menuList().get(0).price()).isEqualTo(
          order.getOrderMenuList().get(0).getPrice());
      assertThat(response.menuList().get(0).quantity()).isEqualTo(
          order.getOrderMenuList().get(0).getQuantity());
    }
  }
}