package com.fourseason.delivery.domain.payment.dto.responseDto;

import com.fourseason.delivery.domain.payment.entity.Payment;
import lombok.Builder;

@Builder
public record PaymentResponseDto(

        String paymentKey,

        int paymentAmount,

        String paymentMethod,

        String paymentStatus
) {

    public static PaymentResponseDto of(Payment payment) {
        return PaymentResponseDto.builder()
                .paymentKey(payment.getPaymentKey())
                .paymentAmount(payment.getPaymentAmount())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .build();
    }
}
