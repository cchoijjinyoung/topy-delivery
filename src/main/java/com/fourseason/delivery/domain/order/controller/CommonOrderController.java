package com.fourseason.delivery.domain.order.controller;

import com.fourseason.delivery.domain.order.dto.response.OrderDetailsResponseDto;
import com.fourseason.delivery.domain.order.dto.response.OrderSummaryResponseDto;
import com.fourseason.delivery.domain.order.service.OrderRoleServiceFinder;
import com.fourseason.delivery.global.auth.CustomPrincipal;
import com.fourseason.delivery.global.dto.PageRequestDto;
import com.fourseason.delivery.global.dto.PageResponseDto;
import com.fourseason.delivery.global.resolver.PageSize;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Validated
public class CommonOrderController {

  private final OrderRoleServiceFinder orderRoleServiceFinder;

  /**
   * 주문 상세 조회 API
   */
  @GetMapping("/{orderId}")
  public ResponseEntity<OrderDetailsResponseDto> getOrder(
      @PathVariable UUID orderId,
      @AuthenticationPrincipal CustomPrincipal principal
  ) {
    return ResponseEntity.ok(
        orderRoleServiceFinder.find(principal.getRole()).getOrder(orderId, principal.getId()));
  }

  /**
   * 주문 목록 조회 API
   */
  @GetMapping
  public ResponseEntity<PageResponseDto<? extends OrderSummaryResponseDto>> getOrderList(
      @RequestParam @NotBlank(message = "유저네임을 입력해주세요.") String username,
      @AuthenticationPrincipal CustomPrincipal principal,
      @RequestParam(defaultValue = "1") int page,
      @PageSize int size,
      @RequestParam(defaultValue = "latest") String orderBy,
      @RequestParam(required = false) String keyword
  ) {
    PageRequestDto pageRequestDto = PageRequestDto.of(page - 1, size, orderBy);
    return ResponseEntity.ok(orderRoleServiceFinder.find(principal.getRole())
        .getOrderList(username, principal.getName(), pageRequestDto, keyword));
  }

  /**
   * 주문 취소 API
   */
  @PostMapping("/{orderId}/cancel")
  public ResponseEntity<Void> cancelOrder(
      @PathVariable UUID orderId,
      @AuthenticationPrincipal CustomPrincipal principal
  ) {
    orderRoleServiceFinder.find(principal.getRole()).cancelOrder(orderId, principal.getId());
    return ResponseEntity.ok().build();
  }
}
