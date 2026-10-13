package com.example.wonit.repository;

import com.example.wonit.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// JpaRepository<Transaction, Long> = "Transaction 엔티티를 다루고, id(txId) 의 타입은 Long"
// findAll() 은 JpaRepository 에서 물려받는다
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // ─────────────────────────────────────────────
    // 완전일치 vs 일부일치
    // findByCategory           → WHERE category = ?            ('식비' 만 찾는다)
    // findByCategoryContaining → WHERE category LIKE '%?%'     ('식' 으로도 '식비' 를 찾는다)
    // ─────────────────────────────────────────────

    // WHERE category = ?
    List<Transaction> findByCategory(String category);

    // WHERE category LIKE '%?%'
//    List<Transaction> findByCategoryContaining(String category);

    // WHERE category = ? OR memo = ?
    List<Transaction> findByCategoryOrMemo(String category, String memo);

    // WHERE category LIKE '%?%' OR memo LIKE '%?%'
//    List<Transaction> findByCategoryContainingOrMemoContaining(String category, String memo);

    // amount BETWEEN min AND max → 양 끝값 포함
    List<Transaction> findByAmountBetween(long minAmount, long maxAmount);
}
