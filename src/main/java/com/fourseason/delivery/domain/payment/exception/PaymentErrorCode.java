package com.fourseason.delivery.domain.payment.exception;

import com.fourseason.delivery.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum PaymentErrorCode implements ErrorCode {
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 결제를 찾을 수 없습니다."),
    PAYMENT_FORBIDDEN(HttpStatus.FORBIDDEN, "해당 회원에게 권한이 없습니다."),
    PAYMENT_CANCEL_TIMEOUT(HttpStatus.FORBIDDEN, "결제 취소 시간을 초과하였습니다."),
    ORDER_BY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 정렬을 찾을 수 없습니다."),
    // 클라이언트 잘못도 서버잘못도 아니지만 서버에서 수정해야하는 부분이므로 server error로 처리
    PAYMENT_MAPPING_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "결제 정보를 가져오는데 실패했습니다."),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "결제금액이 일치하지 않습니다."),
    NO_KEYWORD(HttpStatus.BAD_REQUEST, "검색어를 입력해주세요."),
    PAYMENT_COMPENSATE(HttpStatus.INTERNAL_SERVER_ERROR, "서버문제로 결제가 취소되었습니다.");

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
