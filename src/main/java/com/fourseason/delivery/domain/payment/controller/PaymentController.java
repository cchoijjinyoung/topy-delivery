package com.fourseason.delivery.domain.payment.controller;

import com.fourseason.delivery.domain.payment.dto.request.CancelPaymentRequestDto;
import com.fourseason.delivery.domain.payment.dto.request.CreatePaymentRequestDto;
import com.fourseason.delivery.domain.payment.dto.response.PaymentResponseDto;
import com.fourseason.delivery.domain.payment.service.PaymentExternalService;
import com.fourseason.delivery.domain.payment.service.PaymentService;
import com.fourseason.delivery.global.auth.CustomPrincipal;
import com.fourseason.delivery.global.dto.PageRequestDto;
import com.fourseason.delivery.global.dto.PageResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentExternalService paymentExternalService;


    /**
     * 사용자 결제 전체 조회
     */
    @GetMapping()
    public ResponseEntity<PageResponseDto<PaymentResponseDto>> getPaymentList(
            @RequestParam(defaultValue = "1") final int page,
            @RequestParam(defaultValue = "10") final int size,
            @RequestParam(defaultValue = "latest") final String orderBy,
            @AuthenticationPrincipal CustomPrincipal customPrincipal
    ) {
        PageRequestDto pageRequestDto = PageRequestDto.of(page-1, size, orderBy);
        return ResponseEntity.ok(paymentService.findPaymentList(pageRequestDto, customPrincipal));
    }

    /**
     * 사용자 결제 상세 조회
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponseDto> getPayment(
            @PathVariable final UUID paymentId,
            @AuthenticationPrincipal CustomPrincipal customPrincipal
    ) {
        return ResponseEntity.ok(paymentService.getPayment(paymentId, customPrincipal));
    }

    /**
     * 결제 요청
     * example: localhost:8080/page/checkout?orderId={orderId}&jwt={jwtToken}
     */

    /**
     * 결제 승인 (결제 등록)
     */
    @PostMapping()
    public ResponseEntity<String> confirmPayment (
            @RequestBody @Valid final CreatePaymentRequestDto createPaymentRequestDto,
            @AuthenticationPrincipal CustomPrincipal customPrincipal
    ) {
        String paymentResult = paymentExternalService.confirmPayment(createPaymentRequestDto, customPrincipal);
        System.out.println(paymentResult);
        // 여기서 결제 db저장 이 처리를 비동기로 처리하는것이 best 실패시 재시도 보정도 좋음
        URI location = paymentService.registerPayment(paymentResult, customPrincipal.getId());
        return ResponseEntity.created(location).build();
    }

    /**
     * 결제 취소 고객 기준으로는 order 상태에 따라 결제 취소가 가능하도록 함
     */
    @PutMapping("/{paymentId}")
    public ResponseEntity<Void> cancelPayment(
            @PathVariable final UUID paymentId,
            @RequestBody final CancelPaymentRequestDto cancelPaymentRequestDto,
            @AuthenticationPrincipal CustomPrincipal customPrincipal
    ) {
        String paymentResult = paymentExternalService.cancelPayment(paymentId, cancelPaymentRequestDto, customPrincipal);
        System.out.println(paymentResult);
        URI location = paymentService.cancelPayment(paymentId, paymentResult, customPrincipal);
        return ResponseEntity.ok().location(location).build();

    }


    /**
     * 관리자 결제 전체 조회
     */
    @Secured({"ROLE_MANAGER", "ROLE_ADMIN"})
    @GetMapping("/admin")
    public ResponseEntity<PageResponseDto<PaymentResponseDto>> getPaymentList(
            @RequestParam(defaultValue = "1") final int page,
            @RequestParam(defaultValue = "10") final int size,
            @RequestParam(defaultValue = "latest") final String orderBy
    ) {
        PageRequestDto pageRequestDto = PageRequestDto.of(page-1, size, orderBy);
        return ResponseEntity.ok(paymentService.findPaymentList(pageRequestDto));
    }

    /**
     * 관리자 결졔 상세 조회
     */
    @Secured({"ROLE_MANAGER", "ROLE_ADMIN"})
    @GetMapping("/{paymentId}/admin")
    public ResponseEntity<PaymentResponseDto> getPayment(
            @PathVariable final UUID paymentId
    ) {
        return ResponseEntity.ok(paymentService.getPayment(paymentId));
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
