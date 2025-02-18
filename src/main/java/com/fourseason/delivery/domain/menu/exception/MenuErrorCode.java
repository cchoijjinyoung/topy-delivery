package com.fourseason.delivery.domain.menu.exception;

import com.fourseason.delivery.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum MenuErrorCode implements ErrorCode {

    MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 메뉴를 찾을 수 없습니다."),
    ORDER_BY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 정렬을 찾을 수 없습니다."),
    NO_KEYWORD(HttpStatus.BAD_REQUEST, "검색어를 입력해주세요.");

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
