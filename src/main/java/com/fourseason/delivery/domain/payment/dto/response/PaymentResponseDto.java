package com.fourseason.delivery.domain.payment.dto.response;

import com.fourseason.delivery.domain.payment.entity.Payment;
import lombok.Builder;

import java.util.UUID;

@Builder
public record PaymentResponseDto(

        UUID paymentId,

        String paymentKey,

        int amount,

        String paymentMethod,

        String paymentStatus,

        String cancelReason,

        int balanceAmount
) {

    public static PaymentResponseDto of(Payment payment) {
        return PaymentResponseDto.builder()
                .paymentId(payment.getId())
                .paymentKey(payment.getPaymentKey())
                .amount(payment.getPaymentAmount())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .cancelReason(payment.getCancelReason())
                .balanceAmount(payment.getBalanceAmount())
                .build();
    }
}
