package com.fourseason.delivery.domain.member.exception;

import com.fourseason.delivery.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements ErrorCode {

    // TODO: error message 고민...
    MEMBER_DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 가입된 이메일 주소입니다."),
    // 인증 정보가 잘못됐을 경우 메시지를 따로 안 보내는 것이....
    MEMBER_NOT_FOUND(HttpStatus.UNAUTHORIZED, "ID / 비밀번호가 잘못되었습니다."),
    MEMBER_INVALID_CREDENTIAL(HttpStatus.UNAUTHORIZED, "ID / 비밀번호가 잘못되었습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public HttpStatus httpStatus() {
        return null;
    }

    @Override
    public String message() {
        return "";
    }
}
