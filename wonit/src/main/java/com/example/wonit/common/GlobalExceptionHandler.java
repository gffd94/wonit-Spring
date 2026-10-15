package com.example.wonit.common;

import com.example.wonit.dto.ErrorResponse;
import com.example.wonit.exception.AccountFrozenException;
import com.example.wonit.exception.AccountNotFoundException;
import com.example.wonit.exception.DuplicateAccountException;
import com.example.wonit.exception.InsufficientBalanceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 잘못된 입력 → 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> badRequest(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("BAD_REQUEST", "잘못된 요청임"));
    }

    // 없는 계좌 → 404
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> notFound(AccountNotFoundException e) {
        log.warn("404 응답: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("NOT_FOUND", "계좌 없음"));
    }

    // 요청은 올바르지만 지금 상태와 부딪힘(지급정지 · 잔액부족 · 계좌번호 중복) → 409
    @ExceptionHandler({AccountFrozenException.class,
            InsufficientBalanceException.class,
            DuplicateAccountException.class})
    public ResponseEntity<ErrorResponse> conflict(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("CONFLICT", "뭔가 문제 발생"));
    }

    // 위에서 못 잡은 나머지 전부 → 500
    // 맨 마지막 그물. 🚨 e.getMessage() 를 그대로 내보내면 SQL·클래스명이 손님 화면에 찍힌다
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnknown(Exception e) {
        log.error("처리하지 못한 예외", e);   // ← e 를 {} 없이 마지막에 넘기면 스택트레이스 전체가 찍힌다
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_ERROR", "잠시 후 다시 시도해 주세요."));
    }
}
