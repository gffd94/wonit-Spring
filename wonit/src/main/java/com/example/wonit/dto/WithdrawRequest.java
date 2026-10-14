package com.example.wonit.dto;

// 출금 요청(JSON)을 받는 상자. {"amount": 100000}
// amount 를 Long(참조형)으로 두는 이유: JSON 에서 빠지면 0 이 아니라 null 로 들어와서 "안 보냈다"를 알 수 있습니다
public record WithdrawRequest(Long amount) {
}
