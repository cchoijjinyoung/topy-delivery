package com.fourseason.delivery.domain.order.service;

import static com.fourseason.delivery.domain.member.MemberErrorCode.MEMBER_NOT_FOUND;
import static com.fourseason.delivery.domain.member.entity.Role.CUSTOMER;
import static com.fourseason.delivery.domain.member.entity.Role.OWNER;
import static com.fourseason.delivery.domain.order.entity.OrderStatus.CANCELED;
import static com.fourseason.delivery.domain.order.entity.OrderStatus.PENDING;
import static com.fourseason.delivery.domain.order.entity.OrderType.ONLINE;
import static com.fourseason.delivery.domain.order.exception.OrderErrorCode.ORDER_NOT_FOUND;
import static com.fourseason.delivery.domain.shop.exception.ShopErrorCode.SHOP_NOT_FOUND;
import static com.fourseason.delivery.fixture.MemberFixture.createMember;
import static com.fourseason.delivery.fixture.OrderFixture.createExpiredOrder;
import static com.fourseason.delivery.fixture.OrderFixture.createOrder;
import static com.fourseason.delivery.fixture.OrderMenuFixture.createOrderMenuList;
import static com.fourseason.delivery.fixture.OrderMenuFixture.createOrderMenuWithQuantity;
import static com.fourseason.delivery.fixture.ShopFixture.createShop;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.member.repository.MemberRepository;
import com.fourseason.delivery.domain.order.dto.request.CustomerCreateOrderRequestDto;
import com.fourseason.delivery.domain.order.dto.response.impl.CustomerOrderDetailsResponseDto;
import com.fourseason.delivery.domain.order.dto.response.impl.CustomerOrderSummaryResponseDto;
import com.fourseason.delivery.domain.order.entity.Order;
import com.fourseason.delivery.domain.order.entity.OrderMenu;
import com.fourseason.delivery.domain.order.repository.OrderRepository;
import com.fourseason.delivery.domain.order.repository.OrderSearchRepositoryCustom;
import com.fourseason.delivery.domain.shop.entity.Shop;
import com.fourseason.delivery.domain.shop.repository.ShopRepository;
import com.fourseason.delivery.fixture.ShopFixture;
import com.fourseason.delivery.global.dto.PageRequestDto;
import com.fourseason.delivery.global.dto.PageResponseDto;
import com.fourseason.delivery.global.exception.CustomException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
  ShopRepository shopRepository;

  @Mock
  MemberRepository memberRepository;

  @Mock
  OrderMenuListBuilder orderMenuListBuilder;

  @Mock
  OrderRepository orderRepository;

  @Mock
  OrderSearchRepositoryCustom orderSearchRepositoryCustom;

  @InjectMocks
  OrderCustomerService orderCustomerService;

  @Nested
  class CreateOrder {

    @Test
    @DisplayName("고객 비대면 주문 요청 시, 존재하지 않는 가게면 예외가 발생한다.")
    void not_found_shop() {
      // given
      CustomerCreateOrderRequestDto request = CustomerCreateOrderRequestDto.builder()
          .shopId(UUID.randomUUID())
          .address("배달 주소")
          .instruction("요청 사항")
          .menuList(List.of())
          .build();

      Member loginMember = createMember(CUSTOMER);

      when(shopRepository.findByIdAndDeletedAtIsNull(request.shopId())).thenThrow(
          new CustomException(SHOP_NOT_FOUND));

      // when
      // then
      assertThatThrownBy(
          () -> orderCustomerService.createOnlineOrder(request, loginMember.getId()))
          .isInstanceOf(CustomException.class)
          .hasMessage("해당 가게를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("고객 비대면 주문 요청 시, 존재하지 않는 고객이면 예외가 발생한다.")
    void not_found_customer() {
      // given
      CustomerCreateOrderRequestDto request = CustomerCreateOrderRequestDto.builder()
          .shopId(UUID.randomUUID())
          .address("배달 주소")
          .instruction("요청 사항")
          .menuList(List.of())
          .build();

      Member loginMember = createMember(CUSTOMER);

      Member owner = createMember(OWNER);
      Shop shop = ShopFixture.createShop(owner);

      when(shopRepository.findByIdAndDeletedAtIsNull(request.shopId())).thenReturn(Optional.of(shop));

      when(memberRepository.findByIdAndDeletedAtIsNull(loginMember.getId())).thenThrow(
          new CustomException(MEMBER_NOT_FOUND));

      // when
      // then
      assertThatThrownBy(
          () -> orderCustomerService.createOnlineOrder(request, loginMember.getId()))
          .isInstanceOf(CustomException.class)
          .hasMessage("해당 사용자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("고객 비대면 주문 요청 성공")
    void success() {
      // given
      CustomerCreateOrderRequestDto request = CustomerCreateOrderRequestDto.builder()
          .shopId(UUID.randomUUID())
          .address("배달 주소")
          .instruction("요청 사항")
          .menuList(List.of())
          .build();

      Member loginMember = createMember(CUSTOMER);

      Member owner = createMember(OWNER);
      Shop shop = ShopFixture.createShop(owner);
      List<OrderMenu> orderMenuList = createOrderMenuList(
          createOrderMenuWithQuantity("치킨", 10000, 1),
          createOrderMenuWithQuantity("피자", 10000, 2),
          createOrderMenuWithQuantity("족발", 10000, 3)

      );
      Order order = createOrder(loginMember, shop, PENDING, ONLINE, orderMenuList);

      when(shopRepository.findByIdAndDeletedAtIsNull(request.shopId())).thenReturn(Optional.of(shop));
      when(memberRepository.findByIdAndDeletedAtIsNull(loginMember.getId())).thenReturn(Optional.of(loginMember));
      when(orderMenuListBuilder.create(request.toOrderCreateDto(shop, loginMember)))
          .thenReturn(orderMenuList);
      when(orderRepository.save(any())).thenReturn(order);

      // when
      UUID savedOrderId = orderCustomerService.createOnlineOrder(request, loginMember.getId());

      // then
      verify(orderMenuListBuilder).create(request.toOrderCreateDto(shop, loginMember));
      assertThat(savedOrderId).isEqualTo(order.getId());
    }
  }

  @Nested
  class GetOrder {

    @Test
    @DisplayName("고객이 주문 상세 조회 시, 존재하지 않는 주문이면 예외가 발생한다.")
    void order_not_found() {
      // given
      Member loginMember = createMember(CUSTOMER);
      UUID orderId = UUID.randomUUID();
      when(orderRepository.findByIdAndDeletedAtIsNull(orderId)).thenThrow(new CustomException(ORDER_NOT_FOUND));

      // when
      // then
      assertThatThrownBy(() -> orderCustomerService.getOrder(orderId, loginMember.getId()))
          .isInstanceOf(CustomException.class)
          .hasMessage("해당 주문을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("고객이 주문 상세 조회 시, 주문한 고객이 아니면 예외가 발생한다.")
    void not_ordered_by_customer() {
      // given
      Member loginMember = createMember(CUSTOMER);

      Order order = createOrder();

      // when
      when(orderRepository.findByIdAndDeletedAtIsNull(order.getId())).thenReturn(Optional.of(order));

      // then
      assertThatThrownBy(() -> orderCustomerService.getOrder(
          order.getId(), loginMember.getId()))
          .isInstanceOf(CustomException.class)
          .hasMessage("해당 주문을 요청한 고객이 아닙니다.");
    }

    @Test
    @DisplayName("고객 주문 상세 조회 성공")
    void success() {
      // given
      Member loginMember = createMember(CUSTOMER);

      Shop shop = createShop(loginMember);
      List<OrderMenu> orderMenuList = createOrderMenuList(
          createOrderMenuWithQuantity("치킨", 5000, 1),
          createOrderMenuWithQuantity("피자", 10000, 2),
          createOrderMenuWithQuantity("족발", 20000, 3)
      );

      Order order = createOrder(loginMember, shop, PENDING, ONLINE, orderMenuList);
      when(orderRepository.findByIdAndDeletedAtIsNull(order.getId())).thenReturn(Optional.of(order));

      // when
      CustomerOrderDetailsResponseDto response = orderCustomerService.getOrder(order.getId(),
          loginMember.getId());

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

  @Nested
  class GetOrderList {

    @Test
    @DisplayName("고객 주문 목록 조회 시, 회원 본인의 주문 목록이 아니면 예외가 발생한다.")
    void not_ordered_by_customer() {
      // given
      String username = "want";
      String loginMemberUsername = "me";
      PageRequestDto pageRequestDto = PageRequestDto.of(0, 10, "latest");
      String keyword = "sampleKeyword";

      // when
      // then
      assertThatThrownBy(() -> orderCustomerService.getOrderList(
          username, loginMemberUsername, pageRequestDto, keyword))
          .isInstanceOf(CustomException.class)
          .hasMessage("주문 조회 권한이 없습니다.");
    }

    @Test
    @DisplayName("고객 주문 목록 조회 성공")
    void success() {
      // given
      String username = "want";
      String loginMemberUsername = "want";
      PageRequestDto pageRequestDto = PageRequestDto.of(0, 10, "latest");
      String keyword = "sampleKeyword";

      PageResponseDto<CustomerOrderSummaryResponseDto> expectedResponse =
          new PageResponseDto<>(Collections.singletonList(
              new CustomerOrderSummaryResponseDto(createOrder())
          ), 1L);

      when(orderSearchRepositoryCustom.findByCustomerWithPage(username, pageRequestDto, keyword))
          .thenReturn(expectedResponse);

      // when
      PageResponseDto<CustomerOrderSummaryResponseDto> actualResponse =
          orderCustomerService.getOrderList(username, loginMemberUsername, pageRequestDto, keyword);

      // then
      assertThat(expectedResponse).isEqualTo(actualResponse);
      verify(orderSearchRepositoryCustom).findByCustomerWithPage(username, pageRequestDto, keyword);
    }
  }

  @Nested
  class CancelOrder {

    @Test
    @DisplayName("고객이 주문 취소 시, 존재하지 않는 주문이면 예외가 발생한다.")
    void order_not_found() {
      // given
      Member loginMember = createMember(CUSTOMER);
      UUID orderId = UUID.randomUUID();
      when(orderRepository.findByIdAndDeletedAtIsNull(orderId)).thenThrow(new CustomException(ORDER_NOT_FOUND));

      // when
      // then
      assertThatThrownBy(() -> orderCustomerService.cancelOrder(orderId, loginMember.getId()))
          .isInstanceOf(CustomException.class)
          .hasMessage("해당 주문을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("고객이 주문 취소 시, 해당 주문을 요청한 고객이 아니면 예외가 발생한다.")
    void not_ordered_by_customer() {
      // given
      Member loginMember = createMember(CUSTOMER);

      Member owner = createMember(OWNER);
      Shop shop = createShop(owner);
      List<OrderMenu> orderMenuList = createOrderMenuList(
          createOrderMenuWithQuantity("치킨", 5000, 1),
          createOrderMenuWithQuantity("피자", 10000, 2),
          createOrderMenuWithQuantity("족발", 20000, 3)
      );

      Member another = createMember(CUSTOMER);
      Order order = createOrder(another, shop, PENDING, ONLINE, orderMenuList);
      when(orderRepository.findByIdAndDeletedAtIsNull(order.getId())).thenReturn(Optional.of(order));

      // when
      // then
      assertThatThrownBy(() -> orderCustomerService.cancelOrder(order.getId(), loginMember.getId()))
          .isInstanceOf(CustomException.class)
          .hasMessage("해당 주문을 요청한 고객이 아닙니다.");
    }

    @Test
    @DisplayName("고객이 주문 취소 시, 이미 취소한 주문이면 예외가 발생한다.")
    void already_canceled_order() {
      // given
      Member loginMember = createMember(CUSTOMER);

      Member owner = createMember(OWNER);
      Shop shop = createShop(owner);
      List<OrderMenu> orderMenuList = createOrderMenuList(
          createOrderMenuWithQuantity("치킨", 5000, 1),
          createOrderMenuWithQuantity("피자", 10000, 2),
          createOrderMenuWithQuantity("족발", 20000, 3)
      );

      Order canceledOrder = createOrder(loginMember, shop, CANCELED, ONLINE, orderMenuList);
      when(orderRepository.findByIdAndDeletedAtIsNull(canceledOrder.getId())).thenReturn(Optional.of(canceledOrder));

      // when
      // then
      assertThatThrownBy(
          () -> orderCustomerService.cancelOrder(canceledOrder.getId(), loginMember.getId()))
          .isInstanceOf(CustomException.class)
          .hasMessage("이미 취소된 주문입니다.");
    }

    @Test
    @DisplayName("고객이 주문 취소 시, 주문 취소 기간이 지났으면 예외가 발생한다.")
    void order_cancel_expired() {
      // given
      Member loginMember = createMember(CUSTOMER);

      Member owner = createMember(OWNER);
      Shop shop = createShop(owner);
      List<OrderMenu> orderMenuList = createOrderMenuList(
          createOrderMenuWithQuantity("치킨", 5000, 1),
          createOrderMenuWithQuantity("피자", 10000, 2),
          createOrderMenuWithQuantity("족발", 20000, 3)
      );

      Order order = createExpiredOrder(loginMember, shop, PENDING, ONLINE, orderMenuList);

      when(orderRepository.findByIdAndDeletedAtIsNull(order.getId())).thenReturn(Optional.of(order));

      // when
      // then
      assertThatThrownBy(() -> orderCustomerService.cancelOrder(order.getId(), loginMember.getId()))
          .isInstanceOf(CustomException.class)
          .hasMessage("주문 취소 기간이 만료되었습니다.");
    }

    @Test
    @DisplayName("고객의 요청 주문 취소 성공")
    void success() {
      // given
      Member loginMember = createMember(CUSTOMER);

      Member owner = createMember(OWNER);
      Shop shop = createShop(owner);
      List<OrderMenu> orderMenuList = createOrderMenuList(
          createOrderMenuWithQuantity("치킨", 5000, 1),
          createOrderMenuWithQuantity("피자", 10000, 2),
          createOrderMenuWithQuantity("족발", 20000, 3)
      );

      Order order = createOrder(loginMember, shop, PENDING, ONLINE, orderMenuList);

      when(orderRepository.findByIdAndDeletedAtIsNull(order.getId())).thenReturn(Optional.of(order));

      // when
      orderCustomerService.cancelOrder(order.getId(), loginMember.getId());

      // then
      assertThat(order.getOrderStatus()).isEqualTo(CANCELED);
    }
  }
}