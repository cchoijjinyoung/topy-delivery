package com.fourseason.delivery.domain.order.service;

import static com.fourseason.delivery.domain.order.entity.OrderStatus.ACCEPTED;
import static com.fourseason.delivery.domain.order.exception.OrderErrorCode.ORDER_NOT_FOUND;

import com.fourseason.delivery.domain.order.dto.response.OwnerOrderDetailResponseDto;
import com.fourseason.delivery.domain.order.dto.response.OwnerOrderSummaryResponseDto;
import com.fourseason.delivery.domain.order.entity.Order;
import com.fourseason.delivery.domain.order.repository.OrderRepository;
import com.fourseason.delivery.domain.order.repository.OwnerOrderRepositoryCustom;
import com.fourseason.delivery.global.dto.PageRequestDto;
import com.fourseason.delivery.global.dto.PageResponseDto;
import com.fourseason.delivery.global.exception.CustomException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderOwnerService {

  private final OrderRepository orderRepository;
  private final OwnerOrderRepositoryCustom ownerOrderRepositoryCustom;

  @Transactional
  public void acceptOrder(Long ownerId, UUID orderId) {

    Order order = orderRepository.findById(orderId).
        orElseThrow(() -> new CustomException(ORDER_NOT_FOUND));

    order.assertShopOwner(ownerId);
    order.assertOrderIsPending();

    order.updateStatus(ACCEPTED);
  }

  @Transactional(readOnly = true)
  public OwnerOrderDetailResponseDto getOrder(UUID orderId, Long memberId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new CustomException(ORDER_NOT_FOUND));

    order.assertShopOwner(memberId);

    return OwnerOrderDetailResponseDto.of(order);
  }

  @Transactional(readOnly = true)
  public PageResponseDto<OwnerOrderSummaryResponseDto> getOrderList(
      Long memberId,
      UUID shopId,
      PageRequestDto pageRequestDto
  ) {
    return ownerOrderRepositoryCustom.findOrderListWithPage(memberId, shopId, pageRequestDto);
  }
}