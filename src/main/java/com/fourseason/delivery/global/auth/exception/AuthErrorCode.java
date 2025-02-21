package com.fourseason.delivery.global.auth.exception;

import com.fourseason.delivery.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    ACCESS_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "토큰을 찾을 수 없습니다."),
    ACCESS_TOKEN_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "유효하지 않은 토큰입니다. ACCESS"),
    ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
    REFRESH_TOKEN_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "유효하지 않은 토큰입니다. REFRESH")
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
