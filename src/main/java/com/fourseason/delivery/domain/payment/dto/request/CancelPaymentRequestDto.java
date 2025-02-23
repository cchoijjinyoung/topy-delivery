package com.fourseason.delivery.domain.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CancelPaymentRequestDto (
        @NotBlank(message = "환불사유는 필수 입력 값입니다.")
        String cancelReason,

        @Positive(message = "취소금액은 양수만 가능합니다.")
        Integer cancelAmount
){
}
