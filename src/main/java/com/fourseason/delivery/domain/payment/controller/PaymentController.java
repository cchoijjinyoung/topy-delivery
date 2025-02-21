package com.fourseason.delivery.domain.payment.controller;

import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.member.entity.Role;
import com.fourseason.delivery.domain.payment.dto.request.CreatePaymentRequestDto;
import com.fourseason.delivery.domain.payment.dto.response.PaymentResponseDto;
import com.fourseason.delivery.domain.payment.service.PaymentExternalService;
import com.fourseason.delivery.domain.payment.service.PaymentService;
import com.fourseason.delivery.global.auth.CustomPrincipal;
import com.fourseason.delivery.global.dto.PageRequestDto;
import com.fourseason.delivery.global.dto.PageResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<PageResponseDto<PaymentResponseDto>> getPaymentList(@RequestParam(defaultValue = "1") final int page,
                                                                              @RequestParam(defaultValue = "10") final int size,
                                                                              @RequestParam(defaultValue = "latest") final String orderBy,
                                                                              @AuthenticationPrincipal CustomPrincipal customPrincipal
                                                                    ) {
        PageRequestDto pageRequestDto = PageRequestDto.of(page-1, size, orderBy);
        return ResponseEntity.ok(paymentService.findPaymentList(pageRequestDto, customPrincipal.getName()));
    }

    /**
     * 사용자 결제 상세 조회
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponseDto> getPayment(@PathVariable final UUID paymentId,
                                                         @AuthenticationPrincipal CustomPrincipal customPrincipal
    ) {
        return ResponseEntity.ok(paymentService.getPayment(paymentId, customPrincipal.getName()));
    }

    /**
     * 결제 요청
     * example: http://localhost:8080/page/checkout?orderId=93f73279-8449-40ba-90c8-88e5180d891f&amount=20000&
     */
//    @GetMapping("/checkout/{orderId}")
//    public ResponseEntity<Void> checkoutPayment(@PathVariable final UUID orderId,
//                                                @AuthenticationPrincipal CustomPrincipal customPrincipal) {
//        URI location = paymentExternalService.checkoutPayment(orderId, customPrincipal.getName());
//        return ResponseEntity.status(HttpStatus.FOUND)
//                .location(location)
//                .build();
//    }

    /**
     * 결제 승인 (결제 등록)
     */
    @PostMapping("/confirm")
    public ResponseEntity<String> confirmPayment (
            @RequestBody @Valid final CreatePaymentRequestDto createPaymentRequestDto
    ) {
        String paymentResult = paymentExternalService.confirmPayment(createPaymentRequestDto);
        // 여기서 결제 db저장 이 처리를 비동기로 처리하는것이 best 실패시 재시도 보정도 좋음
        URI location = paymentService.registerPayment(paymentResult);
        return ResponseEntity.created(location).build();
    }

    /**
     * 결제 취소
     * 돌려주는 값이 없으므로 204상태 코드를 사용
     */
    @PutMapping("/{paymentId}")
    public ResponseEntity<Void> cancelPayment(@PathVariable final UUID paymentId,
                                              @AuthenticationPrincipal CustomPrincipal customPrincipal
    ) {
        URI location = paymentService.cancelPayment(paymentId, customPrincipal.getName());
        return ResponseEntity.noContent().location(location).build();

    }

    // 결제 삭제는 이루어질 일이 없을것 같기는 한데...
    /**
     * 결제 삭제
     */
    @DeleteMapping("/{paymentId}")
    public ResponseEntity<Void> deletePayment(@PathVariable final UUID paymentId,
                                              @AuthenticationPrincipal CustomPrincipal customPrincipal
    ) {
        paymentService.deletePayment(paymentId, customPrincipal.getName());
        return ResponseEntity.noContent().build();
    }
}
