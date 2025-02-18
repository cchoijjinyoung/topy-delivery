package com.fourseason.delivery.domain.order.service;

import static com.fourseason.delivery.domain.order.exception.OrderErrorCode.MEMBER_NOT_FOUND;
import static com.fourseason.delivery.domain.shop.exception.ShopErrorCode.SHOP_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.member.repository.MemberRepository;
import com.fourseason.delivery.domain.menu.entity.Menu;
import com.fourseason.delivery.domain.menu.repository.MenuRepository;
import com.fourseason.delivery.domain.order.dto.request.CreateOrderRequestDto;
import com.fourseason.delivery.domain.order.dto.request.CreateOrderRequestDto.MenuDto;
import com.fourseason.delivery.domain.order.entity.Order;
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
      Member member = Member.builder().build();

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
      Member member = Member.builder().build();
      Shop shop = Shop.builder().build();

      when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
      when(shopRepository.findById(request.shopId())).thenReturn(
          Optional.of(shop));
      when(menuRepository.findByIdIn(List.of(request.menuList().get(0).menuId())))
          .thenReturn(List.of());

      // when
      // then
      assertThatThrownBy(() -> orderCustomerService.createOrder(request, memberId))
          .isInstanceOf(CustomException.class)
          .hasMessage("해당 메뉴를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("주문 요청 성공")
    void success() {
      // given
      Member member = Member.builder().build();
      when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

      Shop shop = Shop.builder().build();
      when(shopRepository.findById(request.shopId())).thenReturn(Optional.of(shop));

      Menu menu = mock(Menu.class);
      UUID menuId = request.menuList().get(0).menuId();

      when(menu.getId()).thenReturn(menuId);
      when(menu.getPrice()).thenReturn(1000);
      when(menuRepository.findByIdIn(List.of(request.menuList().get(0).menuId())))
          .thenReturn(List.of(menu));

      UUID orderId = UUID.randomUUID();
      Order savedOrder = mock(Order.class);
      when(savedOrder.getId()).thenReturn(orderId);
      when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

      // when
      UUID savedOrderId = orderCustomerService.createOrder(request, memberId);

      // then
      verify(orderRepository).save(any());
      assertThat(savedOrderId).isEqualTo(orderId);
    }
  }
}