package com.fourseason.delivery.domain.order.service;

import static com.fourseason.delivery.domain.member.entity.Role.CUSTOMER;
import static com.fourseason.delivery.domain.member.entity.Role.OWNER;
import static com.fourseason.delivery.domain.order.entity.OrderStatus.ACCEPTED;
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
import com.fourseason.delivery.domain.order.dto.request.OwnerCreateOrderRequestDto;
import com.fourseason.delivery.domain.order.dto.response.impl.OwnerOrderDetailsResponseDto;
import com.fourseason.delivery.domain.order.dto.response.impl.OwnerOrderSummaryResponseDto;
import com.fourseason.delivery.domain.order.entity.Order;
import com.fourseason.delivery.domain.order.entity.OrderMenu;
import com.fourseason.delivery.domain.order.repository.OrderRepository;
import com.fourseason.delivery.domain.order.repository.OrderSearchRepositoryCustom;
import com.fourseason.delivery.domain.payment.entity.Payment;
import com.fourseason.delivery.domain.payment.repository.PaymentRepository;
import com.fourseason.delivery.domain.shop.entity.Shop;
import com.fourseason.delivery.domain.shop.repository.ShopRepository;
import com.fourseason.delivery.fixture.MemberFixture;
import com.fourseason.delivery.fixture.OrderFixture;
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
class OrderOwnerServiceTest {

  @Mock
  private ShopRepository shopRepository;

  @Mock
  private OrderMenuListBuilder orderMenuListBuilder;

  @Mock
  private OrderRepository orderRepository;

  @Mock
  private PaymentRepository paymentRepository;

  @Mock
  private OrderSearchRepositoryCustom orderSearchRepositoryCustom;

  @InjectMocks
  private OrderOwnerService orderOwnerService;

  @Nested
  class CreateOrder {

    @Test
    @DisplayName("대면 주문 생성 시, 존재하지 않는 가게면 예외가 발생한다.")
    void not_found_shop() {
      // given
      OwnerCreateOrderRequestDto request = OwnerCreateOrderRequestDto.builder()
          .shopId(UUID.randomUUID())
          .address("배달 주소")
          .instruction("요청 사항")
          .menuList(List.of())
          .build();

      Long ownerId = 1L;

      when(shopRepository.findByIdAndDeletedAtIsNull(request.shopId())).thenThrow(
          new CustomException(SHOP_NOT_FOUND));

      // when
      // then
      assertThatThrownBy(
          () -> orderOwnerService.createOfflineOrder(request, ownerId))
          .isInstanceOf(CustomException.class)
          .hasMessage("해당 가게를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("대면 주문 생성 시, 가게 주인이 아니면 예외가 발생한다.")
    void not_shop_owner() {
      // given
      Member owner = createMember(OWNER);

      Member another = createMember(OWNER);
      Shop shop = createShop(another);

      OwnerCreateOrderRequestDto request = OwnerCreateOrderRequestDto.builder()
          .shopId(shop.getId())
          .address("배달 주소")
          .menuList(List.of())
          .instruction("요청 사항")
          .build();

      when(shopRepository.findByIdAndDeletedAtIsNull(request.shopId())).thenReturn(
          Optional.of(shop));

      // when
      // then
      assertThatThrownBy(
          () -> orderOwnerService.createOfflineOrder(request, owner.getId()))
          .isInstanceOf(CustomException.class)
          .hasMessage("가게 주인이 아닙니다.");
    }

    @Test
    @DisplayName("대면 주문 생성 성공")
    void success() {
      // given
      Member owner = createMember(OWNER);
      Shop shop = createShop(owner);

      OwnerCreateOrderRequestDto request = OwnerCreateOrderRequestDto.builder()
          .shopId(shop.getId())
          .address("배달 주소")
          .instruction("요청 사항")
          .menuList(List.of())
          .build();

      Member customer = createMember(CUSTOMER);
      List<OrderMenu> orderMenuList = createOrderMenuList(
          createOrderMenuWithQuantity("치킨", 10000, 1),
          createOrderMenuWithQuantity("피자", 10000, 2),
          createOrderMenuWithQuantity("족발", 10000, 3)
      );

      when(shopRepository.findByIdAndDeletedAtIsNull(request.shopId())).thenReturn(
          Optional.of(shop));
      when(orderMenuListBuilder.create(request.toOrderCreateDto(shop)))
          .thenReturn(orderMenuList);

      Order order = createOrder(customer, shop, PENDING, ONLINE, orderMenuList);
      when(orderRepository.save(any())).thenReturn(order);

      // when
      UUID actualId = orderOwnerService.createOfflineOrder(request, owner.getId());

      // then
      verify(orderRepository).save(any(Order.class));
      assertThat(actualId).isEqualTo(order.getId());
    }
  }

  @Nested
  class AcceptOrder {

    UUID orderId = UUID.randomUUID();

    @Test
    @DisplayName("주문 수락 시 존재하지 않은 주문이면 예외가 발생한다.")
    void order_not_found() {
      // given
      Member loginMember = MemberFixture.createMember(OWNER);

      when(orderRepository.findByIdAndDeletedAtIsNull(orderId)).thenThrow(
          new CustomException(ORDER_NOT_FOUND));

      // when
      // then
      assertThatThrownBy(() -> orderOwnerService.acceptOrder(orderId, loginMember.getId()))
          .isInstanceOf(CustomException.class)
          .hasMessage("해당 주문을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("주문 수락 시 해당 가게의 점주가 아니면 예외가 발생한다.")
    void not_shop_order() {
      // given
      Member loginMember = MemberFixture.createMember(OWNER);

      Member anotherOwner = MemberFixture.createMember(OWNER);
      Shop anotherOwnerShop = createShop(anotherOwner);

      Order anotherOrder = OrderFixture.createOrder(
          MemberFixture.createMember(CUSTOMER), anotherOwnerShop, PENDING, ONLINE, List.of());

      when(orderRepository.findByIdAndDeletedAtIsNull(orderId)).thenReturn(
          Optional.of(anotherOrder));

      // when
      // then
      assertThatThrownBy(() -> orderOwnerService.acceptOrder(orderId, loginMember.getId()))
          .isInstanceOf(CustomException.class)
          .hasMessage("가게 주인이 아닙니다.");
    }

    @Test
    @DisplayName("주문 수락 시 수락 대기중인 주문이 아니면 예외가 발생한다.")
    void no_pending_order() {
      // given
      Member loginMember = MemberFixture.createMember(OWNER);

      Shop shop = createShop(loginMember);

      Order noPendingOrder = OrderFixture.createOrder(
          MemberFixture.createMember(CUSTOMER), shop, ACCEPTED, ONLINE, List.of());

      when(orderRepository.findByIdAndDeletedAtIsNull(orderId))
          .thenReturn(Optional.of(noPendingOrder));

      // when
      // then
      assertThatThrownBy(() -> orderOwnerService.acceptOrder(orderId, loginMember.getId()))
          .isInstanceOf(CustomException.class)
          .hasMessage("보류 중인 주문이 아닙니다.");
    }

    @Test
    @DisplayName("주문 수락 시 결제된 주문이 아니면 예외가 발생한다.")
    void not_paid_order() {
      // given
      Member loginMember = MemberFixture.createMember(OWNER);

      Shop shop = createShop(loginMember);

      Order order = OrderFixture.createOrder(
          MemberFixture.createMember(CUSTOMER), shop, PENDING, ONLINE, List.of());

      when(orderRepository.findByIdAndDeletedAtIsNull(orderId))
          .thenReturn(Optional.of(order));

      Payment payment = Payment.builder()
          .paymentStatus("READY")
          .build();

      when(paymentRepository.findByOrderAndDeletedAtIsNull(order)).thenReturn(Optional.of(payment));

      // when
      // then
      assertThatThrownBy(() -> orderOwnerService.acceptOrder(orderId, loginMember.getId()))
          .isInstanceOf(CustomException.class)
          .hasMessage("결제되지 않은 주문입니다.");
    }

    @Test
    @DisplayName("주문 수락 성공")
    void success() {
      // given
      Member loginMember = MemberFixture.createMember(OWNER);
      Shop shop = createShop(loginMember);
      Order order = OrderFixture.createOrder(
          MemberFixture.createMember(CUSTOMER), shop, PENDING, ONLINE, List.of());

      when(orderRepository.findByIdAndDeletedAtIsNull(orderId)).thenReturn(Optional.of(order));

      Payment payment = Payment.builder()
          .paymentStatus("DONE")
          .build();

      when(paymentRepository.findByOrderAndDeletedAtIsNull(order)).thenReturn(Optional.of(payment));

      // when
      orderOwnerService.acceptOrder(orderId, loginMember.getId());

      // then
      assertThat(order.getOrderStatus()).isEqualTo(ACCEPTED);
    }
  }

  @Nested
  class GetOrder {

    @Test
    @DisplayName("점주가 주문 상세 조회 시, 존재하지 않는 주문이면 예외가 발생한다.")
    void order_not_found() {
      // given
      Member loginMember = createMember(OWNER);
      UUID orderId = UUID.randomUUID();
      when(orderRepository.findByIdAndDeletedAtIsNull(orderId)).thenThrow(
          new CustomException(ORDER_NOT_FOUND));

      // when
      // then
      assertThatThrownBy(() -> orderOwnerService.getOrder(orderId, loginMember.getId()))
          .isInstanceOf(CustomException.class)
          .hasMessage("해당 주문을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("점주가 주문 상세 조회 시, 주문의 고객도 아니고, 주문 가게의 주인도 아니면 예외가 발생한다.")
    void not_ordered_by_customer() {
      // given
      Member loginMember = createMember(OWNER);

      Member anotherOwner = createMember(OWNER);
      Shop anotherOwnerShop = createShop(anotherOwner);
      List<OrderMenu> orderMenuList = createOrderMenuList(
          createOrderMenuWithQuantity("치킨", 5000, 1),
          createOrderMenuWithQuantity("피자", 10000, 2),
          createOrderMenuWithQuantity("족발", 20000, 3)
      );

      Member customer = createMember(CUSTOMER);
      Order order = createOrder(customer, anotherOwnerShop, PENDING, ONLINE, orderMenuList);
      when(orderRepository.findByIdAndDeletedAtIsNull(order.getId())).thenReturn(
          Optional.of(order));

      // when
      // then
      assertThatThrownBy(() -> orderOwnerService.getOrder(order.getId(), loginMember.getId()))
          .isInstanceOf(CustomException.class)
          .hasMessage("가게 주인이거나 주문 고객이어야 합니다.");
    }

    @Test
    @DisplayName("점주 주문 상세 조회 성공")
    void success() {
      // given
      Member loginMember = createMember(OWNER);

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
      OwnerOrderDetailsResponseDto response = orderOwnerService.getOrder(order.getId(),
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
    @DisplayName("주문 목록 조회 시, 회원 본인의 주문 목록이 아니면 예외가 발생한다.")
    void not_ordered_by_customer() {
      // given
      String username = "want";
      String loginMemberUsername = "me";
      PageRequestDto pageRequestDto = PageRequestDto.of(0, 10, "latest");
      String keyword = "sampleKeyword";

      // when
      // then
      assertThatThrownBy(() -> orderOwnerService.getOrderList(
          username, loginMemberUsername, pageRequestDto, keyword))
          .isInstanceOf(CustomException.class)
          .hasMessage("주문 조회 권한이 없습니다.");
    }

    @Test
    @DisplayName("주문 목록 조회 성공")
    void success() {
      // given
      String username = "want";
      String loginMemberUsername = "want";
      PageRequestDto pageRequestDto = PageRequestDto.of(0, 10, "latest");
      String keyword = "sampleKeyword";

      PageResponseDto<OwnerOrderSummaryResponseDto> expectedResponse =
          new PageResponseDto<>(Collections.singletonList(
              new OwnerOrderSummaryResponseDto(createOrder())
          ), 1L);

      when(orderSearchRepositoryCustom.findByOwnerWithPage(username, pageRequestDto, keyword))
          .thenReturn(expectedResponse);

      // when
      PageResponseDto<OwnerOrderSummaryResponseDto> actualResponse =
          orderOwnerService.getOrderList(username, loginMemberUsername, pageRequestDto, keyword);

      // then
      assertThat(expectedResponse).isEqualTo(actualResponse);
      verify(orderSearchRepositoryCustom).findByOwnerWithPage(username, pageRequestDto, keyword);
    }
  }

  @Nested
  class SearchOrderList {

    @Test
    @DisplayName("점주가 주문 검색 시, 존재하지 않는 가게면 예외가 발생한다.")
    void shop_not_found() {
      // given
      Member member = createMember(OWNER);
      UUID shopId = UUID.randomUUID();

      PageRequestDto pageRequestDto = PageRequestDto.of(0, 10, "latest");
      String keyword = "sampleKeyword";

      // when
      when(shopRepository.findByIdAndDeletedAtIsNull(shopId)).thenReturn(Optional.empty());
      // then
      assertThatThrownBy(() -> orderOwnerService.searchOrderList(
          member.getId(), shopId, pageRequestDto, keyword))
          .isInstanceOf(CustomException.class)
          .hasMessage("해당 가게를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("점주가 주문 검색 시, 본인의 가게가 아니면 예외가 발생한다.")
    void order_not_found() {
      // given
      Member member = createMember(OWNER);

      Member another = createMember(OWNER);
      Shop shop = createShop(another);

      PageRequestDto pageRequestDto = PageRequestDto.of(0, 10, "latest");
      String keyword = "sampleKeyword";

      // when
      when(shopRepository.findByIdAndDeletedAtIsNull(shop.getId())).thenReturn(Optional.of(shop));
      // then
      assertThatThrownBy(() -> orderOwnerService.searchOrderList(
          member.getId(), shop.getId(), pageRequestDto, keyword))
          .isInstanceOf(CustomException.class)
          .hasMessage("가게 주인이 아닙니다.");
    }

    @Test
    @DisplayName("점주가 주문 검색 시, shopId가 없어도 성공한다.")
    void success_shopId_is_null() {
      // given
      Member member = createMember(OWNER);
      PageRequestDto pageRequestDto = PageRequestDto.of(0, 10, "latest");
      String keyword = "sampleKeyword";

      PageResponseDto<OwnerOrderSummaryResponseDto> expectedResponse =
          new PageResponseDto<>(Collections.singletonList(
              new OwnerOrderSummaryResponseDto(createOrder())
          ), 1L);

      when(orderSearchRepositoryCustom.searchByOwnerWithPage(member.getId(), null, pageRequestDto,
          keyword)).thenReturn(expectedResponse);

      // when
      PageResponseDto<OwnerOrderSummaryResponseDto> actualResponse =
          orderOwnerService.searchOrderList(member.getId(), null, pageRequestDto, keyword);

      // then
      assertThat(expectedResponse).isEqualTo(actualResponse);
      verify(orderSearchRepositoryCustom).searchByOwnerWithPage(
          member.getId(), null, pageRequestDto, keyword);
    }

    @Test
    @DisplayName("점주 가게 주문 내역 검색 성공.")
    void success_shopId_is_not_null() {
      // given
      Member member = createMember(OWNER);
      Shop shop = createShop(member);
      PageRequestDto pageRequestDto = PageRequestDto.of(0, 10, "latest");
      String keyword = "sampleKeyword";

      when(shopRepository.findByIdAndDeletedAtIsNull(shop.getId())).thenReturn(Optional.of(shop));

      PageResponseDto<OwnerOrderSummaryResponseDto> expectedResponse =
          new PageResponseDto<>(Collections.singletonList(
              new OwnerOrderSummaryResponseDto(createOrder())
          ), 1L);
      when(orderSearchRepositoryCustom.searchByOwnerWithPage(member.getId(), shop.getId(),
          pageRequestDto,
          keyword)).thenReturn(expectedResponse);

      // when
      PageResponseDto<OwnerOrderSummaryResponseDto> actualResponse =
          orderOwnerService.searchOrderList(member.getId(), shop.getId(), pageRequestDto, keyword);

      // then
      assertThat(expectedResponse).isEqualTo(actualResponse);
      verify(orderSearchRepositoryCustom).searchByOwnerWithPage(
          member.getId(), shop.getId(), pageRequestDto, keyword);
    }
  }

  @Nested
  class CancelOrder {

    @Test
    @DisplayName("점주가 주문 취소 시, 존재하지 않는 주문이면 예외가 발생한다.")
    void order_not_found() {
      // given
      Member loginMember = createMember(OWNER);
      UUID orderId = UUID.randomUUID();
      when(orderRepository.findByIdAndDeletedAtIsNull(orderId)).thenThrow(
          new CustomException(ORDER_NOT_FOUND));

      // when
      // then
      assertThatThrownBy(() -> orderOwnerService.cancelOrder(orderId, loginMember.getId()))
          .isInstanceOf(CustomException.class)
          .hasMessage("해당 주문을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("점주가 주문 취소 시, 주문의 고객도 아니고, 주문 가게의 주인도 아니면 예외가 발생한다.")
    void not_ordered_by_customer() {
      // given
      Member loginMember = createMember(OWNER);

      Member customer = createMember(CUSTOMER);
      Member anotherOwner = createMember(OWNER);
      Shop anotherShop = createShop(anotherOwner);
      List<OrderMenu> orderMenuList = createOrderMenuList(
          createOrderMenuWithQuantity("치킨", 5000, 1),
          createOrderMenuWithQuantity("피자", 10000, 2),
          createOrderMenuWithQuantity("족발", 20000, 3)
      );
      Order order = createOrder(customer, anotherShop, PENDING, ONLINE, orderMenuList);
      when(orderRepository.findByIdAndDeletedAtIsNull(order.getId())).thenReturn(
          Optional.of(order));

      // when
      // then
      assertThatThrownBy(() -> orderOwnerService.cancelOrder(order.getId(), loginMember.getId()))
          .isInstanceOf(CustomException.class)
          .hasMessage("가게 주인이거나 주문 고객이어야 합니다.");
    }

    @Test
    @DisplayName("점주의 요청 주문 취소 성공")
    void success() {
      // given
      Member loginMember = createMember(OWNER);

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
      orderOwnerService.cancelOrder(order.getId(), loginMember.getId());

      // then
      assertThat(order.getOrderStatus()).isEqualTo(CANCELED);
    }
  }
}