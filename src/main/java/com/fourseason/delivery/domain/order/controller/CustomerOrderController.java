package com.fourseason.delivery.domain.order.controller;

import com.fourseason.delivery.domain.order.dto.request.CustomerCreateOrderRequestDto;
import com.fourseason.delivery.domain.order.service.OrderCustomerService;
import com.fourseason.delivery.global.auth.CustomPrincipal;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customer/orders")
public class CustomerOrderController {

  private final OrderCustomerService orderCustomerService;

  /**
   * 주문 요청 API
   */
  @PostMapping
  public ResponseEntity<UUID> submitOrder(
      @RequestBody @Valid CustomerCreateOrderRequestDto request,
      @AuthenticationPrincipal CustomPrincipal principal
  ) {
    UUID orderId = orderCustomerService.createOnlineOrder(request, principal.getId());
    return ResponseEntity.created(
        UriComponentsBuilder.fromUriString("/api/orders/{orderId}")
            .buildAndExpand(orderId)
            .toUri()).build();
  }
}
