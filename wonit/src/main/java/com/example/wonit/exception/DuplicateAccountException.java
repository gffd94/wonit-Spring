package com.example.wonit.exception;

// 계좌번호를 새로 발급하려는데 이미 같은 번호가 있을 때 던지는 예외. 이름만 봐도 실패 이유를 알 수 있게 업무 이름으로 만든다.
public class DuplicateAccountException extends RuntimeException {
    public DuplicateAccountException(String message) {
        super(message);
    }
}
