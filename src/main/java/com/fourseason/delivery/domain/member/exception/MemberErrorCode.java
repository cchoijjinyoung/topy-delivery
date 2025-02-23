package com.fourseason.delivery.domain.member.exception;

import com.fourseason.delivery.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements ErrorCode {

    MEMBER_DUPLICATE_USERNAME(HttpStatus.CONFLICT, "해당 username 은 사용할 수 없습니다."),
    MEMBER_DUPLICATE_EMAIL(HttpStatus.CONFLICT, "해당 email 은 사용할 수 없습니다."),
    // 인증 정보가 잘못됐을 경우 메시지를 따로 안 보내는 것이....
    MEMBER_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "ID / 비밀번호가 잘못되었습니다."),
    MEMBER_INVALID_CREDENTIAL(HttpStatus.UNAUTHORIZED, "ID / 비밀번호가 잘못되었습니다.")
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
