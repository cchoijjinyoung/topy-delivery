package com.fourseason.delivery.domain.order.service;

import static com.fourseason.delivery.domain.member.entity.Role.CUSTOMER;
import static com.fourseason.delivery.domain.member.entity.Role.OWNER;
import static com.fourseason.delivery.domain.menu.entity.MenuStatus.SHOW;
import static com.fourseason.delivery.domain.order.entity.OrderStatus.ACCEPTED;
import static com.fourseason.delivery.domain.order.entity.OrderStatus.CANCELED;
import static com.fourseason.delivery.domain.order.entity.OrderStatus.PENDING;
import static com.fourseason.delivery.domain.order.entity.OrderType.ONLINE;
import static com.fourseason.delivery.domain.order.exception.OrderErrorCode.ORDER_NOT_FOUND;
import static com.fourseason.delivery.domain.shop.exception.ShopErrorCode.SHOP_NOT_FOUND;
import static com.fourseason.delivery.fixture.MemberFixture.createMember;
import static com.fourseason.delivery.fixture.MenuFixture.createMenu;
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
import com.fourseason.delivery.domain.menu.entity.Menu;
import com.fourseason.delivery.domain.menu.repository.MenuRepository;
import com.fourseason.delivery.domain.order.dto.request.OwnerCreateOrderRequestDto;
import com.fourseason.delivery.domain.order.dto.request.OwnerCreateOrderRequestDto.MenuDto;
import com.fourseason.delivery.domain.order.dto.response.OwnerOrderDetailResponseDto;
import com.fourseason.delivery.domain.order.entity.Order;
import com.fourseason.delivery.domain.order.entity.OrderMenu;
import com.fourseason.delivery.domain.order.entity.OrderType;
import com.fourseason.delivery.domain.order.repository.OrderRepository;
import com.fourseason.delivery.domain.shop.entity.Shop;
import com.fourseason.delivery.domain.shop.repository.ShopRepository;
import com.fourseason.delivery.fixture.MemberFixture;
import com.fourseason.delivery.fixture.OrderFixture;
import com.fourseason.delivery.global.exception.CustomException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OrderOwnerServiceTest {

  @Mock
  ShopRepository shopRepository;

  @Mock
  MenuRepository menuRepository;

  @Mock
  OrderRepository orderRepository;

  @InjectMocks
  OrderOwnerService orderOwnerService;

  @Nested
  class createOrder {

    @Test
    @DisplayName("점주가 대면 주문 접수 시, 존재하지 않은 가게면, 예외가 발생한다.")
    void shop_not_found() {
      // given
      OwnerCreateOrderRequestDto request = OwnerCreateOrderRequestDto.builder()
          .shopId(UUID.randomUUID())
          .address("배달 주소")
          .instruction("요청 사항")
          .menuList(List.of())
          .build();

      Long memberId = 1L;

      when(shopRepository.findById(request.shopId())).thenThrow(
          new CustomException(SHOP_NOT_FOUND));

      // when
      // then
      assertThatThrownBy(
          () -> orderOwnerService.createOrder(request, memberId))
          .isInstanceOf(CustomException.class)
          .hasMessage("해당 가게를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("점주가 대면 주문 접수 시, 가게 주인이 아니라면 예외가 발생한다.")
    void not_shop_owner() {
      // given
      Member loginMember = createMember(OWNER);

      Member anotherOwner = createMember(OWNER);
      Shop anotherShop = createShop(anotherOwner);

      OwnerCreateOrderRequestDto request = OwnerCreateOrderRequestDto.builder()
          .shopId(anotherShop.getId())
          .address("배달 주소")
          .instruction("요청 사항")
          .menuList(List.of())
          .build();

      when(shopRepository.findById(request.shopId())).thenReturn(Optional.of(anotherShop));

      // when
      // then
      assertThatThrownBy(
          () -> orderOwnerService.createOrder(request, loginMember.getId()))
          .isInstanceOf(CustomException.class)
          .hasMessage("가게 주인이 아닙니다.");
    }

    @Test
    @DisplayName("점주가 대면 주문 접수 시, 존재히지 않는 메뉴라면 예외가 발생한다.")
    void not_found_menu() {
      // given
      Member loginMember = createMember(OWNER);

      Shop shop = createShop(loginMember);

      // 요청 메뉴: 한 종류
      MenuDto menuDto = MenuDto.builder()
          .menuId(UUID.randomUUID())
          .quantity(2)
          .build();

      OwnerCreateOrderRequestDto request = OwnerCreateOrderRequestDto.builder()
          .shopId(shop.getId())
          .address("배달 주소")
          .instruction("요청 사항")
          .menuList(List.of(menuDto))
          .build();

      when(shopRepository.findById(request.shopId())).thenReturn(Optional.of(shop));
      when(menuRepository.findByIdInAndMenuStatusAndShopAndDeletedAtIsNull(
          request.menuList().stream().map(MenuDto::menuId).toList(), SHOW, shop))
          .thenReturn(List.of()); // 비어있는 메뉴 리스트가 응답될 시,

      // when
      // then
      assertThatThrownBy(
          () -> orderOwnerService.createOrder(request, loginMember.getId()))
          .isInstanceOf(CustomException.class)
          .hasMessage("해당 메뉴를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("점주대면 주문 접수 성공")
    void success() {
      // given
      Member loginMember = createMember(OWNER);

      Shop shop = createShop(loginMember);

      Menu menu1 = createMenu(shop, "치킨", 10000);
      Menu menu2 = createMenu(shop, "피자", 10000);
      Menu menu3 = createMenu(shop, "족발", 10000);

      OwnerCreateOrderRequestDto request = OwnerCreateOrderRequestDto.builder()
          .shopId(shop.getId())
          .address("배달 주소")
          .instruction("요청 사항")
          .menuList(List.of(
              MenuDto.builder().menuId(menu1.getId()).quantity(1).build(),
              MenuDto.builder().menuId(menu2.getId()).quantity(2).build(),
              MenuDto.builder().menuId(menu3.getId()).quantity(3).build()
          ))
          .build();

      when(shopRepository.findById(request.shopId())).thenReturn(Optional.of(shop));
      when(menuRepository.findByIdInAndMenuStatusAndShopAndDeletedAtIsNull(
          request.menuList().stream().map(MenuDto::menuId).toList(), SHOW, shop))
          .thenReturn(List.of(menu1, menu2, menu3));

      Order order = Order.builder().build();
      ReflectionTestUtils.setField(order, "id", UUID.randomUUID());
      when(orderRepository.save(any(Order.class))).thenReturn(order);

      // when
      UUID savedOrderId = orderOwnerService.createOrder(request, loginMember.getId());

      // then
      ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
      verify(orderRepository).save(orderCaptor.capture());
      Order capturedOrder = orderCaptor.getValue();

      assertThat(capturedOrder.getShop()).isEqualTo(shop);
      assertThat(capturedOrder.getAddress()).isEqualTo("배달 주소");
      assertThat(capturedOrder.getInstruction()).isEqualTo("요청 사항");
      assertThat(capturedOrder.getTotalPrice()).isEqualTo(60000);

      assertThat(capturedOrder.getOrderStatus()).isEqualTo(ACCEPTED);
      assertThat(capturedOrder.getOrderType()).isEqualTo(OrderType.OFFLINE);

      List<OrderMenu> orderMenus = capturedOrder.getOrderMenuList();
      assertThat(orderMenus).hasSize(3);

      OrderMenu orderMenu1 = orderMenus.get(0);
      assertThat(orderMenu1.getName()).isEqualTo("치킨");
      assertThat(orderMenu1.getPrice()).isEqualTo(10000);
      assertThat(orderMenu1.getQuantity()).isEqualTo(1);

      OrderMenu orderMenu2 = orderMenus.get(1);
      assertThat(orderMenu2.getName()).isEqualTo("피자");
      assertThat(orderMenu2.getPrice()).isEqualTo(10000);
      assertThat(orderMenu2.getQuantity()).isEqualTo(2);

      OrderMenu orderMenu3 = orderMenus.get(2);
      assertThat(orderMenu3.getName()).isEqualTo("족발");
      assertThat(orderMenu3.getPrice()).isEqualTo(10000);
      assertThat(orderMenu3.getQuantity()).isEqualTo(3);

      assertThat(savedOrderId).isEqualTo(order.getId());
    }
  }

  @Nested
  class acceptOrder {

    UUID orderId = UUID.randomUUID();

    @Test
    @DisplayName("주문 수락 시 존재하지 않은 주문이면 예외가 발생한다.")
    void order_not_found() {
      // given
      Member loginMember = MemberFixture.createMember(OWNER);

      when(orderRepository.findById(orderId)).thenThrow(new CustomException(ORDER_NOT_FOUND));

      // when  
      // then      
      assertThatThrownBy(() -> orderOwnerService.acceptOrder(loginMember.getId(), orderId))
          .isInstanceOf(CustomException.class)
          .hasMessage("해당 주문을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("주문 수락 시 해당 가게의 주문이 아니면 예외가 발생한다.")
    void not_shop_order() {
      // given
      Member loginMember = MemberFixture.createMember(OWNER);

      Member anotherOwner = MemberFixture.createMember(OWNER);
      Shop anotherOwnerShop = createShop(anotherOwner);

      Order anotherOrder = OrderFixture.createOrder(
          MemberFixture.createMember(CUSTOMER), anotherOwnerShop, PENDING, ONLINE, List.of());

      when(orderRepository.findById(orderId)).thenReturn(Optional.of(anotherOrder));

      // when
      // then
      assertThatThrownBy(() -> orderOwnerService.acceptOrder(loginMember.getId(), orderId))
          .isInstanceOf(CustomException.class)
          .hasMessage("가게 주인이 아닙니다.");
    }

    @Test
    @DisplayName("주문 수락 성공")
    void success() {
      // given
      Member loginMember = MemberFixture.createMember(OWNER);
      Shop shop = createShop(loginMember);
      Order order = OrderFixture.createOrder(
          MemberFixture.createMember(CUSTOMER), shop, PENDING, ONLINE, List.of());

      when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

      // when
      orderOwnerService.acceptOrder(loginMember.getId(), orderId);

      // then
      assertThat(order.getOrderStatus()).isEqualTo(ACCEPTED);
    }
  }

  @Nested
  class getOrder {

    @Test
    @DisplayName("점주 주문 상세 조회 시, 존재하지 않는 주문이면 예외가 발생한다.")
    void order_not_found() {
      // given
      Member loginMember = createMember(OWNER);
      UUID orderId = UUID.randomUUID();
      when(orderRepository.findById(orderId)).thenThrow(new CustomException(ORDER_NOT_FOUND));

      // when
      // then
      assertThatThrownBy(() -> orderOwnerService.getOrder(orderId, loginMember.getId()))
          .isInstanceOf(CustomException.class)
          .hasMessage("해당 주문을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("점주 주문 상세 조회 시, 주문 가게의 점주가 아니면 예외가 발생한다.")
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
      when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

      // when
      // then
      assertThatThrownBy(() -> orderOwnerService.getOrder(order.getId(), loginMember.getId()))
          .isInstanceOf(CustomException.class)
          .hasMessage("가게 주인이 아닙니다.");
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
      when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

      // when
      OwnerOrderDetailResponseDto response = orderOwnerService.getOrder(order.getId(),
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
  class CancelOrder {

    @Test
    @DisplayName("점주가 주문 취소 시, 존재하지 않는 주문이면 예외가 발생한다.")
    void order_not_found() {
      // given
      Member loginMember = createMember(OWNER);
      UUID orderId = UUID.randomUUID();
      when(orderRepository.findById(orderId)).thenThrow(new CustomException(ORDER_NOT_FOUND));

      // when
      // then
      assertThatThrownBy(() -> orderOwnerService.cancelOrder(orderId, loginMember.getId()))
          .isInstanceOf(CustomException.class)
          .hasMessage("해당 주문을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("점주가 주문 취소 시, 가게 주인이 아니면 예외가 발생한다.")
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
      when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

      // when
      // then
      assertThatThrownBy(() -> orderOwnerService.cancelOrder(order.getId(), loginMember.getId()))
          .isInstanceOf(CustomException.class)
          .hasMessage("가게 주인이 아닙니다.");
    }

    @Test
    @DisplayName("고객의 요청 주문 취소 성공")
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
      when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

      // when
      orderOwnerService.cancelOrder(order.getId(), loginMember.getId());

      // then
      assertThat(order.getOrderStatus()).isEqualTo(CANCELED);
    }
  }
}