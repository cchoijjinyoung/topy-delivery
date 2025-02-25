package com.fourseason.delivery.domain.review.exception;

import com.fourseason.delivery.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReviewErrorCode implements ErrorCode {

    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 리뷰를 찾을 수 없습니다."),
    SHOP_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 가게를 찾을 수 없습니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 주문을 찾을 수 없습니다."),
    ORDER_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "배달이 완료되지 않았습니다."),
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "해당 주문의 리뷰가 존재합니다." ),
    REVIEW_IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 리뷰 이미지를 찾을 수 없습니다."),
    REVIEW_PERIOD_EXPIRED(HttpStatus.BAD_REQUEST, "이 주문에 대한 리뷰 기간이 만료되었습니다." );




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
