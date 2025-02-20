package com.fourseason.delivery.domain.order.controller;

import com.fourseason.delivery.domain.order.service.OrderOwnerService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
}