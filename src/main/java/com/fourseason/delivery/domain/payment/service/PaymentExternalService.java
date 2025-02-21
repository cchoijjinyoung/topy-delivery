package com.fourseason.delivery.domain.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.order.entity.Order;
import com.fourseason.delivery.domain.order.exception.OrderErrorCode;
import com.fourseason.delivery.domain.order.repository.OrderRepository;
import com.fourseason.delivery.domain.payment.dto.external.ExternalPaymentDto;
import com.fourseason.delivery.domain.payment.dto.request.CreatePaymentRequestDto;
import com.fourseason.delivery.domain.payment.exception.PaymentErrorCode;
import com.fourseason.delivery.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    private final PaymentService paymentService;

    String widgetSecretKey = "test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6";
    Base64.Encoder encoder = Base64.getEncoder();
    byte[] encodedBytes = encoder.encode((widgetSecretKey + ":").getBytes(StandardCharsets.UTF_8));
    String authorizations = "Basic " + new String(encodedBytes);

    /**
     * payment 요청을 위한 order값 전달
     */
    public URI checkoutPayment(UUID orderId) {
        // 원래는 orderId로 order 조회후 값을 넣는다
        URI location = URI.create("/static/checkout?orderId="+ orderId + "&amount=" + 20000);
        return location;
    }

    /**
     * tossPayment를 통해 결제 승인, payment객체를 받아옴
     * TODO: 결제 진행후 이어서 db에 결제정보를 저장하도록 함
     */
    public ResponseEntity<String> confirmPayment(CreatePaymentRequestDto createPaymentRequestDto, Member member) {
        // 결제 검증
//        requestCheck(createPaymentRequestDto, member);
        // 승인 요청
        RestClient restClient = RestClient.create();
        ResponseEntity<String> response = ResponseEntity.ok().build();
        try {
            String result = restClient.post()
                    .uri("https://api.tosspayments.com/v1/payments/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", authorizations)
                    .body(createPaymentRequestDto)
                    .retrieve()
                    .body(String.class);

            response = ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result);

            System.out.println(result);
            // 여기서 결제 db저장 이 처리를 비동기로 처리하는것이 best 실패시 재시도 보정도 좋음
            ObjectMapper objectMapper = new ObjectMapper();
            ExternalPaymentDto externalPaymentDto = objectMapper.readValue(result, ExternalPaymentDto.class);

            System.out.println(externalPaymentDto.paymentKey());
            System.out.println(externalPaymentDto.amount());
            System.out.println(externalPaymentDto.method());
            System.out.println(externalPaymentDto.status());
            System.out.println(externalPaymentDto.orderId());
//            paymentService.registerPayment(externalPaymentDto, member);

        // 외부 api 호출 과정에서 internal error 발생시
        } catch (HttpServerErrorException e) {
            String errorMessage = e.getResponseBodyAsString();
            response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorMessage);

        // 결제 정보를 가져오는 과정에서 형식이 변하거나 했을 때
        } catch (JsonProcessingException e) {
            System.out.println(e.getMessage());
            throw new CustomException(PaymentErrorCode.PAYMENT_MAPPING_FAIL);
        }

        return response;
    }

    /**
     * 결제 시 검증
     */
    private void requestCheck(CreatePaymentRequestDto createPaymentRequestDto, Member member){
        Order order = orderRepository.findById(createPaymentRequestDto.orderId())
                .orElseThrow(() ->
                        new CustomException(OrderErrorCode.ORDER_NOT_FOUND)
                );
        if (!order.getMember().getId().equals(member.getId())) {
            throw new CustomException(OrderErrorCode.NOT_ORDERED_BY_CUSTOMER);
        }
        if (order.getTotalPrice() != createPaymentRequestDto.amount()) {
            throw new CustomException(PaymentErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
    }
}