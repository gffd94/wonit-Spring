package com.example.wonit.controller;

import com.example.wonit.dto.AccountResponse;
import com.example.wonit.entity.Account;
import com.example.wonit.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "swagger 테스트 API", description = "swagger 테스트를 진행하는 API")
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

    // 특정 계좌번호의 정보 조회
    // GET /api/accounts/1002-345-112233
    // 🚨 이름을 꼭 적습니다: @PathVariable("accountNo"). 주소의 {accountNo} 자리 값이 이 매개변수로 들어옵니다
    @GetMapping("/{accountNo}")
    public AccountResponse findOne(@PathVariable("accountNo") String accountNo) {
        return AccountResponse.from(accountService.getAccount(accountNo));
    }

//    // POST /api/accounts/1002-345-112233/withdraw?amount=100000
//    // 없는 계좌 · 지급정지 · 잔액 부족이면 예외가 터집니다. common/GlobalExceptionHandler 가 받아 404 · 409 로 바꿔 줍니다 (앞 단계에서는 전부 500 입니다)
//    @PostMapping("/{accountNo}/withdraw")
//    public WithdrawResponse withdraw(@PathVariable("accountNo") String accountNo,
//                                     @RequestParam("amount") long amount) {
//        long remainingBalance = accountService.withdraw(accountNo, amount);
//        return new WithdrawResponse(accountNo, amount, remainingBalance);
//    }

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
}
