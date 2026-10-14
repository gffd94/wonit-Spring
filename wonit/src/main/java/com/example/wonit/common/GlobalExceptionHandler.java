package com.example.wonit.common;

import com.example.wonit.exception.AccountFrozenException;
import com.example.wonit.exception.AccountNotFoundException;
import com.example.wonit.exception.DuplicateAccountException;
import com.example.wonit.exception.InsufficientBalanceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

// 컨트롤러에서 터진 예외를 여기서 받아 알맞은 HTTP 상태코드로 바꿉니다
// 이 파일이 없으면 모든 예외가 500(서버 오류)으로 나갑니다
// ProblemDetail = 에러 응답의 표준 모양(RFC 9457). {"status":404, "detail":"...", ...} 형태의 JSON 으로 나갑니다
//
// extends ResponseEntityExceptionHandler: Spring 이 스스로 던지는 예외도 같은 ProblemDetail 모양으로 바꿔 줍니다
//   깨진 JSON · 필수 파라미터 누락(?minBalance 빠짐) · 타입 불일치(?minBalance=abc) → 400
//   없는 주소 → 404,  GET 만 되는 주소에 POST → 405
//   이게 없으면 Spring 기본 에러 응답이 나가는데, 개발 모드에서는 그 안에 서버 내부 코드(stack trace)까지 담겨 나갑니다
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // 잘못된 입력 → 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail badRequest(IllegalArgumentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    // 없는 계좌 → 404
    @ExceptionHandler(AccountNotFoundException.class)
    public ProblemDetail notFound(AccountNotFoundException e) {
        log.warn("404 응답: {}", e.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    // 요청은 올바르지만 지금 상태와 부딪힘(지급정지 · 잔액부족 · 계좌번호 중복) → 409
    @ExceptionHandler({AccountFrozenException.class,
            InsufficientBalanceException.class,
            DuplicateAccountException.class})
    public ProblemDetail conflict(RuntimeException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    // 위에서 못 잡은 나머지 전부 → 500
    // 🏦 원인(e.getMessage())은 밖으로 보내지 않습니다. SQL · 테이블 이름 같은 내부 정보가 섞여 있을 수 있기 때문입니다
    //    원인은 서버 로그에만 남기고, 고객에게는 정해진 문장만 보냅니다
    @ExceptionHandler(Exception.class)
    public ProblemDetail internalError(Exception e) {
        log.error("처리하지 못한 예외", e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
    }
}
