package com.fourseason.delivery.domain.order.service;

import static com.fourseason.delivery.domain.member.entity.Role.OWNER;
import static com.fourseason.delivery.domain.order.entity.OrderStatus.ACCEPTED;
import static com.fourseason.delivery.domain.order.entity.OrderStatus.CANCELED;
import static com.fourseason.delivery.domain.order.exception.OrderErrorCode.NOT_PAID_ORDER;
import static com.fourseason.delivery.domain.order.exception.OrderErrorCode.NOT_READ_ORDER_PERMISSION;
import static com.fourseason.delivery.domain.order.exception.OrderErrorCode.NOT_SHOP_OWNER;
import static com.fourseason.delivery.domain.order.exception.OrderErrorCode.ORDER_NOT_FOUND;
import static com.fourseason.delivery.domain.payment.exception.PaymentErrorCode.PAYMENT_NOT_FOUND;
import static com.fourseason.delivery.domain.shop.exception.ShopErrorCode.SHOP_NOT_FOUND;

import com.fourseason.delivery.domain.member.entity.Role;
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
public class OrderOwnerService implements OrderRoleService {

  private final ShopRepository shopRepository;
  private final OrderMenuListBuilder orderMenuListBuilder;
  private final OrderRepository orderRepository;
  private final PaymentRepository paymentRepository;
  private final OrderSearchRepositoryCustom orderSearchRepositoryCustom;

  @Override
  public Role supports() {
    return OWNER;
  }

  @Transactional
  public UUID createOfflineOrder(OwnerCreateOrderRequestDto request, Long ownerId) {
    Shop shop = findShopOrThrow(request.shopId());
    if (!shop.getMember().getId().equals(ownerId)) {
      throw new CustomException(NOT_SHOP_OWNER);
    }
    List<OrderMenu> orderMenuList = orderMenuListBuilder.create(request.toOrderCreateDto(shop));

    Order createOrder = Order.addByOwner(
        shop,
        request.address(),
        request.instruction(),
        orderMenuList
    );
    return orderRepository.save(createOrder).getId();
  }

  @Transactional
  public void acceptOrder(UUID orderId, Long ownerId) {
    Order order = findOrderOrThrow(orderId);
    order.assertShopOwner(ownerId);
    order.assertOrderIsPending();

    Payment payment = findPaymentOrThrow(order);
    if (!payment.getPaymentStatus().equals("DONE")) {
      throw new CustomException(NOT_PAID_ORDER);
    }
    order.updateStatus(ACCEPTED);
  }

  @Override
  @Transactional(readOnly = true)
  public OwnerOrderDetailsResponseDto getOrder(UUID orderId, Long memberId) {
    Order order = findOrderOrThrow(orderId);
    order.assertOwnerOrCustomer(memberId);
    return OwnerOrderDetailsResponseDto.of(order);
  }

  @Override
  public PageResponseDto<OwnerOrderSummaryResponseDto> getOrderList(
      String username,
      String loginMemberUsername,
      PageRequestDto pageRequestDto,
      String keyword
  ) {
    if (!username.equals(loginMemberUsername)) {
      throw new CustomException(NOT_READ_ORDER_PERMISSION);
    }
    return orderSearchRepositoryCustom.findByOwnerWithPage(username, pageRequestDto, keyword);
  }

  @Transactional(readOnly = true)
  public PageResponseDto<OwnerOrderSummaryResponseDto> searchOrderList(
      Long memberId,
      UUID shopId,
      PageRequestDto pageRequestDto,
      String keyword
  ) {
    if (shopId != null) {
      Shop shop = findShopOrThrow(shopId);

      if (!shop.getMember().getId().equals(memberId)) {
        throw new CustomException(NOT_SHOP_OWNER);
      }
    }
    return orderSearchRepositoryCustom.searchByOwnerWithPage(
        memberId, shopId, pageRequestDto, keyword);
  }

  @Override
  @Transactional
  public void cancelOrder(UUID orderId, Long memberId) {
    Order order = findOrderOrThrow(orderId);
    order.assertOwnerOrCustomer(memberId);
    order.updateStatus(CANCELED);
  }

  private Payment findPaymentOrThrow(Order order) {
    return paymentRepository.findByOrderAndDeletedAtIsNull(order)
        .orElseThrow(() -> new CustomException(PAYMENT_NOT_FOUND));
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
