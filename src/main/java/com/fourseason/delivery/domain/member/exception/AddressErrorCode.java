package com.fourseason.delivery.domain.member.exception;

import com.fourseason.delivery.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AddressErrorCode implements ErrorCode {

    ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 주소를 찾을 수 없습니다"),
    ADDRESS_NOT_BELONG_TO_MEMBER(HttpStatus.FORBIDDEN, "해당 주소는 현재 회원에 속하지 않습니다." );

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
