package com.fourseason.delivery.domain.payment.controller;

import com.fourseason.delivery.domain.payment.dto.response.PaymentResponseDto;
import com.fourseason.delivery.domain.payment.service.PaymentService;
import com.fourseason.delivery.global.auth.CustomPrincipal;
import com.fourseason.delivery.global.dto.PageRequestDto;
import com.fourseason.delivery.global.dto.PageResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
public class AdminPaymentController {

    private final PaymentService paymentService;

    /**
     * 관리자 결제 전체 조회
     */
    @Secured({"ROLE_MANAGER", "ROLE_ADMIN"})
    @GetMapping
    public ResponseEntity<PageResponseDto<PaymentResponseDto>> getPaymentList(
            @RequestParam(defaultValue = "1") final int page,
            @RequestParam(defaultValue = "10") final int size,
            @RequestParam(defaultValue = "latest") final String orderBy
    ) {
        PageRequestDto pageRequestDto = PageRequestDto.of(page-1, size, orderBy);
        return ResponseEntity.ok(paymentService.findPaymentListForCustomer(pageRequestDto));
    }

    /**
     * 관리자 결졔 상세 조회
     */
    @Secured({"ROLE_MANAGER", "ROLE_ADMIN"})
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponseDto> getPayment(
            @PathVariable final UUID paymentId
    ) {
        return ResponseEntity.ok(paymentService.getPaymentForCustomer(paymentId));
    }

    /**
     * 결제 삭제 관리자
     */
    @Secured({"ROLE_MANAGER", "ROLE_ADMIN"})
    @DeleteMapping("/{paymentId}")
    public ResponseEntity<Void> deletePayment(
            @PathVariable final UUID paymentId,
            @AuthenticationPrincipal CustomPrincipal customPrincipal
    ) {
        paymentService.deletePayment(paymentId, customPrincipal);
        return ResponseEntity.noContent().build();
    }
}
