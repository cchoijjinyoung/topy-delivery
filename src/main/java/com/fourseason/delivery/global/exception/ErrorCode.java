package com.fourseason.delivery.global.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
    HttpStatus httpStatus();

    String message();
}
