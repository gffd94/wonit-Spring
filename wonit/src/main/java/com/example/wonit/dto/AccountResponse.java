package com.example.wonit.dto;

import com.example.wonit.entity.Account;

// 바깥(JSON)으로 내보내는 "안 바뀌는 상자". 바뀌는 상자(Account)는 class, 응답 상자는 record 다
// accountId 는 내부용 번호라서 밖으로 내보내지 않습니다
public record AccountResponse(String accountNo, String accountType, long balance, String status) {

    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getAccountNo(),
                account.getAccountType(),
                account.getBalance(),
                account.getStatus());
    }
}