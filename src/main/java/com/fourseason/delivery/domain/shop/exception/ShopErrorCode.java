package com.fourseason.delivery.domain.shop.exception;

import static org.springframework.http.HttpStatus.FORBIDDEN;

import com.fourseason.delivery.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ShopErrorCode implements ErrorCode {

    SHOP_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 가게를 찾을 수 없습니다."),
    SHOP_IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 가게 이미지를 찾을 수 없습니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 카테고리를 찾을 수 없습니다."),
    ORDER_BY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 정렬을 찾을 수 없습니다."),
    NOT_SHOP_OWNER(FORBIDDEN, "가게 주인이 아닙니다."),

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
