package com.fourseason.delivery.domain.order.exception;

import static org.springframework.http.HttpStatus.*;

import com.fourseason.delivery.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {

  NOT_PENDING_ORDER(BAD_REQUEST, "보류 중인 주문이 아닙니다."),
  ALREADY_CANCELED_ORDER(BAD_REQUEST, "이미 취소된 주문입니다."),
  ORDER_CANCEL_EXPIRED(BAD_REQUEST, "주문 취소 기간이 만료되었습니다."),
  NOT_EXIST_MEMBER_ROLE(BAD_REQUEST, "존재하지 않는 Role 타입입니다."),
  NOT_PAID_ORDER(BAD_REQUEST, "결제되지 않은 주문입니다."),

  NOT_OWNER_OR_CUSTOMER(FORBIDDEN, "가게 주인이거나 주문 고객이어야 합니다."),
  NOT_ORDERED_BY_CUSTOMER(FORBIDDEN, "해당 주문을 요청한 고객이 아닙니다."),
  NOT_SHOP_OWNER(FORBIDDEN, "가게 주인이 아닙니다."),
  NOT_CREATE_ORDER_PERMISSION(FORBIDDEN, "주문 생성 권한이 없습니다."),
  NOT_READ_ORDER_PERMISSION(FORBIDDEN, "주문 조회 권한이 없습니다."),

  ORDER_NOT_FOUND(NOT_FOUND, "해당 주문을 찾을 수 없습니다."),
  ORDER_BY_NOT_FOUND(NOT_FOUND, "해당 정렬을 찾을 수 없습니다."),

  // TODO: 각 도메인 ErrorCode 로 옮기기,
  MENU_NOT_FOUND(NOT_FOUND, "해당 메뉴를 찾을 수 없습니다.");


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
