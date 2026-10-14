package com.example.wonit.dto;

// 출금 결과 응답 상자. 어느 계좌에서 얼마를 뺐고, 남은 잔액이 얼마인지
public record WithdrawResponse(String accountNo, long amount, long balance) {
}
