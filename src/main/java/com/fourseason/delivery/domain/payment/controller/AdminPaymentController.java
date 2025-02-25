package com.fourseason.delivery.domain.payment.controller;

import com.fourseason.delivery.domain.payment.dto.response.PaymentResponseDto;
import com.fourseason.delivery.domain.payment.service.PaymentService;
import com.fourseason.delivery.global.auth.CustomPrincipal;
import com.fourseason.delivery.global.dto.PageRequestDto;
import com.fourseason.delivery.global.dto.PageResponseDto;
import com.fourseason.delivery.global.resolver.PageSize;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
@Validated
public class AdminPaymentController {

    private final PaymentService paymentService;

    /**
     * 관리자 결제 전체 조회
     */
    @Secured({"ROLE_MANAGER", "ROLE_MASTER"})
    @GetMapping
    public ResponseEntity<PageResponseDto<PaymentResponseDto>> getPaymentList(
            @RequestParam(defaultValue = "1") final int page,
            @PageSize final int size,
            @RequestParam(defaultValue = "latest") final String orderBy
    ) {
        PageRequestDto pageRequestDto = PageRequestDto.of(page-1, size, orderBy);
        return ResponseEntity.ok(paymentService.findPaymentListForAdmin(pageRequestDto));
    }

    /**
     * 관리자 결제 전체 검색
     */
    @Secured({"ROLE_MANAGER", "ROLE_MASTER"})
    @GetMapping("/search")
    public ResponseEntity<PageResponseDto<PaymentResponseDto>> searchPaymentList(
            @RequestParam @NotBlank(message = "검색어를 입력해주세요.") String keyword,
            @RequestParam(defaultValue = "1") final int page,
            @PageSize final int size,
            @RequestParam(defaultValue = "latest") final String orderBy
    ) {
        PageRequestDto pageRequestDto = PageRequestDto.of(page-1, size, orderBy);
        return ResponseEntity.ok(paymentService.searchPaymentListForAdmin(pageRequestDto, keyword));
    }

    /**
     * 관리자 결제 상세 조회
     */
    @Secured({"ROLE_MANAGER", "ROLE_MASTER"})
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponseDto> getPayment(
            @PathVariable final UUID paymentId
    ) {
        return ResponseEntity.ok(paymentService.getPaymentForCustomer(paymentId));
    }

    /**
     * 결제 삭제 관리자
     */
    @Secured({"ROLE_MANAGER", "ROLE_MASTER"})
    @DeleteMapping("/{paymentId}")
    public ResponseEntity<Void> deletePayment(
            @PathVariable final UUID paymentId,
            @AuthenticationPrincipal CustomPrincipal customPrincipal
    ) {
        paymentService.deletePayment(paymentId, customPrincipal);
        return ResponseEntity.noContent().build();
    }
}
