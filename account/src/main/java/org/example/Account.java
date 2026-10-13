package org.example;

import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
//@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class Account {
    int accountId; // 계좌번호 pk
    private final String accountNo; // 실제 계좌번호
    String accountType; // 계좌 여부 (입출금/출금)
    int balance;
}
