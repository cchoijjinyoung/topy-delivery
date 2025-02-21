package com.fourseason.delivery.domain.payment.exception;

import com.fourseason.delivery.global.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public class CustomRestClientException extends RuntimeException {

    private final HttpStatusCode statusCode;  // 응답 상태 코드 저장
    private final String message;  // 응답 본문을 저장


    public CustomRestClientException(HttpStatusCode statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }
}