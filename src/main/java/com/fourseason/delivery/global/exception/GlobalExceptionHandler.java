package com.fourseason.delivery.global.exception;

import com.fourseason.delivery.domain.payment.exception.CustomRestClientException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle CustomException
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponseEntity> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();

        return ErrorResponseEntity.toResponseEntity(errorCode);
    }

    /**
     * Handle MethodArgumentNotValidException (requestBody validation)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseEntity> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseEntity.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(e.getBindingResult().getAllErrors().stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.joining("\n"))
                )
                .build());
    }

    /**
     * Handle AccessDeniedException
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseEntity> handleAccessDeniedException(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponseEntity.builder()
                .status(HttpStatus.FORBIDDEN.value())
                .message("권한이 없습니다.")
                .build());
    }

    /**
     * Handle CustomRestClientException
     */
    @ExceptionHandler(CustomRestClientException.class)
    public ResponseEntity<ErrorResponseEntity> handleCustomRestClientException(CustomRestClientException e) {
        return ResponseEntity.status(e.getStatusCode())
                .body(ErrorResponseEntity.builder()
                        .status(e.getStatusCode().value())
                        .message(e.getMessage())
                        .build());
    }

    /**
     * Handle ConstraintViolationException (parameter validation)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseEntity> handleConstraintViolationException(ConstraintViolationException e) {
        String errorMessage = e.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseEntity.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message(errorMessage)
                        .build());
    }

    /**
     * Handle ConstraintViolationException (parameter validation)
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponseEntity> handleConstraintViolationException(MissingServletRequestParameterException e) {
        String errorMessage = e.getParameterName() + "를(을) 입력해 주세요.";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseEntity.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message(errorMessage)
                        .build());
    }


}
