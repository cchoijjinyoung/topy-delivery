package com.fourseason.delivery.domain.order.controller;

import com.fourseason.delivery.domain.order.dto.request.CreateOrderRequestDto;
import com.fourseason.delivery.domain.order.dto.response.OrderResponseDto;
import com.fourseason.delivery.domain.order.service.OrderCustomerService;
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
@RequestMapping("/api/customer/orders")
public class OrderCustomerController {

  private final OrderCustomerService orderCustomerService;

  /**
   * 고객 주문 요청 API role: CUSTOMER
   */
  @PostMapping
  public ResponseEntity<UUID> createOrder(
      @RequestBody @Valid CreateOrderRequestDto request,
      @RequestParam Long memberId // TODO: @AuthenticationPrincipal 로 변경
  ) {
    UUID orderId = orderCustomerService.createOrder(request, memberId);
    return ResponseEntity.created(
        UriComponentsBuilder.fromUriString("/api/customer/orders/{orderId}")
            .buildAndExpand(orderId)
            .toUri()).build();
  }

  /**
   * 고객의 주문 상세 조회 API role: CUSTOMER
   */
  @GetMapping("/{orderId}")
  public ResponseEntity<OrderResponseDto> getOrder(
      @PathVariable UUID orderId,
      @RequestParam Long memberId
  ) {
    return ResponseEntity.ok(orderCustomerService.getOrder(orderId, memberId));
  }

  /**
   * 고객 주문 목록 조회 API role: CUSTOMER
   */
  @GetMapping
  public ResponseEntity<PageResponseDto<OrderResponseDto>> getOrderList(
      @RequestParam Long memberId,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "latest") String orderBy) {
    PageRequestDto pageRequestDto = PageRequestDto.of(page - 1, size, orderBy);
    return ResponseEntity.ok(orderCustomerService.getOrderList(memberId, pageRequestDto));
  }
}
