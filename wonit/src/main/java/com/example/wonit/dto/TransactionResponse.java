package com.example.wonit.dto;

import com.example.wonit.entity.Transaction;

import java.time.LocalDateTime;

// 거래내역 응답 상자. txId · accountId · transferId 는 내부용 번호라서 밖으로 내보내지 않습니다
public record TransactionResponse(String txType, long amount,
                                  String category, String memo, String counterparty,
                                  LocalDateTime txDatetime) {

    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.getTxType(),
                transaction.getAmount(),
                transaction.getCategory(),
                transaction.getMemo(),
                transaction.getCounterparty(),
                transaction.getTxDatetime());
    }
}
