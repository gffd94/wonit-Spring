package com.example.wonit.entity;

import com.example.wonit.exception.AccountFrozenException;
import com.example.wonit.exception.InsufficientBalanceException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

// account 표의 한 줄 = Account 객체 하나입니다.
// @Entity 를 붙이면 JPA 가 이 클래스를 표와 짝지어 관리합니다.
// 어제의 Account 에서 달라진 점은 @Entity · @Table · @Id · @Column 이 추가되었고,
// @AllArgsConstructor 가 @NoArgsConstructor 로 바뀌었다는 것입니다.

// @NoArgsConstructor(PROTECTED): JPA 는 빈 객체를 먼저 만들고 값을 나중에 채웁니다.
// 그 빈 생성자만 열어 두고, 다른 곳에서는 new Account() 를 직접 사용하지 못하게 막습니다.

// @AllArgsConstructor 는 뺐습니다.
// 이제 new Account(...) 를 하는 곳이 없습니다.
// JPA 가 표에서 데이터를 읽어서 객체를 만들기 때문입니다.

// @ToString 은 여기서는 안전합니다.
// 다른 엔티티를 필드로 갖지 않기 때문입니다.
// 연결된 엔티티가 있는 Transaction 에는 붙이지 않습니다.

@Entity
@Table(name = "account")
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {

    // 🚨 id 타입은 표의 INT 에 맞춰 Integer 로 사용합니다.
    // Long 으로 쓰면 ddl-auto: validate 가 "타입이 다르다"며 앱을 멈춥니다.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // 번호는 DB 의 AUTO_INCREMENT 가 매깁니다.
    @Column(name = "account_id")
    private Integer accountId;

    @Column(name = "account_no")
    private String accountNo;

    @Column(name = "customer_id")
    private Integer customerId;

    @Column(name = "account_type")
    private String accountType;

    @Column(name = "balance")
    private long balance;

    @Column(name = "status")
    private String status;

    // customer_id · opened_at 칸은 사용하지 않으므로 매핑하지 않습니다.
    // validate 는 매핑한 칸만 확인합니다.

    // 🚨 @Setter 는 일부러 붙이지 않습니다.
    // 잔액은 아래 withdraw() 로만 변경되도록 해서
    // "지급정지 · 잔액부족 검사"를 건너뛰고 잔액을 마음대로 변경하지 못하게 막습니다.
    public void withdraw(long amount) {
        if (status.equals("지급정지")) {
            throw new AccountFrozenException(
                    accountNo + "는 지급정지 계좌라 출금할 수 없습니다.");
        }
        if (balance < amount) {
            throw new InsufficientBalanceException(
                    "잔액이 " + (amount - balance) + "원 부족합니다.");
        }
        balance -= amount;
    }
}
