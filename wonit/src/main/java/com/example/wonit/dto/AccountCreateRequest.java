package com.example.wonit.dto;

// 계좌개설 요청(JSON)을 받는 상자
// 계좌번호 · 상태 · 잔액 · accountId 는 받지 않습니다. 서버가 정합니다
//   계좌번호: 서버가 발급, 상태: "정상" 고정, 잔액: 0 으로 시작, accountId: DB 가 매김
// customerId 를 Integer(참조형)로 두는 이유: JSON 에서 빠지면 0 이 아니라 null 로 들어와서 "안 보냈다"를 알 수 있습니다
public record AccountCreateRequest(Integer customerId, String accountType) {
}
