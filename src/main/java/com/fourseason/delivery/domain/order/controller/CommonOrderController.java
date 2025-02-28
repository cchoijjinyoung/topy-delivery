package com.fourseason.delivery.domain.order.controller;

import com.fourseason.delivery.domain.order.dto.request.SubmitOrderRequestDto;
import com.fourseason.delivery.domain.order.dto.response.OrderDetailsResponseDto;
import com.fourseason.delivery.domain.order.dto.response.OrderSummaryResponseDto;
import com.fourseason.delivery.domain.order.service.OrderService;
import com.fourseason.delivery.global.auth.CustomPrincipal;
import com.fourseason.delivery.global.dto.PageRequestDto;
import com.fourseason.delivery.global.dto.PageResponseDto;
import com.fourseason.delivery.global.resolver.PageSize;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ROLE_CUSTOMER', 'ROLE_OWNER', 'ROLE_MANAGER')")
@RequestMapping("/api/orders")
public class CommonOrderController {

  private final OrderService orderService;

  /**
   * 온라인 주문 요청
   */
  @PostMapping
  public ResponseEntity<Void> submitOrder(
      @RequestBody SubmitOrderRequestDto request,
      @AuthenticationPrincipal CustomPrincipal principal
  ) {
    UUID createdOrderId = orderService.createOnlineOrder(request, principal.getId());
    return ResponseEntity.created(
        UriComponentsBuilder.fromUriString("/api/orders/{orderId}")
        .buildAndExpand(createdOrderId)
        .toUri()).build();
  }

  /**
   * 주문 상세 조회
   */
  @GetMapping("/{orderId}")
  public ResponseEntity<OrderDetailsResponseDto> getOrder(
      @PathVariable UUID orderId,
      @AuthenticationPrincipal CustomPrincipal principal
  ) {
    return ResponseEntity.ok(orderService.readOne(orderId, principal));
  }

  /**
   * 내 주문 목록 조회
   */
  @GetMapping("/me")
  public ResponseEntity<PageResponseDto<? extends OrderSummaryResponseDto>> getMyOrderList(
      @AuthenticationPrincipal CustomPrincipal principal,
      @RequestParam(defaultValue = "1") int page,
      @PageSize int size,
      @RequestParam(defaultValue = "latest") String orderBy,
      @RequestParam(required = false) String keyword
  ) {
    PageRequestDto pageRequestDto = PageRequestDto.of(page - 1, size, orderBy);
    return ResponseEntity.ok(orderService.searchBy(principal, pageRequestDto, keyword));
  }

  /**
   * 주문 취소 API
   */
  @PostMapping("/{orderId}/cancel")
  public ResponseEntity<Void> cancelOrder(
      @PathVariable UUID orderId,
      @AuthenticationPrincipal CustomPrincipal principal
  ) {
    orderService.cancelOrder(orderId, principal);
    return ResponseEntity.ok().build();
  }
}
