package com.fourseason.delivery.domain.payment.service;

import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.member.exception.MemberErrorCode;
import com.fourseason.delivery.domain.member.repository.MemberRepository;
import com.fourseason.delivery.domain.order.entity.Order;
import com.fourseason.delivery.domain.order.exception.OrderErrorCode;
import com.fourseason.delivery.domain.order.repository.OrderRepository;
import com.fourseason.delivery.domain.payment.dto.request.CancelPaymentRequestDto;
import com.fourseason.delivery.domain.payment.dto.request.CreatePaymentRequestDto;
import com.fourseason.delivery.domain.payment.entity.Payment;
import com.fourseason.delivery.domain.payment.exception.CustomRestClientException;
import com.fourseason.delivery.domain.payment.exception.PaymentErrorCode;
import com.fourseason.delivery.domain.payment.repository.PaymentRepository;
import com.fourseason.delivery.global.auth.CustomPrincipal;
import com.fourseason.delivery.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentExternalService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final MemberRepository memberRepository;

    String widgetSecretKey = "test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6";
    Base64.Encoder encoder = Base64.getEncoder();
    byte[] encodedBytes = encoder.encode((widgetSecretKey + ":").getBytes(StandardCharsets.UTF_8));
    String authorizations = "Basic " + new String(encodedBytes);


    /**
     * tossPayment를 통해 결제 승인, payment객체를 받아옴
     */
    public String confirmPayment(CreatePaymentRequestDto createPaymentRequestDto, CustomPrincipal customPrincipal) {
        // 결제 검증
        checkConfirm(createPaymentRequestDto, customPrincipal.getId());
        // 승인 요청
        RestClient restClient = RestClient.create();
        try {
            return restClient.post()
                    .uri("https://api.tosspayments.com/v1/payments/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", authorizations)
                    .body(createPaymentRequestDto)
                    .retrieve()
                    .body(String.class);

        // 외부 api 호출 과정에서 internal error 발생시
        } catch (HttpStatusCodeException e) {
            throw new CustomRestClientException(e.getStatusCode(), e.getResponseBodyAsString());
        }
    }

    /**
     * 결제 취소
     */
    public String cancelPayment(UUID paymentId, CancelPaymentRequestDto cancelPaymentRequestDto, CustomPrincipal customPrincipal) {
        // 취소 검증
        Payment payment = checkCancel(paymentId, customPrincipal.getId());
        // 취소 요청
        RestClient restClient = RestClient.create();
        try {
            return restClient.post()
                    .uri("https://api.tosspayments.com//v1/payments/" + payment.getPaymentKey() + "/cancel")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", authorizations)
                    .body(cancelPaymentRequestDto)
                    .retrieve()
                    .body(String.class);

            // 외부 api 호출 과정에서 internal error 발생시
        } catch (HttpServerErrorException e) {
            throw new CustomRestClientException(e.getStatusCode(), e.getResponseBodyAsString());
        }
    }


    /**
     * 결제 시 검증
     */
    private void checkConfirm(CreatePaymentRequestDto createPaymentRequestDto, Long memberId){
        Order order = orderRepository.findById(createPaymentRequestDto.orderId())
                .orElseThrow(() ->
                        new CustomException(OrderErrorCode.ORDER_NOT_FOUND)
                );
        if (order.getMember().getDeletedAt() != null) {
            throw new CustomException(MemberErrorCode.MEMBER_NOT_FOUND);
        }
        if (!order.getMember().getId().equals(checkMember(memberId).getId())) {
            throw new CustomException(OrderErrorCode.NOT_ORDERED_BY_CUSTOMER);
            // NOT_ORDERED_BY_CUSTOMER forbiden이 맞지 않을까용?
        }
        if (order.getTotalPrice() != createPaymentRequestDto.amount()) {
            throw new CustomException(PaymentErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
    }

    private Payment checkCancel(UUID paymentId, Long memberId) {
        Payment payment = paymentRepository.findByIdAndDeletedAtIsNull(paymentId)
                .orElseThrow(
                        () -> new CustomException(PaymentErrorCode.PAYMENT_NOT_FOUND));
        if (!payment.getMember().getId().equals(checkMember(memberId).getId())) {
            throw new CustomException(PaymentErrorCode.PAYMENT_FORBIDDEN);
        }

        return payment;
    }

    private Member checkMember(final Long memberId) {
        Member member = memberRepository.findByIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));
        return member;
    }
}