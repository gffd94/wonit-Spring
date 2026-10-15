package com.example.wonit.controller;

import com.example.wonit.dto.AccountCreateRequest;
import com.example.wonit.dto.AccountResponse;
import com.example.wonit.dto.WithdrawRequest;
import com.example.wonit.dto.WithdrawResponse;
import com.example.wonit.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Tag(name = "계좌 API", description = "계좌 조회 · 개설 API")
@Slf4j
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    // 어차피 스프린 컨테이너가 관리하는 bean이므로 private로 막아둠.
    // @Autowired : 서로서로 연결된 빈이라는 걸 알려주지만, 생략하면 spring boot가 달아줌.
    private final AccountService accountService;

    // 모든 accounts를 조회
    @Operation(summary = "정보 모두 조회", description = "Account의 전체 정보를 조회합니다.")
    @GetMapping
    public List<AccountResponse> findAll() {
        return accountService.findAll().stream()
                .map(AccountResponse::from)
                .toList();
    }

    // 계좌개설
    // POST /api/accounts   body: {"customerId": 1, "accountType": "입출금"}
    // 만들기(생성)는 "모음 주소(/api/accounts)에 POST" 가 REST 규칙. 주소에 open · create 같은 동사를 넣지 않습니다
    // 성공하면 200 이 아니라 201 Created, Location 헤더에 새 계좌를 조회할 주소(/api/accounts/{accountNo})를 담아 줍니다
    @Operation(summary = "계좌개설", description = "새 계좌를 엽니다. 계좌번호는 서버가 발급하고 상태는 '정상', 잔액은 0 으로 시작합니다.")
    @PostMapping
    public ResponseEntity<AccountResponse> openAccount(@RequestBody AccountCreateRequest request) {
        AccountResponse created = accountService.openAccount(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{accountNo}")
                .buildAndExpand(created.accountNo())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    // 계좌 전체를 조회해서 그중에 '정상' 계좌만 계좌번호만 출력

    @GetMapping("/report1")
    public List<String> normalAccountNos() {
        return accountService.normalAccountNos();
    }

    // 전체 계좌중에 account_type이 '적금'인 경우만 금액을 모두 더해서 리턴
    // /api/accounts/report2 라는 주소로
    @GetMapping("/report2")
    public long savingBalanceSum() {
        return accountService.savingBalanceSum();
    }

        // GET /api/accounts/report
    // /{accountNo} 보다 위에 둡니다. "report" 도 계좌번호 자리에 들어갈 수 있는 글자라서, 읽는 사람이 헷갈리지 않게 먼저 적어줍니다
    // (Spring 은 글자가 정확히 맞는 주소를 먼저 고르므로 순서가 바뀌어도 동작은 같습니다)
//    @GetMapping("/report")
//    public AccountReport report() {
//        return new AccountReport(
//                accountService.nonNormalAccountNos(),
//                accountService.savingsBalanceSum());
//    }

    /**
     * 계좌번호로 계좌 하나를 찾는다.
     * <p>테스트: localhost:8080/api/accounts/1002-345-112233</p>
     *
     * @param accountNo 찾을 계좌번호 (예: 1002-345-112233)
     * @return 찾은 계좌
     * @throws com.example.wonit.exception.AccountNotFoundException 해당 계좌번호의 계좌가 없을 때
     */
    @GetMapping("/{accountNo}")
    public AccountResponse findOne(@PathVariable("accountNo") String accountNo) {
        log.info("[계좌조회] 요청 accountNo={}", accountNo);
        AccountResponse response = AccountResponse.from(accountService.getAccount(accountNo));
        log.info("[계좌조회] 응답 accountNo={}, balance={}", response.accountNo(), response.balance());
        return response;
    }

    // 출금
    // POST /api/accounts/1002-345-112233/withdrawals   body: {"amount": 100000}
    // REST 규칙: 주소에 동사(withdraw)를 넣지 않고, "이 계좌의 출금 내역(withdrawals)에 1건을 새로 만든다"로 표현합니다
    //           금액은 주소(?amount=)가 아니라 body 로 받습니다. 주소는 서버 로그 · 브라우저 기록에 그대로 남기 때문입니다
    // 새 거래 1건이 생기므로 201 Created. (거래 1건을 조회하는 주소가 아직 없어서 Location 헤더는 붙이지 않습니다)
    // 없는 계좌 · 지급정지 · 잔액 부족이면 예외가 터집니다. common/GlobalExceptionHandler 가 받아 404 · 409 로 바꿔 줍니다
    @Operation(summary = "출금", description = "계좌에서 출금하고 거래내역을 남깁니다. 없는 계좌 404, 지급정지 · 잔액부족 409, 0 이하 금액 400.")
    @PostMapping("/{accountNo}/withdrawals")
    public ResponseEntity<WithdrawResponse> withdraw(@PathVariable("accountNo") String accountNo,
                                                     @RequestBody WithdrawRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.withdraw(accountNo, request));
    }

    // min~ max balance 사이의 계좌 정보를 검색
    // http://localhost:8080/api/accounts/balance?minBalance=500000&maxBalance=1000000
    @GetMapping("/balance")
    public List<AccountResponse> getAccountBetweenBalance(@RequestParam long minBalance, @RequestParam long maxBalance) {
        return accountService.getAccountBetweenBalance(minBalance, maxBalance);
    }


    // balance 순서대로 작은 금액에서 큰 금액 순서로 모든 계좌 조회
    // http://localhost:8080/api/accounts/sorted
    @GetMapping("/sorted")
    public List<AccountResponse> getAccountsOrderByBalance() {
        return accountService.getAccountsOrderByBalance();
    }

    // status 에서 정상 / 휴면 / 지급정지 들어오면 각 상황에 맞는 계좌만 필터링 -> 예외나 엣지케이스 테스트케이스 같이 만들고 처리
    // http://localhost:8080/api/accounts/status?status=휴면
    @GetMapping("/status")
    public List<AccountResponse> getAccountsByStatus(@RequestParam String status) {
        return accountService.getAccountsByStatus(status);
    }

    // account_type에 적금 / 입출금 들어오면 각 상황에 맞는 계좌만 필터링
    // http://localhost:8080/api/accounts/type?accountType=적금
    @GetMapping("/type")
    public List<AccountResponse> getAccountsByAccountType(@RequestParam String accountType) {
        return accountService.getAccountsByAccountType(accountType);
    }

    // 계좌 삭제. 성공하면 본문 없이 204, 없는 계좌면 404
    @DeleteMapping("/{accountNo}")
    public ResponseEntity<Void> delete(@PathVariable("accountNo") String accountNo) {
        accountService.delete(accountNo);
        return ResponseEntity.noContent().build();
    }
}
