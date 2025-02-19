package com.fourseason.delivery.global.auth.exception;

import com.fourseason.delivery.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    ACCESS_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "유효한 토큰을 찾을 수 없습니다."),

    // TODO: message 내용 다르게 고민..
    ACCESS_TOKEN_NOT_AVAILABLE(HttpStatus.UNAUTHORIZED, "토큰 검증에 문제....")
    ;


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
