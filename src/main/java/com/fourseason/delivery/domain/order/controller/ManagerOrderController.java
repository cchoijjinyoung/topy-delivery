package com.fourseason.delivery.domain.order.controller;

import com.fourseason.delivery.domain.order.dto.request.ManagerCreateOrderRequestDto;
import com.fourseason.delivery.domain.order.dto.response.impl.ManagerOrderSummaryResponseDto;
import com.fourseason.delivery.domain.order.service.OrderManagerService;
import com.fourseason.delivery.global.auth.CustomPrincipal;
import com.fourseason.delivery.global.dto.PageRequestDto;
import com.fourseason.delivery.global.dto.PageResponseDto;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@Secured("ROLE_MANAGER")
@RequestMapping("/api/manager/orders")
public class ManagerOrderController {

  private final OrderManagerService orderManagerService;

  /**
   * 관리자 전용 주문 생성 API
   */
  @PostMapping
  public ResponseEntity<UUID> createOrder(
      @RequestBody @Valid ManagerCreateOrderRequestDto request
  ) {
    UUID orderId = orderManagerService.createOrder(request);
    return ResponseEntity.created(
        UriComponentsBuilder.fromUriString("/api/orders/{orderId}")
            .buildAndExpand(orderId)
            .toUri()).build();
  }

  /**
   * 관리자 전용 주문 목록 조회
   * @param customerUsername 주문 고객 username
   * @param shopId  주문 가게
   * @param keyword 검색 키워드
   */
  @GetMapping
  public ResponseEntity<PageResponseDto<ManagerOrderSummaryResponseDto>> getOrderList(
      @RequestParam(required = false) String customerUsername,
      @RequestParam(required = false) UUID shopId,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "latest") String orderBy,
      @RequestParam(required = false) String keyword
  ) {
    PageRequestDto pageRequestDto = PageRequestDto.of(page - 1, size, orderBy);
    return ResponseEntity.ok(
        orderManagerService.searchOrderList(customerUsername, shopId, pageRequestDto, keyword));
  }

  /**
   * 주문 삭제 API
   */
  @DeleteMapping("/{orderId}")
  public ResponseEntity<Void> deleteOrder(
      @PathVariable UUID orderId,
      @AuthenticationPrincipal CustomPrincipal principal
  ) {
    orderManagerService.deleteOrder(orderId, principal.getName());
    return ResponseEntity.ok().build();
  }
}