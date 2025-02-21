package com.fourseason.delivery.domain.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourseason.delivery.domain.payment.dto.request.CreatePaymentRequestDto;
import com.fourseason.delivery.domain.payment.dto.response.PaymentResponseDto;
import com.fourseason.delivery.domain.payment.exception.CustomRestClientException;
import com.fourseason.delivery.domain.payment.exception.PaymentErrorCode;
import com.fourseason.delivery.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentRestService {

    String widgetSecretKey = "test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6";
    Base64.Encoder encoder = Base64.getEncoder();
    byte[] encodedBytes = encoder.encode((widgetSecretKey + ":").getBytes(StandardCharsets.UTF_8));
    String authorizations = "Basic " + new String(encodedBytes);

//    public ResponseEntity<PaymentResponseDto> confirmPayment(CreatePaymentRequestDto createPaymentRequestDto) {
//        RestClient restClient = RestClient.create();
//
//        ResponseEntity<PaymentResponseDto> result = restClient.post()
//                .uri("https://api.tosspayments.com/v1/payments/confirm")
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", authorizations)
//                .body(createPaymentRequestDto)
//                .retrieve()
//                .toEntity(PaymentResponseDto.class);
//
//        return result;
//    }
    public ResponseEntity<String> confirmPayment(CreatePaymentRequestDto createPaymentRequestDto) {
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

        } catch (HttpServerErrorException e) {
            String errorMessage = e.getResponseBodyAsString();
            response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorMessage);

        }

        return response;
    }
//
//    // JSON 문자열을 PaymentResponseDto로 변환하는 메서드
//    private PaymentResponseDto convertToPaymentResponseDto(String responseBody) {
//        try {
//            ObjectMapper objectMapper = new ObjectMapper();
//            return objectMapper.readValue(responseBody, PaymentResponseDto.class);
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new RuntimeException("Failed to convert response to PaymentResponseDto");
//        }
//    }
}