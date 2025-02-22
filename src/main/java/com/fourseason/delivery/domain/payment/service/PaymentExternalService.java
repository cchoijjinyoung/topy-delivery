package com.fourseason.delivery.domain.payment.service;

import com.fourseason.delivery.domain.member.MemberErrorCode;
import com.fourseason.delivery.domain.order.entity.Order;
import com.fourseason.delivery.domain.order.exception.OrderErrorCode;
import com.fourseason.delivery.domain.order.repository.OrderRepository;
import com.fourseason.delivery.domain.payment.dto.request.CancelPaymentRequestDto;
import com.fourseason.delivery.domain.payment.dto.request.CreatePaymentRequestDto;
import com.fourseason.delivery.domain.payment.entity.Payment;
import com.fourseason.delivery.domain.payment.exception.CustomRestClientException;
import com.fourseason.delivery.domain.payment.exception.PaymentErrorCode;
import com.fourseason.delivery.domain.payment.repository.PaymentRepository;
import com.fourseason.delivery.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentExternalService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    String widgetSecretKey = "test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6";
    Base64.Encoder encoder = Base64.getEncoder();
    byte[] encodedBytes = encoder.encode((widgetSecretKey + ":").getBytes(StandardCharsets.UTF_8));
    String authorizations = "Basic " + new String(encodedBytes);

//    /**
//     * payment 요청을 위한 order값 전달
//     */
//    public URI checkoutPayment(UUID orderId, String username) {
//        Order order = orderRepository.findById(orderId).orElseThrow(
//                () -> new CustomException(OrderErrorCode.ORDER_NOT_FOUND));
//        // 원래는 orderId로 order 조회후 값을 넣는다
//        return URI.create("/static/checkout?orderId="+ orderId + "&amount=" + 20000);
//    }

    /**
     * tossPayment를 통해 결제 승인, payment객체를 받아옴
     * TODO: 결제 진행후 이어서 db에 결제정보를 저장하도록 함
     * 고민사항: 결제를 남이 해주는 경우가 있을까? 회원탈퇴한 사용자가 본인의 남아있던 주문을 결제하는경우가 있나?
     * 회원탈퇴한 사람이 주문을 결제하는 경우가 아니라면 주문 생성시에도 username을 확인할 필요가 없어지고 confirmPayment와 registerPayment를 분리 할 수 있다.
     */
    public String confirmPayment(CreatePaymentRequestDto createPaymentRequestDto) {
        // 결제 검증
//        String username = requestCheck(createPaymentRequestDto);
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
        } catch (HttpServerErrorException e) {
            throw new CustomRestClientException(e.getStatusCode(), e.getResponseBodyAsString());
        }
    }

    /**
     * 결제 취소
     */
    public String cancelPayment(UUID paymentId, CancelPaymentRequestDto cancelPaymentRequestDto, String paymentKey) {
//        Payment payment = paymentRepository.findByIdAndDeletedAtIsNotNull(paymentId)
//                .orElseThrow(
//                        () -> new CustomException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        RestClient restClient = RestClient.create();
        try {
            return restClient.post()
//                    .uri("https://api.tosspayments.com//v1/payments/" + payment.getPaymentKey() + "/cancel")
                    .uri("https://api.tosspayments.com//v1/payments/" + paymentKey + "/cancel")
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
    private String requestCheck(CreatePaymentRequestDto createPaymentRequestDto){
        Order order = orderRepository.findById(createPaymentRequestDto.orderId())
                .orElseThrow(() ->
                        new CustomException(OrderErrorCode.ORDER_NOT_FOUND)
                );
        if (order.getMember().getDeletedAt() != null) {
            throw new CustomException(MemberErrorCode.MEMBER_NOT_FOUND);
        }
        if (order.getTotalPrice() != createPaymentRequestDto.amount()) {
            throw new CustomException(PaymentErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
        return order.getMember().getUsername();
    }
}