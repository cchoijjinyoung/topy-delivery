package com.fourseason.delivery.domain.order.controller;

import com.fourseason.delivery.domain.order.dto.response.OrderDetailResponseDto;
import com.fourseason.delivery.domain.order.dto.response.OrderSummaryResponseDto;
import com.fourseason.delivery.domain.order.dto.response.OwnerOrderDetailResponseDto;
import com.fourseason.delivery.domain.order.dto.response.OwnerOrderSummaryResponseDto;
import com.fourseason.delivery.domain.order.service.OrderOwnerService;
import com.fourseason.delivery.global.dto.PageRequestDto;
import com.fourseason.delivery.global.dto.PageResponseDto;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/owner/orders")
public class OrderOwnerController {

  private final OrderOwnerService orderOwnerService;

  /**
   * 주문 수락 API
   * role: OWNER
   */
  @PostMapping("{orderId}/accept")
  public ResponseEntity<UUID> acceptOrder(
      @RequestParam Long memberId, // TODO: @AuthenticationPrincipal 로 변경,
      @PathVariable UUID orderId
  ) {
    orderOwnerService.acceptOrder(memberId, orderId);
    return ResponseEntity.ok().build();
  }

  /**
   * 점주 주문 상세 조회 API
   * 가게 주문을 상세 조회합니다.
   */
  @GetMapping("/{orderId}")
  public ResponseEntity<OwnerOrderDetailResponseDto> getOrder(
      @PathVariable UUID orderId,
      @RequestParam Long memberId
  ) {
    return ResponseEntity.ok(orderOwnerService.getOrder(orderId, memberId));
  }

  /**
   * 점주 주문 목록 조회 API
   * 점주가 관리하는 가게들의 주문 목록을 조회합니다.
   * @param shopId 가게로 필터링하기 위한 검색 조건
   */
  @GetMapping
  public ResponseEntity<PageResponseDto<OwnerOrderSummaryResponseDto>> getOrderList(
      @RequestParam Long memberId,
      @RequestParam(required = false) UUID shopId,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "latest") String orderBy) {
    PageRequestDto pageRequestDto = PageRequestDto.of(page - 1, size, orderBy);
    return ResponseEntity.ok(orderOwnerService.getOrderList(memberId, shopId, pageRequestDto));
  }
}