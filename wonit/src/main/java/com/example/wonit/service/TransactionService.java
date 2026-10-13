package com.example.wonit.service;

import com.example.wonit.dto.TransactionResponse;
import com.example.wonit.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    // 전체 거래내역 조회
    public List<TransactionResponse> findAll() {
        return transactionRepository.findAll().stream()
                .map(TransactionResponse::from)
                .toList();
    }

    // 카테고리로 검색 — 지금은 완전일치. 일부일치로 바꾸려면 findByCategoryContaining 으로 교체
    public List<TransactionResponse> getTransactionByCategory(String category) {
        return transactionRepository.findByCategory(category).stream()
                .map(TransactionResponse::from)
                .toList();
    }

    // 카테고리 또는 메모로 검색 — 지금은 완전일치. 일부일치로 바꾸려면 findByCategoryContainingOrMemoContaining 으로 교체
    public List<TransactionResponse> getTransactionByCategoryOrMemo(String category, String memo) {
        return transactionRepository.findByCategoryOrMemo(category, memo).stream()
                .map(TransactionResponse::from)
                .toList();
    }

    // 최소~최대 금액 사이의 거래내역 검색 (양 끝값 포함)
    public List<TransactionResponse> getTransactionByAmount(long minAmount, long maxAmount) {
        return transactionRepository.findByAmountBetween(minAmount, maxAmount).stream()
                .map(TransactionResponse::from)
                .toList();
    }
}
