package com.fourseason.delivery.domain.payment.controller;

import com.fourseason.delivery.domain.payment.dto.request.CancelPaymentRequestDto;
import com.fourseason.delivery.domain.payment.dto.response.PaymentResponseDto;
import com.fourseason.delivery.domain.payment.service.PaymentService;
import com.fourseason.delivery.global.auth.CustomPrincipal;
import com.fourseason.delivery.global.dto.PageRequestDto;
import com.fourseason.delivery.global.dto.PageResponseDto;
import com.fourseason.delivery.global.resolver.PageSize;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/owner/payments")
@RequiredArgsConstructor
public class OwnerPaymentController {

    private final PaymentService paymentService;


    /**
     * 가게 결제 전체 조회
     */
    @Secured("ROLE_OWNER")
    @GetMapping("shops/{shopId}")
    public ResponseEntity<PageResponseDto<PaymentResponseDto>> getPaymentList(
            @PathVariable final UUID shopId,
            @RequestParam(defaultValue = "1") final int page,
            @PageSize final int size,
            @RequestParam(defaultValue = "latest") final String orderBy,
            @AuthenticationPrincipal CustomPrincipal customPrincipal
    ) {
        PageRequestDto pageRequestDto = PageRequestDto.of(page-1, size, orderBy);
        return ResponseEntity.ok(paymentService.findPaymentListByShop(pageRequestDto, shopId, customPrincipal));
    }

    /**
     * 가게 결졔 상세 조회
     */
    @Secured("ROLE_OWNER")
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponseDto> getPayment(
            @PathVariable final UUID paymentId,
            @AuthenticationPrincipal CustomPrincipal customPrincipal
    ) {
        return ResponseEntity.ok(paymentService.getPaymentListForOwner(paymentId, customPrincipal));
    }

    /**
     * 결제 취소 가게주인
     */
    @Secured("ROLE_OWNER")
    @PutMapping("/{paymentId}")
    public ResponseEntity<Void> cancelPayment(
            @PathVariable final UUID paymentId,
            @RequestBody final CancelPaymentRequestDto cancelPaymentRequestDto,
            @AuthenticationPrincipal CustomPrincipal customPrincipal
    ) {
        URI location = paymentService.cancelPaymentForOwner(paymentId, cancelPaymentRequestDto, customPrincipal);
        return ResponseEntity.ok().location(location).build();
    }
}
