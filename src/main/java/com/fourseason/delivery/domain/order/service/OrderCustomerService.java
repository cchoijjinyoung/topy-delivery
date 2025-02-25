package com.fourseason.delivery.domain.order.service;

import static com.fourseason.delivery.domain.member.entity.Role.*;
import static com.fourseason.delivery.domain.member.exception.MemberErrorCode.MEMBER_NOT_FOUND;
import static com.fourseason.delivery.domain.order.constant.OrderConstants.ORDER_CANCELED_DEADLINE;
import static com.fourseason.delivery.domain.order.entity.OrderStatus.CANCELED;
import static com.fourseason.delivery.domain.order.exception.OrderErrorCode.*;
import static com.fourseason.delivery.domain.order.exception.OrderErrorCode.ORDER_NOT_FOUND;
import static com.fourseason.delivery.domain.shop.exception.ShopErrorCode.SHOP_NOT_FOUND;

import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.member.entity.Role;
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
import com.fourseason.delivery.global.dto.PageRequestDto;
import com.fourseason.delivery.global.dto.PageResponseDto;
import com.fourseason.delivery.global.exception.CustomException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderCustomerService implements OrderRoleService {

  private final ShopRepository shopRepository;
  private final MemberRepository memberRepository;
  private final OrderMenuListBuilder orderMenuListBuilder;
  private final OrderRepository orderRepository;
  private final OrderSearchRepositoryCustom orderSearchRepositoryCustom;

  @Override
  public Role supports() {
    return CUSTOMER;
  }

  @Transactional
  public UUID createOnlineOrder(CustomerCreateOrderRequestDto request, Long customerId) {
    Shop shop = findShopOrThrow(request.shopId());
    Member customer = findMemberOrThrow(customerId);

    List<OrderMenu> orderMenuList = orderMenuListBuilder.create(
        request.toOrderCreateDto(shop, customer));

    Order createOrder = Order.addByCustomer(
        shop,
        customer,
        request.address(),
        request.instruction(),
        orderMenuList
    );

    return orderRepository.save(createOrder).getId();
  }

  @Transactional(readOnly = true)
  public CustomerOrderDetailsResponseDto getOrder(UUID orderId, Long memberId) {
    Order order = findOrderOrThrow(orderId);
    order.assertOrderedBy(memberId);
    return CustomerOrderDetailsResponseDto.of(order);
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponseDto<CustomerOrderSummaryResponseDto> getOrderList(
      String username,
      String loginMemberUsername,
      PageRequestDto pageRequestDto,
      String keyword
  ) {
    if (!username.equals(loginMemberUsername)) {
      throw new CustomException(NOT_READ_ORDER_PERMISSION);
    }
    return orderSearchRepositoryCustom.findByCustomerWithPage(username, pageRequestDto, keyword);
  }

  @Override
  @Transactional
  public void cancelOrder(UUID orderId, Long memberId) {
    Order order = findOrderOrThrow(orderId);
    order.assertOrderedBy(memberId);
    order.assertAlreadyCanceled();
    order.assertExpiredCancelTime(ORDER_CANCELED_DEADLINE);
    order.updateStatus(CANCELED);
  }

  private Member findMemberOrThrow(Long customerId) {
    return memberRepository.findByIdAndDeletedAtIsNull(customerId)
        .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
  }

  private Shop findShopOrThrow(UUID shopId) {
    return shopRepository.findByIdAndDeletedAtIsNull(shopId)
        .orElseThrow(() -> new CustomException(SHOP_NOT_FOUND));
  }

  private Order findOrderOrThrow(UUID orderId) {
    return orderRepository.findByIdAndDeletedAtIsNull(orderId)
        .orElseThrow(() -> new CustomException(ORDER_NOT_FOUND));
  }
}
