package com.fourseason.delivery.domain.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.order.entity.Order;
import com.fourseason.delivery.domain.payment.dto.external.ExternalPaymentDto;
import com.fourseason.delivery.domain.payment.dto.request.CancelPaymentRequestDto;
import com.fourseason.delivery.domain.payment.entity.Payment;
import com.fourseason.delivery.domain.payment.exception.PaymentErrorCode;
import com.fourseason.delivery.domain.payment.repository.PaymentRepository;
import com.fourseason.delivery.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class PaymentCompensatingService {

    private final PaymentRepository paymentRepository;
    private final PaymentExternalService paymentExternalService;


    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_BACKOFF = 1000L; // 1초

    @Async
    public CompletableFuture<URI> registerPaymentWithRetry(String paymentResult, Order order, Member member) {
        int attempt = 0;
        long backoffTime = INITIAL_BACKOFF;

        while (attempt < MAX_RETRIES) {
            try {
                attempt++;
                // 실제 비즈니스 로직
                System.out.println("Attempt #" + attempt);
                // 비즈니스 로직이 성공적으로 수행되면 종료
                URI result = registerPayment(paymentResult, order, member);
                return CompletableFuture.completedFuture(result);  // 비동기 결과 반환
            } catch (Exception e) {
                if (attempt == MAX_RETRIES) {
                    // 재시도 횟수를 초과했을 때 보상 작업 실행
                    compensatingCancel(paymentResult);
                } else {
                    // 지수 백오프 적용 (대기 시간 증가)
                    try {
                        System.out.println("Retrying after " + backoffTime + " ms...");
                        Thread.sleep(backoffTime);
                        backoffTime *= 2;  // 백오프 시간 지수적으로 증가
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        throw new CustomException(PaymentErrorCode.PAYMENT_COMPENSATE);
    }

    /**
     * 결제 등록
     */
    @Transactional
    public URI registerPayment(String paymentResult, Order order, Member member) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ExternalPaymentDto externalPaymentDto = objectMapper.readValue(paymentResult, ExternalPaymentDto.class);

            Payment payment = Payment.addOf(externalPaymentDto, order, member);
            paymentRepository.save(payment);

            return ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(payment.getId())
                    .toUri();
        } catch (JsonProcessingException e) {
            throw new CustomException(PaymentErrorCode.PAYMENT_MAPPING_FAIL);
        }
    }

    private void compensatingCancel(String paymentResult) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ExternalPaymentDto externalPaymentDto = objectMapper.readValue(paymentResult, ExternalPaymentDto.class);

            CancelPaymentRequestDto cancelPaymentRequestDto = new CancelPaymentRequestDto("서버 오류", null);

            String paymentKey = externalPaymentDto.paymentKey();
            paymentExternalService.cancelPayment(cancelPaymentRequestDto, paymentKey);
        } catch (JsonProcessingException e) {
            throw new CustomException(PaymentErrorCode.PAYMENT_MAPPING_FAIL);
        }
    }
}
