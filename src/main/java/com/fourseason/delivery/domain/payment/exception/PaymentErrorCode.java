package com.fourseason.delivery.domain.payment.exception;

import com.fourseason.delivery.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode implements ErrorCode {
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 결제를 찾을 수 없습니다."),
    PAYMENT_NOT_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "해당 회원에게 권한이 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public HttpStatus httpStatus() {
        return httpStatus;
    }

    @Override
    public String message() {
        return message;
    }
}
