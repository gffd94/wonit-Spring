package com.example.wonit.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

// transaction 표의 한 줄 = Transaction 객체 하나입니다.
// account_id · transfer_id 는 @ManyToOne 연관관계 없이 숫자 칸으로만 매핑합니다.
// (transfer 표는 만들지 않으므로, 연관관계를 걸면 FK 때문에 data.sql INSERT 가 실패합니다)
@Entity
@Table(name = "transaction")
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transaction {

    // tx_id 는 BIGINT 라서 Long 으로 사용합니다.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tx_id")
    private Long txId;

    @Column(name = "account_id")
    private Integer accountId;

    // 이체가 아닌 거래는 transfer_id 가 NULL 이라서 Long(참조형)으로 둡니다.
    @Column(name = "transfer_id")
    private Long transferId;

    @Column(name = "tx_type")
    private String txType;

    @Column(name = "amount")
    private long amount;

    @Column(name = "balance_after")
    private long balanceAfter;

    @Column(name = "category")
    private String category;

    @Column(name = "memo")
    private String memo;

    @Column(name = "counterparty")
    private String counterparty;

    @Column(name = "tx_datetime")
    private LocalDateTime txDatetime;

    // 출금 거래 1건. new Transaction() 은 막혀 있으므로 이 메서드로만 만듭니다
    // account.withdraw() 가 끝난 "뒤"에 불러야 balance_after 에 출금 후 잔액이 들어갑니다
    public static Transaction withdrawal(Account account, long amount) {
        Transaction tx = new Transaction();
        tx.accountId = account.getAccountId();
        tx.txType = "출금";
        tx.amount = amount;
        tx.balanceAfter = account.getBalance();
        tx.txDatetime = LocalDateTime.now();
        return tx;
    }
}
