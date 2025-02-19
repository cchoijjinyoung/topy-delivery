package com.fourseason.delivery.domain.payment.controller;


import com.fourseason.delivery.domain.payment.dto.request.CreatePaymentRequestDto;
import com.fourseason.delivery.domain.payment.service.PaymentRestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class WidgetController {

    private final PaymentRestService paymentRestService;


    @RequestMapping(value = "/confirm")
    public ResponseEntity<String> confirmPayment(
            @RequestBody @Valid final CreatePaymentRequestDto createPaymentRequestDto
    ) {
        return paymentRestService.confirmPayment(createPaymentRequestDto);
    }
}
