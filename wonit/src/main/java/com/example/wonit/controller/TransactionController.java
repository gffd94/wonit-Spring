package com.example.wonit.controller;

import com.example.wonit.dto.TransactionResponse;
import com.example.wonit.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    // 모든 거래내역 조회
    // http://localhost:8080/api/transactions
    @GetMapping
    public List<TransactionResponse> findAll() {
        return transactionService.findAll();
    }

    // ─────────────────────────────────────────────
    // 아래 두 메서드는 주소(/select)가 같습니다.
    // params 조건으로 memo 가 "없으면" 이쪽, "있으면" 아래쪽으로 갑니다.
    // ─────────────────────────────────────────────

    // 카테고리명으로 거래내역 검색
    // http://localhost:8080/api/transactions/select?category=식비
    @GetMapping(value = "/select", params = "!memo")
    public List<TransactionResponse> getTransactionByCategory(@RequestParam String category) {
        return transactionService.getTransactionByCategory(category);
    }

    // 카테고리 또는 메모로 거래내역 검색 (memo 만 보내도 이쪽으로 옵니다)
    // http://localhost:8080/api/transactions/select?category=식비&memo=회사
    // memo 를 required = false 로 둔 이유: Swagger 는 같은 주소의 API 를 하나만 보여 줍니다(이 메서드).
    // memo 가 필수면 Swagger 에서 memo 를 비울 수 없어 위의 getTransactionByCategory 를 호출할 방법이 없습니다.
    // params = "memo" 조건 때문에 이 메서드는 어차피 memo 가 있을 때만 호출되므로 동작은 같습니다.
    @GetMapping(value = "/select", params = "memo")
    public List<TransactionResponse> getTransactionByCategoryOrMemo(@RequestParam(required = false) String category,
                                                                    @RequestParam(required = false) String memo) {
        return transactionService.getTransactionByCategoryOrMemo(category, memo);
    }

    // 최소~최대 금액으로 거래내역 검색
    // http://localhost:8080/api/transactions/select2?minAmount=10000&maxAmount=20000
    @GetMapping("/select2")
    public List<TransactionResponse> getTransactionByAmount(@RequestParam long minAmount,
                                                            @RequestParam long maxAmount) {
        return transactionService.getTransactionByAmount(minAmount, maxAmount);
    }
}
