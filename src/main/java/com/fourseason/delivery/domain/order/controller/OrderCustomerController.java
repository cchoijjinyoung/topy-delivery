package com.fourseason.delivery.domain.order.controller;

import com.fourseason.delivery.domain.order.dto.request.CreateOrderRequestDto;
import com.fourseason.delivery.domain.order.service.OrderCustomerService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customer/orders")
public class OrderCustomerController {

  private final OrderCustomerService orderCustomerService;

  /**
   * 주문 요청 API
   * role: CUSTOMER
   */
  @PostMapping
  public ResponseEntity<UUID> createOrder(
      @RequestBody @Valid CreateOrderRequestDto request,
      @RequestParam Long memberId // TODO: @AuthenticationPrincipal 로 변경
  ) {
    return ResponseEntity.ok(orderCustomerService.createOrder(request, memberId));
  }
}
