package com.fourseason.delivery.domain.order.controller;

import com.fourseason.delivery.domain.order.dto.request.OwnerCreateOrderRequestDto;
import com.fourseason.delivery.domain.order.dto.response.OwnerOrderDetailResponseDto;
import com.fourseason.delivery.domain.order.dto.response.OwnerOrderSummaryResponseDto;
import com.fourseason.delivery.domain.order.service.OrderOwnerService;
import com.fourseason.delivery.global.dto.PageRequestDto;
import com.fourseason.delivery.global.dto.PageResponseDto;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/owner/orders")
public class OrderOwnerController {

  private final OrderOwnerService orderOwnerService;

  /**
   * 점주 대면 주문 접수 대면으로 주문한 요청에 대해 점주가 직접 접수합니다. role: OWNER
   */
  @PostMapping
  public ResponseEntity<UUID> createOrder(
      @RequestBody @Valid OwnerCreateOrderRequestDto request,
      @RequestParam Long memberId // TODO: @AuthenticationPrincipal 로 변경,
  ) {
    UUID orderId = orderOwnerService.createOrder(request, memberId);
    return ResponseEntity.created(
        UriComponentsBuilder.fromUriString("/api/owner/orders/{orderId}")
            .buildAndExpand(orderId)
            .toUri()).build();
  }

  /**
   * 점주 주문 수락 API role: OWNER
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
   * 점주 주문 상세 조회 API 가게 주문을 상세 조회합니다.
   */
  @GetMapping("/{orderId}")
  public ResponseEntity<OwnerOrderDetailResponseDto> getOrder(
      @PathVariable UUID orderId,
      @RequestParam Long memberId
  ) {
    return ResponseEntity.ok(orderOwnerService.getOrder(orderId, memberId));
  }

  /**
   * 점주 주문 목록 조회 API 점주가 관리하는 가게들의 주문 목록을 조회합니다.
   *
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

  /**
   * 점주 주문 취소 API
   */
  @PostMapping("/{orderId}/cancel")
  public ResponseEntity<Void> cancelOrder(
      @PathVariable UUID orderId,
      @RequestParam Long memberId
  ) {
    orderOwnerService.cancelOrder(orderId, memberId);
    return ResponseEntity.ok().build();
  }
}