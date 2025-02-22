package com.fourseason.delivery.domain.payment.dto.external;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ExternalCancelPaymentDto (
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        UUID orderId,

        String paymentKey,

        int amount,

        String method,

        String status,

        Cancel cancel
){
    public record Cancel (
            String cancelReason,
            int cancelAmount
    ){
    }
}
