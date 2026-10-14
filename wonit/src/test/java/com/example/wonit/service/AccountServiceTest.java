package com.example.wonit.service;

import com.example.wonit.dto.AccountCreateRequest;
import com.example.wonit.dto.AccountResponse;
import com.example.wonit.dto.WithdrawRequest;
import com.example.wonit.dto.WithdrawResponse;
import com.example.wonit.exception.AccountFrozenException;
import com.example.wonit.exception.AccountNotFoundException;
import com.example.wonit.exception.InsufficientBalanceException;
import com.example.wonit.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// data.sql 의 계좌 3건으로 검사합니다
//  1002-345-678901  입출금  1,523,000  지급정지
//  1002-345-112233  적금    1,099,500  정상
//  1002-345-998877  적금      497,000  휴면
@SpringBootTest
class AccountServiceTest {

    @Autowired
    AccountService accountService;

    @Autowired
    TransactionRepository transactionRepository;

    private List<String> accountNos(List<AccountResponse> responses) {
        return responses.stream().map(AccountResponse::accountNo).toList();
    }

    // ── 잔액 범위 ──────────────────────────────
    @Test
    void 잔액_범위_조회() {
        assertEquals(List.of("1002-345-112233"),
                accountNos(accountService.getAccountBetweenBalance(500000, 1100000)));
    }

    @Test
    void 잔액_범위에_계좌가_없으면_빈_리스트() {
        assertTrue(accountService.getAccountBetweenBalance(500000, 1000000).isEmpty());
    }

    @Test
    void 잔액_범위는_경계값을_포함한다() {
        assertEquals(List.of("1002-345-998877"),
                accountNos(accountService.getAccountBetweenBalance(497000, 497000)));
    }

    @Test
    void min_이_max_보다_크면_예외() {
        assertThrows(IllegalArgumentException.class,
                () -> accountService.getAccountBetweenBalance(1000000, 500000));
    }

    @Test
    void 음수_잔액이면_예외() {
        assertThrows(IllegalArgumentException.class,
                () -> accountService.getAccountBetweenBalance(-1, 500000));
    }

    // ── 잔액 오름차순 ──────────────────────────
    @Test
    void 잔액_오름차순_조회() {
        List<Long> balances = accountService.getAccountsOrderByBalance().stream()
                .map(AccountResponse::balance)
                .toList();
        assertEquals(List.of(497000L, 1099500L, 1523000L), balances);
    }

    // ── status ────────────────────────────────
    @Test
    void status_정상() {
        assertEquals(List.of("1002-345-112233"), accountNos(accountService.getAccountsByStatus("정상")));
    }

    @Test
    void status_휴면() {
        assertEquals(List.of("1002-345-998877"), accountNos(accountService.getAccountsByStatus("휴면")));
    }

    @Test
    void status_지급정지() {
        assertEquals(List.of("1002-345-678901"), accountNos(accountService.getAccountsByStatus("지급정지")));
    }

    @Test
    void status_앞뒤_공백은_잘라서_조회() {
        assertEquals(List.of("1002-345-112233"), accountNos(accountService.getAccountsByStatus(" 정상 ")));
    }

    @Test
    void status_허용되지_않은_값이면_예외() {
        assertThrows(IllegalArgumentException.class, () -> accountService.getAccountsByStatus("해지"));
    }

    @Test
    void status_비어있으면_예외() {
        assertThrows(IllegalArgumentException.class, () -> accountService.getAccountsByStatus(""));
        assertThrows(IllegalArgumentException.class, () -> accountService.getAccountsByStatus("   "));
        assertThrows(IllegalArgumentException.class, () -> accountService.getAccountsByStatus(null));
    }

    // ── account_type ──────────────────────────
    @Test
    void accountType_적금() {
        List<String> result = accountNos(accountService.getAccountsByAccountType("적금"));
        assertEquals(2, result.size());
        assertTrue(result.containsAll(List.of("1002-345-112233", "1002-345-998877")));
    }

    @Test
    void accountType_입출금() {
        assertEquals(List.of("1002-345-678901"), accountNos(accountService.getAccountsByAccountType("입출금")));
    }

    @Test
    void accountType_허용되지_않은_값이면_예외() {
        assertThrows(IllegalArgumentException.class, () -> accountService.getAccountsByAccountType("예금"));
    }

    @Test
    void accountType_비어있으면_예외() {
        assertThrows(IllegalArgumentException.class, () -> accountService.getAccountsByAccountType(""));
        assertThrows(IllegalArgumentException.class, () -> accountService.getAccountsByAccountType(null));
    }

    // ── 계좌개설 ──────────────────────────────
    // @Transactional: 테스트가 끝나면 rollback 해서, 새로 만든 계좌가 위의 status · type 테스트 결과를 바꾸지 않게 합니다
    @Test
    @Transactional
    void 계좌개설_정상() {
        AccountResponse created = accountService.openAccount(new AccountCreateRequest(1, "입출금"));

        assertTrue(created.accountNo().matches("1002-345-\\d{6}"));
        assertEquals("입출금", created.accountType());
        assertEquals(0L, created.balance());
        assertEquals("정상", created.status());
        // 저장까지 됐는지: 발급된 번호로 다시 조회된다
        assertEquals(created.accountNo(), accountService.getAccount(created.accountNo()).getAccountNo());
    }

    @Test
    @Transactional
    void 계좌개설_accountType_앞뒤_공백은_잘라서_저장() {
        assertEquals("적금", accountService.openAccount(new AccountCreateRequest(1, " 적금 ")).accountType());
    }

    @Test
    @Transactional
    void 계좌개설_두번_하면_서로_다른_계좌번호() {
        String first = accountService.openAccount(new AccountCreateRequest(1, "입출금")).accountNo();
        String second = accountService.openAccount(new AccountCreateRequest(1, "입출금")).accountNo();
        assertNotEquals(first, second);
    }

    @Test
    void 계좌개설_accountType_이_잘못되면_예외() {
        assertThrows(IllegalArgumentException.class,
                () -> accountService.openAccount(new AccountCreateRequest(1, "예금")));
        assertThrows(IllegalArgumentException.class,
                () -> accountService.openAccount(new AccountCreateRequest(1, null)));
    }

    @Test
    void 계좌개설_customerId_가_없거나_0_이하면_예외() {
        assertThrows(IllegalArgumentException.class,
                () -> accountService.openAccount(new AccountCreateRequest(null, "입출금")));
        assertThrows(IllegalArgumentException.class,
                () -> accountService.openAccount(new AccountCreateRequest(0, "입출금")));
    }

    @Test
    void 없는_계좌번호_조회는_예외() {
        assertThrows(AccountNotFoundException.class, () -> accountService.getAccount("0000-000-000000"));
    }

    // ── 출금 ──────────────────────────────────
    // @Transactional: 끝나면 rollback 해서 잔액 · 거래내역이 원래대로 돌아갑니다 (위의 잔액 테스트가 깨지지 않게)
    @Test
    @Transactional
    void 출금_정상() {
        long txCountBefore = transactionRepository.count();

        WithdrawResponse result = accountService.withdraw("1002-345-112233", new WithdrawRequest(99500L));

        assertEquals(99500L, result.amount());
        assertEquals(1000000L, result.balance());
        assertEquals(1000000L, accountService.getAccount("1002-345-112233").getBalance());
        assertEquals(txCountBefore + 1, transactionRepository.count());   // 거래내역 1건 INSERT
    }

    @Test
    @Transactional
    void 출금_잔액_전부_빼기는_가능() {
        assertEquals(0L, accountService.withdraw("1002-345-112233", new WithdrawRequest(1099500L)).balance());
    }

    @Test
    @Transactional
    void 출금_잔액보다_많으면_예외_잔액과_거래내역은_그대로() {
        long txCountBefore = transactionRepository.count();

        assertThrows(InsufficientBalanceException.class,
                () -> accountService.withdraw("1002-345-112233", new WithdrawRequest(1099501L)));

        assertEquals(1099500L, accountService.getAccount("1002-345-112233").getBalance());
        assertEquals(txCountBefore, transactionRepository.count());
    }

    @Test
    @Transactional
    void 출금_지급정지_계좌면_예외() {
        assertThrows(AccountFrozenException.class,
                () -> accountService.withdraw("1002-345-678901", new WithdrawRequest(1000L)));
    }

    @Test
    @Transactional
    void 출금_없는_계좌면_예외() {
        assertThrows(AccountNotFoundException.class,
                () -> accountService.withdraw("0000-000-000000", new WithdrawRequest(1000L)));
    }

    @Test
    @Transactional
    void 출금액이_0_이하거나_비어있으면_예외() {
        assertThrows(IllegalArgumentException.class,
                () -> accountService.withdraw("1002-345-112233", new WithdrawRequest(0L)));
        assertThrows(IllegalArgumentException.class,
                () -> accountService.withdraw("1002-345-112233", new WithdrawRequest(-100000L)));
        assertThrows(IllegalArgumentException.class,
                () -> accountService.withdraw("1002-345-112233", new WithdrawRequest(null)));
        assertEquals(1099500L, accountService.getAccount("1002-345-112233").getBalance());
    }
}
