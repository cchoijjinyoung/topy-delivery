package com.fourseason.delivery.domain.order.service;

import static com.fourseason.delivery.domain.member.entity.Role.CUSTOMER;
import static com.fourseason.delivery.domain.member.entity.Role.MANAGER;
import static com.fourseason.delivery.domain.member.entity.Role.OWNER;
import static com.fourseason.delivery.domain.member.exception.MemberErrorCode.MEMBER_NOT_FOUND;
import static com.fourseason.delivery.domain.order.entity.OrderStatus.CANCELED;
import static com.fourseason.delivery.domain.order.entity.OrderStatus.PENDING;
import static com.fourseason.delivery.domain.order.entity.OrderType.ONLINE;
import static com.fourseason.delivery.domain.order.exception.OrderErrorCode.ORDER_NOT_FOUND;
import static com.fourseason.delivery.domain.shop.exception.ShopErrorCode.SHOP_NOT_FOUND;
import static com.fourseason.delivery.fixture.MemberFixture.createMember;
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
import com.fourseason.delivery.domain.order.dto.request.ManagerCreateOrderRequestDto;
import com.fourseason.delivery.domain.order.dto.response.impl.ManagerOrderDetailsResponseDto;
import com.fourseason.delivery.domain.order.dto.response.impl.ManagerOrderSummaryResponseDto;
import com.fourseason.delivery.domain.order.entity.Order;
import com.fourseason.delivery.domain.order.entity.OrderMenu;
import com.fourseason.delivery.domain.order.repository.OrderRepository;
import com.fourseason.delivery.domain.order.repository.OrderSearchRepositoryCustom;
import com.fourseason.delivery.domain.shop.entity.Shop;
import com.fourseason.delivery.domain.shop.repository.ShopRepository;
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
class OrderManagerServiceTest {

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private ShopRepository shopRepository;

  @Mock
  private OrderMenuListBuilder orderMenuListBuilder;

  @Mock
  private OrderRepository orderRepository;

  @Mock
  private OrderSearchRepositoryCustom orderSearchRepositoryCustom;

  @InjectMocks
  private OrderManagerService orderManagerService;

  @Nested
  class CreateOrder {

    @Test
    @DisplayName("주문 생성 시, 존재하지 않는 가게면 예외가 발생한다.")
    void not_found_shop() {
      // given
      ManagerCreateOrderRequestDto request = ManagerCreateOrderRequestDto.builder()
          .shopId(UUID.randomUUID())
          .address("배달 주소")
          .instruction("요청 사항")
          .menuList(List.of())
          .build();

      when(shopRepository.findByIdAndDeletedAtIsNull(request.shopId())).thenThrow(
          new CustomException(SHOP_NOT_FOUND));

      // when
      // then
      assertThatThrownBy(
          () -> orderManagerService.createOrder(request))
          .isInstanceOf(CustomException.class)
          .hasMessage("해당 가게를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("주문 생성 시, 존재하지 않는 고객이면 예외가 발생한다.")
    void not_found_customer() {
      // given
      Member customer = createMember(CUSTOMER);
      Member owner = createMember(OWNER);
      Shop shop = createShop(owner);

      ManagerCreateOrderRequestDto request = ManagerCreateOrderRequestDto.builder()
          .shopId(shop.getId())
          .address("배달 주소")
          .menuList(List.of())
          .instruction("요청 사항")
          .orderStatus(PENDING)
          .orderType(ONLINE)
          .customerId(customer.getId())
          .build();

      when(shopRepository.findByIdAndDeletedAtIsNull(request.shopId())).thenReturn(
          Optional.of(shop));

      when(memberRepository.findByIdAndDeletedAtIsNull(request.customerId())).thenThrow(
          new CustomException(MEMBER_NOT_FOUND));

      // when
      // then
      assertThatThrownBy(
          () -> orderManagerService.createOrder(request))
          .isInstanceOf(CustomException.class)
          .hasMessage("해당 사용자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("customerId가 null일 때, 주문 성공")
    void success_customerId_is_null() {
      // given
      Member customer = null;
      Member owner = createMember(OWNER);
      Shop shop = createShop(owner);

      ManagerCreateOrderRequestDto request = ManagerCreateOrderRequestDto.builder()
          .shopId(shop.getId())
          .address("배달 주소")
          .menuList(List.of())
          .instruction("요청 사항")
          .orderStatus(PENDING)
          .orderType(ONLINE)
          .customerId(null)
          .build();

      List<OrderMenu> orderMenuList = createOrderMenuList(
          createOrderMenuWithQuantity("치킨", 10000, 1),
          createOrderMenuWithQuantity("피자", 10000, 2),
          createOrderMenuWithQuantity("족발", 10000, 3)

      );
      Order order = createOrder(customer, shop, PENDING, ONLINE, orderMenuList);

      when(shopRepository.findByIdAndDeletedAtIsNull(request.shopId())).thenReturn(
          Optional.of(shop));
      when(orderMenuListBuilder.create(request.toOrderCreateDto(shop, customer)))
          .thenReturn(orderMenuList);
      when(orderRepository.save(any())).thenReturn(order);

      // when
      UUID actualId = orderManagerService.createOrder(request);

      // then
      verify(orderMenuListBuilder).create(request.toOrderCreateDto(shop, customer));
      assertThat(actualId).isEqualTo(order.getId());
    }

    @Test
    @DisplayName("customerId가 null이 아닐 때, 주문 성공")
    void success_customerId_is_not_null() {
      // given
      Member customer = createMember(CUSTOMER);
      Member owner = createMember(OWNER);
      Shop shop = createShop(owner);

      ManagerCreateOrderRequestDto request = ManagerCreateOrderRequestDto.builder()
          .shopId(UUID.randomUUID())
          .menuList(List.of())
          .customerId(customer.getId())
          .build();

      List<OrderMenu> orderMenuList = createOrderMenuList(
          createOrderMenuWithQuantity("치킨", 10000, 1),
          createOrderMenuWithQuantity("피자", 10000, 2),
          createOrderMenuWithQuantity("족발", 10000, 3)
      );

      when(shopRepository.findByIdAndDeletedAtIsNull(request.shopId())).thenReturn(
          Optional.of(shop));
      when(orderMenuListBuilder.create(request.toOrderCreateDto(shop, customer)))
          .thenReturn(orderMenuList);
      when(memberRepository.findByIdAndDeletedAtIsNull(customer.getId())).thenReturn(
          Optional.of(customer));

      Order order = createOrder(customer, shop, PENDING, ONLINE, orderMenuList);
      when(orderRepository.save(any())).thenReturn(order);

      // when
      UUID actualId = orderManagerService.createOrder(request);

      // then
      verify(orderRepository).save(any(Order.class));
      assertThat(actualId).isEqualTo(order.getId());
    }
  }

  @Nested
  class GetOrder {

    @Test
    @DisplayName("관리자가 주문 상세 조회 시, 존재하지 않는 주문이면 예외가 발생한다.")
    void order_not_found() {
      // given
      Member loginMember = createMember(MANAGER);
      UUID orderId = UUID.randomUUID();
      when(orderRepository.findByIdAndDeletedAtIsNull(orderId)).thenThrow(
          new CustomException(ORDER_NOT_FOUND));

      // when
      // then
      assertThatThrownBy(() -> orderManagerService.getOrder(orderId, loginMember.getId()))
          .isInstanceOf(CustomException.class)
          .hasMessage("해당 주문을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("관리자가 주문 상세 조회 성공")
    void success() {
      // given
      Member loginMember = createMember(MANAGER);

      Shop shop = createShop(loginMember);
      List<OrderMenu> orderMenuList = createOrderMenuList(
          createOrderMenuWithQuantity("치킨", 5000, 1),
          createOrderMenuWithQuantity("피자", 10000, 2),
          createOrderMenuWithQuantity("족발", 20000, 3)
      );

      Order order = createOrder(loginMember, shop, PENDING, ONLINE, orderMenuList);
      when(orderRepository.findByIdAndDeletedAtIsNull(order.getId())).thenReturn(
          Optional.of(order));

      // when
      ManagerOrderDetailsResponseDto response = orderManagerService.getOrder(order.getId(),
          loginMember.getId());

      // then
      assertThat(response.shopName()).isEqualTo(order.getShop().getName());
      assertThat(response.address()).isEqualTo(order.getAddress());
      assertThat(response.orderedUsername()).isEqualTo(order.getMember().getUsername());
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
    @DisplayName("관리자가 주문 목록 조회 성공")
    void success() {
      // given
      String username = "want";
      String loginMemberUsername = "me";
      PageRequestDto pageRequestDto = PageRequestDto.of(0, 10, "latest");
      String keyword = "sampleKeyword";

      PageResponseDto<ManagerOrderSummaryResponseDto> expectedResponse =
          new PageResponseDto<>(Collections.singletonList(
              new ManagerOrderSummaryResponseDto(createOrder())
          ), 1L);

      when(orderSearchRepositoryCustom.findByManagerWithPage(username, pageRequestDto, keyword))
          .thenReturn(expectedResponse);

      // when
      PageResponseDto<ManagerOrderSummaryResponseDto> actualResponse =
          orderManagerService.getOrderList(username, loginMemberUsername, pageRequestDto, keyword);

      // then
      assertThat(expectedResponse).isEqualTo(actualResponse);
      verify(orderSearchRepositoryCustom).findByManagerWithPage(username, pageRequestDto, keyword);
    }
  }

  @Nested
  class SearchOrderList {

    @Test
    @DisplayName("관리자 전용 주문 내역 검색 성공")
    void success() {
      // given
      String customerUsername = "customer";
      UUID shopId = UUID.randomUUID();

      PageRequestDto pageRequestDto = PageRequestDto.of(0, 10, "latest");
      String keyword = "sampleKeyword";

      PageResponseDto<ManagerOrderSummaryResponseDto> expectedResponse =
          new PageResponseDto<>(Collections.singletonList(
              new ManagerOrderSummaryResponseDto(createOrder())
          ), 1L);
      when(orderSearchRepositoryCustom.searchByManagerWithPage(customerUsername, shopId,
          pageRequestDto,
          keyword)).thenReturn(expectedResponse);

      // when
      PageResponseDto<ManagerOrderSummaryResponseDto> actualResponse =
          orderManagerService.searchOrderList(customerUsername, shopId, pageRequestDto, keyword);

      // then
      assertThat(expectedResponse).isEqualTo(actualResponse);
      verify(orderSearchRepositoryCustom).searchByManagerWithPage(
          customerUsername, shopId, pageRequestDto, keyword);
    }
  }

  @Nested
  class CancelOrder {

    @Test
    @DisplayName("관리자의 주문 취소 성공")
    void success() {
      // given
      Member loginMember = createMember(MANAGER);

      Member customer = createMember(CUSTOMER);
      Shop shop = createShop(loginMember);
      List<OrderMenu> orderMenuList = createOrderMenuList(
          createOrderMenuWithQuantity("치킨", 5000, 1),
          createOrderMenuWithQuantity("피자", 10000, 2),
          createOrderMenuWithQuantity("족발", 20000, 3)
      );
      Order order = createOrder(customer, shop, PENDING, ONLINE, orderMenuList);
      when(orderRepository.findByIdAndDeletedAtIsNull(order.getId())).thenReturn(
          Optional.of(order));

      // when
      orderManagerService.cancelOrder(order.getId(), loginMember.getId());

      // then
      assertThat(order.getOrderStatus()).isEqualTo(CANCELED);
    }
  }
}