package com.fourseason.delivery.domain.order.service;

import static com.fourseason.delivery.domain.member.entity.Role.MANAGER;
import static com.fourseason.delivery.domain.member.exception.MemberErrorCode.MEMBER_NOT_FOUND;
import static com.fourseason.delivery.domain.order.entity.OrderStatus.CANCELED;
import static com.fourseason.delivery.domain.order.exception.OrderErrorCode.ORDER_NOT_FOUND;
import static com.fourseason.delivery.domain.shop.exception.ShopErrorCode.SHOP_NOT_FOUND;

import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.member.entity.Role;
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
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderManagerService implements OrderRoleService {

  private final MemberRepository memberRepository;
  private final ShopRepository shopRepository;
  private final OrderMenuListBuilder orderMenuListBuilder;
  private final OrderRepository orderRepository;
  private final OrderSearchRepositoryCustom orderSearchRepositoryCustom;

  @Override
  public Role supports() {
    return MANAGER;
  }

  @Transactional
  public UUID createOrder(ManagerCreateOrderRequestDto request) {
    Shop shop = findShopOrThrow(request.shopId());
    Member customer = resolveCustomer(request.customerId());
    List<OrderMenu> orderMenuList = orderMenuListBuilder.create(
        request.toOrderCreateDto(shop, customer));

    Order createOrder = Order.addByManager(
        shop,
        customer,
        request.address(),
        request.instruction(),
        orderMenuList,
        request.orderStatus(),
        request.orderType());

    return orderRepository.save(createOrder).getId();
  }

  @Override
  @Transactional(readOnly = true)
  public ManagerOrderDetailsResponseDto getOrder(UUID orderId, Long loginMemberId) {
    Order order = findOrderOrThrow(orderId);
    return ManagerOrderDetailsResponseDto.of(order);
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponseDto<ManagerOrderSummaryResponseDto> getOrderList(
      String username,
      String loginMemberUsername,
      PageRequestDto pageRequestDto,
      String keyword) {
    return orderSearchRepositoryCustom.findByManagerWithPage(
        username, pageRequestDto, keyword);
  }

  @Transactional(readOnly = true)
  public PageResponseDto<ManagerOrderSummaryResponseDto> searchOrderList(
      String customerUsername,
      UUID shopId,
      PageRequestDto pageRequestDto,
      String keyword
  ) {
    return orderSearchRepositoryCustom.searchByManagerWithPage(customerUsername, shopId,
        pageRequestDto, keyword);
  }

  @Override
  @Transactional
  public void cancelOrder(UUID orderId, Long loginMemberId) {
    Order order = findOrderOrThrow(orderId);
    order.assertAlreadyCanceled();
    order.updateStatus(CANCELED);
  }

  @Transactional
  public void deleteOrder(UUID orderId, String username) {
    Order order = findOrderOrThrow(orderId);
    order.assertAlreadyDeleted();
    order.deleteOf(username);
  }

  private Member resolveCustomer(Long customerId) {
    if (customerId == null) {
      return null;
    }
    return memberRepository.findByIdAndDeletedAtIsNull(customerId)
        .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
  }

  private Order findOrderOrThrow(UUID orderId) {
    return orderRepository.findByIdAndDeletedAtIsNull(orderId)
        .orElseThrow(() -> new CustomException(ORDER_NOT_FOUND));
  }

  private Shop findShopOrThrow(UUID shopId) {
    return shopRepository.findByIdAndDeletedAtIsNull(shopId)
        .orElseThrow(() -> new CustomException(SHOP_NOT_FOUND));
  }
}
