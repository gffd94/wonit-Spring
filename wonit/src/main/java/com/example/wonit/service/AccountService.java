package com.example.wonit.service;

import com.example.wonit.dto.AccountResponse;
import com.example.wonit.entity.Account;
import com.example.wonit.repository.AccountRepository;
import com.example.wonit.exception.AccountNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    public Optional<Account> findAccount(String accountNo) {
        return accountRepository.findByAccountNo(accountNo);
    }

    public Account getAccount(String accountNo) {
        return findAccount(accountNo)
                .orElseThrow(() -> new AccountNotFoundException(accountNo + " 계좌를 찾을 수 없습니다."));
    }

    // // 🏦 출금은 "잔액 UPDATE + 거래 INSERT" 가 한 묶음.  commit/rollback 을 손으로 쓰는 대신
    // //    @Transactional 한 줄이 같은 일을 합니다: 메서드가 정상으로 끝나면 commit, RuntimeException 이 나면 둘 다 rollback
    // @Transactional
    // public long withdraw(String accountNo, long amount) {
    //     Account account = getAccount(accountNo);
    //     account.withdraw(amount);
    //     transactionRepository.save(Transaction.withdrawal(account, amount));   // INSERT
    //     return account.getBalance();
    // }   // 여기서 commit. 중간에 RuntimeException(예외 3종)이 나면 둘 다 rollback

    public List<String> normalAccountNos() {
        return accountRepository.findAll().stream()
                .filter(account -> account.getStatus().equals("정상"))
                .map(Account::getAccountNo)
                .toList();
    }

    public long savingBalanceSum() {
        return accountRepository.findAll().stream()
                .filter(account -> account.getAccountType().equals("적금"))
                .mapToLong(Account::getBalance)
                .sum();
    }

    public List<AccountResponse> getAccountBetweenBalance(long minBalance, long maxBalance) {
        if (minBalance < 0 || maxBalance < 0) {
            throw new IllegalArgumentException("잔액은 0 이상이어야 합니다.");
        }
        if (minBalance > maxBalance) {
            throw new IllegalArgumentException("minBalance 가 maxBalance 보다 클 수 없습니다.");
        }
        return toResponses(accountRepository.findByBalanceBetween(minBalance, maxBalance));
    }

    public List<AccountResponse> getAccountsOrderByBalance() {
        return toResponses(accountRepository.findAllByOrderByBalanceAsc());
    }

    // 잘못된 값은 예외, 값은 맞지만 해당 계좌가 없으면 빈 리스트
    public List<AccountResponse> getAccountsByStatus(String status) {
        return toResponses(accountRepository.findByStatus(validate(status, STATUSES, "status")));
    }

    public List<AccountResponse> getAccountsByAccountType(String accountType) {
        return toResponses(accountRepository.findByAccountType(validate(accountType, ACCOUNT_TYPES, "accountType")));
    }

    private static final Set<String> STATUSES = Set.of("정상", "휴면", "지급정지");
    private static final Set<String> ACCOUNT_TYPES = Set.of("적금", "입출금");

    // null · 공백 · 허용 목록에 없는 값이면 예외. 앞뒤 공백은 잘라서 받아준다
    private String validate(String value, Set<String> allowed, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " 값이 비어 있습니다.");
        }
        String trimmed = value.trim();
        if (!allowed.contains(trimmed)) {
            throw new IllegalArgumentException(name + " 는 " + allowed + " 중 하나여야 합니다: " + value);
        }
        return trimmed;
    }

    private List<AccountResponse> toResponses(List<Account> accounts) {
        return accounts.stream()
                .map(AccountResponse::from)
                .toList();
    }
}
