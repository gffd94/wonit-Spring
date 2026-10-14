package com.example.wonit.service;

import com.example.wonit.dto.AccountCreateRequest;
import com.example.wonit.dto.AccountResponse;
import com.example.wonit.dto.WithdrawRequest;
import com.example.wonit.dto.WithdrawResponse;
import com.example.wonit.entity.Account;
import com.example.wonit.entity.Transaction;
import com.example.wonit.exception.DuplicateAccountException;
import com.example.wonit.repository.AccountRepository;
import com.example.wonit.repository.TransactionRepository;
import com.example.wonit.exception.AccountNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

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

    // 계좌개설: 검증 → 계좌번호 발급 → 저장
    // @Transactional: 정상으로 끝나면 commit, RuntimeException 이 나면 rollback
    @Transactional
    public AccountResponse openAccount(AccountCreateRequest request) {
        if (request.customerId() == null || request.customerId() <= 0) {
            throw new IllegalArgumentException("customerId 는 1 이상이어야 합니다.");
        }
        String accountType = validate(request.accountType(), ACCOUNT_TYPES, "accountType");

        Account account = Account.open(request.customerId(), accountType, newAccountNo());
        return AccountResponse.from(accountRepository.save(account));   // INSERT
    }

    private static final String ACCOUNT_NO_PREFIX = "1002-345-";
    private static final int ACCOUNT_NO_MAX_TRIES = 10;

    // "1002-345-" + 6자리 난수. 이미 있는 번호면 다시 뽑고, 10번 모두 겹치면 포기합니다
    private String newAccountNo() {
        for (int i = 0; i < ACCOUNT_NO_MAX_TRIES; i++) {
            String accountNo = ACCOUNT_NO_PREFIX + String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
            if (!accountRepository.existsByAccountNo(accountNo)) {
                return accountNo;
            }
        }
        throw new DuplicateAccountException("계좌번호 발급에 실패했습니다. 잠시 후 다시 시도해 주세요.");
    }

    // 🏦 출금은 "잔액 UPDATE + 거래 INSERT" 가 한 묶음.  commit/rollback 을 손으로 쓰는 대신
    //    @Transactional 한 줄이 같은 일을 합니다: 메서드가 정상으로 끝나면 commit, RuntimeException 이 나면 둘 다 rollback
    // 잔액 UPDATE 는 save() 를 부르지 않아도 됩니다. 트랜잭션 안에서 바뀐 엔티티는 commit 때 JPA 가 알아서 UPDATE 합니다(변경 감지)
    @Transactional
    public WithdrawResponse withdraw(String accountNo, WithdrawRequest request) {
        if (request.amount() == null) {
            throw new IllegalArgumentException("amount 값이 비어 있습니다.");
        }
        long amount = request.amount();
        Account account = getAccount(accountNo);                                // 없으면 AccountNotFoundException
        account.withdraw(amount);                                              // 지급정지 · 잔액부족이면 예외
        transactionRepository.save(Transaction.withdrawal(account, amount));   // INSERT
        return new WithdrawResponse(accountNo, amount, account.getBalance());
    }   // 여기서 commit. 중간에 RuntimeException(예외 3종)이 나면 둘 다 rollback

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
