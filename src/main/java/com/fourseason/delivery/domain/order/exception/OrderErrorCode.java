package com.fourseason.delivery.domain.order.exception;

import com.fourseason.delivery.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {

  ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 주문을 찾을 수 없습니다."),
  NOT_SHOP_OWNER(HttpStatus.BAD_REQUEST, "가게 주인이 아닙니다."),
  NOT_PENDING_ORDER(HttpStatus.BAD_REQUEST, "보류 중인 주문이 아닙니다."),
  NOT_ORDERED_BY_CUSTOMER(HttpStatus.BAD_REQUEST, "해당 주문을 요청한 고객이 아닙니다."),
  ORDER_BY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 정렬을 찾을 수 없습니다."),

  // TODO: 각 도메인 ErrorCode 로 옮기기,
  MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 회원을 찾을 수 없습니다."),
  MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 메뉴를 찾을 수 없습니다.");


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
