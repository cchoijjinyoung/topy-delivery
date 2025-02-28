package com.fourseason.delivery.domain.order.controller;

import com.fourseason.delivery.domain.order.dto.request.AcceptOfflineOrderRequestDto;
import com.fourseason.delivery.domain.order.dto.response.impl.OwnerOrderSummaryResponseDto;
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
@PreAuthorize("hasRole('ROLE_OWNER')")
@RequestMapping("/api/owner/orders")
public class OwnerOrderController {

  private final OrderService orderService;

  /**
   * 오프라인 주문 접수
   */
  @PostMapping
  public ResponseEntity<Void> acceptOfflineOrder(
      @RequestBody AcceptOfflineOrderRequestDto request,
      @AuthenticationPrincipal CustomPrincipal principal
  ) {
    UUID createdOrderId = orderService.createOfflineOrder(request, principal.getId());
    return ResponseEntity.created(
        UriComponentsBuilder.fromUriString("/api/orders/{orderId}")
            .buildAndExpand(createdOrderId)
            .toUri()).build();
  }

  /**
   * 점주가 고객이 요청한 주문을 수락
   */
  @PostMapping("{orderId}/accept")
  public ResponseEntity<Void> acceptOrder(
      @PathVariable UUID orderId,
      @AuthenticationPrincipal CustomPrincipal principal
  ) {
    orderService.acceptOrder(orderId, principal.getId());
    return ResponseEntity.ok().build();
  }

  /**
   * 점주 주문 목록 조회 API 점주가 관리하는 가게들의 주문 목록을 조회합니다.
   * @param shopId 가게로 필터링하기 위한 검색 조건
   */
  @GetMapping
  public ResponseEntity<PageResponseDto<OwnerOrderSummaryResponseDto>> searchOrderList(
      @AuthenticationPrincipal CustomPrincipal principal,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) UUID shopId,
      @RequestParam(defaultValue = "1") int page,
      @PageSize int size,
      @RequestParam(defaultValue = "latest") String orderBy) {
    PageRequestDto pageRequestDto = PageRequestDto.of(page - 1, size, orderBy);
    return ResponseEntity.ok(
        orderService.searchBy(principal.getId(), shopId, pageRequestDto, keyword));
  }
}