package com.fourseason.delivery.domain.order.service;

import static com.fourseason.delivery.domain.order.entity.OrderStatus.ACCEPTED;
import static com.fourseason.delivery.domain.order.exception.OrderErrorCode.ORDER_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.order.entity.Order;
import com.fourseason.delivery.domain.order.repository.OrderRepository;
import com.fourseason.delivery.domain.shop.entity.Shop;
import com.fourseason.delivery.global.exception.CustomException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OrderOwnerServiceTest {

  @Mock
  OrderRepository orderRepository;

  @InjectMocks
  OrderOwnerService orderOwnerService;

  @Nested
  class acceptOrder {

    Long ownerId = 1L;

    UUID orderId = UUID.randomUUID();

    @Test
    @DisplayName("주문 수락 시 존재하지 않은 주문이면 예외가 발생한다.")
    void order_not_found() {
      // given
      when(orderRepository.findById(orderId)).thenThrow(new CustomException(ORDER_NOT_FOUND));

      // when  
      // then      
      assertThatThrownBy(() -> orderOwnerService.acceptOrder(ownerId, orderId))
          .isInstanceOf(CustomException.class)
          .hasMessage("해당 주문을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("주문 수락 시 해당 가게의 주문이 아니면 예외가 발생한다.")
    void not_shop_order() {
      // given
      Member owner = Member.builder().build();
      ReflectionTestUtils.setField(owner, "id", ownerId);

      Long anotherId = 2L;
      Member another = Member.builder().build();
      ReflectionTestUtils.setField(another, "id", anotherId);

      Shop anotherShop = Shop.builder()
          .member(another)
          .build();

      Order anotherOrder = Order.builder()
          .shop(anotherShop)
          .build();

      when(orderRepository.findById(orderId)).thenReturn(Optional.of(anotherOrder));

      // when
      // then
      assertThatThrownBy(() -> orderOwnerService.acceptOrder(ownerId, orderId))
          .isInstanceOf(CustomException.class)
          .hasMessage("가게 주인이 아닙니다.");
    }

    @Test
    @DisplayName("주문 수락 성공")
    void success() {
      // given
      Member owner = Member.builder().build();
      ReflectionTestUtils.setField(owner, "id", ownerId);

      Shop shop = Shop.builder()
          .member(owner)
          .build();

      Order order = Order.builder()
          .shop(shop)
          .build();

      when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

      // when
      orderOwnerService.acceptOrder(ownerId, orderId);

      // then
      assertThat(order.getOrderStatus()).isEqualTo(ACCEPTED);
    }
  }
}