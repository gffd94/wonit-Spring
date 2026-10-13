package com.example.wonit.service;

import com.example.wonit.dto.AccountResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
}
