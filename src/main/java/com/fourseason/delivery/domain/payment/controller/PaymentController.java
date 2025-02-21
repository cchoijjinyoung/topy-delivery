package com.fourseason.delivery.domain.payment.controller;

import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.member.entity.Role;
import com.fourseason.delivery.domain.payment.dto.request.CreatePaymentRequestDto;
import com.fourseason.delivery.domain.payment.dto.response.PaymentResponseDto;
import com.fourseason.delivery.domain.payment.service.PaymentExternalService;
import com.fourseason.delivery.domain.payment.service.PaymentService;
import com.fourseason.delivery.global.dto.PageRequestDto;
import com.fourseason.delivery.global.dto.PageResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentExternalService paymentExternalService;

    // Todo: 임시User 사용 회원가입 절차 적용후 수정 필요
    Member testMember = new Member("유저", "user@example.com", "1234", "010-0000-0000", Role.CUSTOMER);

    /**
     * 사용자 결제 전체 조회
     */
    @GetMapping()
    public ResponseEntity<PageResponseDto<PaymentResponseDto>> getPaymentList(@RequestParam(defaultValue = "1") final int page,
                                                                               @RequestParam(defaultValue = "10") final int size,
                                                                               @RequestParam(defaultValue = "latest") final String orderBy
//                                                                   @AuthenticationPrincipal UserDetailsImpl userDetails
                                                                    ) {
        PageRequestDto pageRequestDto = PageRequestDto.of(page-1, size, orderBy);
        return ResponseEntity.ok(paymentService.findPaymentList(pageRequestDto, testMember));
    }

    /**
     * 사용자 결제 상세 조회
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponseDto> getPayment(@PathVariable final UUID paymentId
//                                                         @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.ok(paymentService.getPayment(paymentId, testMember));
    }

    /**
     * 결제 요청
     */
    @GetMapping("/checkout/{orderId}")
    public ResponseEntity<Void> checkoutPayment(@PathVariable final UUID orderId) {
        URI location = paymentExternalService.checkoutPayment(orderId);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(location)
                .build();
    }

    /**
     * 결제 승인 (결제 등록)
     */
    @PostMapping("/confirm")
    public ResponseEntity<String> confirmPayment (
            @RequestBody @Valid final CreatePaymentRequestDto createPaymentRequestDto
//            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return paymentExternalService.confirmPayment(createPaymentRequestDto, testMember);
    }

    /**
     * 결제 취소
     * 돌려주는 값이 없으므로 204상태 코드를 사용
     */
    @PutMapping("/{paymentId}")
    public ResponseEntity<Void> cancelPayment(@PathVariable final UUID paymentId
//                                              @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        URI location = paymentService.cancelPayment(paymentId, testMember);
        return ResponseEntity.noContent().location(location).build();

    }

    /**
     * 결제 삭제
     */
    @DeleteMapping("/{paymentId}")
    public ResponseEntity<Void> deletePayment(@PathVariable final UUID paymentId
//            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        paymentService.deletePayment(paymentId, testMember);
        return ResponseEntity.noContent().build();
    }
}
