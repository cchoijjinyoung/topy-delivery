package com.fourseason.delivery.domain.payment.controller;

import com.fourseason.delivery.domain.payment.dto.request.CancelPaymentRequestDto;
import com.fourseason.delivery.domain.payment.dto.request.CreatePaymentRequestDto;
import com.fourseason.delivery.domain.payment.dto.response.PaymentResponseDto;
import com.fourseason.delivery.domain.payment.service.PaymentService;
import com.fourseason.delivery.global.auth.CustomPrincipal;
import com.fourseason.delivery.global.dto.PageRequestDto;
import com.fourseason.delivery.global.dto.PageResponseDto;
import com.fourseason.delivery.global.resolver.PageSize;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.WebAsyncTask;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/customer/payments")
@RequiredArgsConstructor
@Validated
public class CustomerPaymentController {

    private final PaymentService paymentService;


    /**
     * 사용자 결제 전체 조회
     */
    @GetMapping
    public ResponseEntity<PageResponseDto<PaymentResponseDto>> getPaymentList(
            @RequestParam(defaultValue = "1") final int page,
            @PageSize final int size,
            @RequestParam(defaultValue = "latest") final String orderBy,
            @AuthenticationPrincipal CustomPrincipal customPrincipal
    ) {
        PageRequestDto pageRequestDto = PageRequestDto.of(page-1, size, orderBy);
        return ResponseEntity.ok(paymentService.findPaymentListForCustomer(pageRequestDto, customPrincipal));
    }

    /**
     * 사용자 결제 상세 조회
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponseDto> getPayment(
            @PathVariable final UUID paymentId,
            @AuthenticationPrincipal CustomPrincipal customPrincipal
    ) {
        return ResponseEntity.ok(paymentService.getPaymentForCustomer(paymentId, customPrincipal));
    }

    /**
     * 결제 요청
     * example: localhost:8080/page/checkout?orderId={orderId}&jwt={jwtToken}
     */

    /**
     * 결제 승인 (결제 등록)
     */
    @PostMapping
    public WebAsyncTask<ResponseEntity<String>> confirmPayment (
            @RequestBody @Valid final CreatePaymentRequestDto createPaymentRequestDto,
            @AuthenticationPrincipal CustomPrincipal customPrincipal
    ) {
        return new WebAsyncTask<>(() -> {
            // 비동기 작업 수행
            CompletableFuture<URI> locationFuture = paymentService.confirmAndRegister(createPaymentRequestDto, customPrincipal);
            // 결과 대기 및 반환
            URI location = locationFuture.get();
            return ResponseEntity.created(location).build();
        });
    }

    /**
     * 결제 취소 고객
     */
    @PutMapping("/{paymentId}")
    public ResponseEntity<Void> cancelPayment(
            @PathVariable final UUID paymentId,
            @RequestBody final CancelPaymentRequestDto cancelPaymentRequestDto,
            @AuthenticationPrincipal CustomPrincipal customPrincipal
    ) {
        URI location = paymentService.cancelPaymentForCustomer(paymentId, cancelPaymentRequestDto, customPrincipal);
        return ResponseEntity.ok().location(location).build();
    }
}
