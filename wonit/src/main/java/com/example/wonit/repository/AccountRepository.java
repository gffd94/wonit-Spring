package com.example.wonit.repository;

import com.example.wonit.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// 인터페이스만 적으면 JPA 가 구현을 만들어 준다
// JpaRepository<Account, Integer> = "Account 엔티티를 다루고, id 의 타입은 Integer"
// findAll() 은 JpaRepository 에서 물려받는다. saveWithdrawal 도 필요 없다
@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {

    // 구현이 없다. 메서드 "이름"이 곧 질문이다: findBy + AccountNo → account_no 로 찾는다 (필드 이름 accountNo 와 같아야 한다)
    Optional<Account> findByAccountNo(String accountNo);

    // existsBy + AccountNo → 그 계좌번호가 있으면 true. 계좌개설 때 번호 중복 검사에 씁니다
    boolean existsByAccountNo(String accountNo);

    // 여기에 필요한 경우 사용자 정의 쿼리를 추가할 수 있습니다.
    // ─────────────────────────────────────────────
    // JPA 메서드 네이밍 규칙으로 쿼리 자동 생성
    // "find + By + 필드명 + 조건키워드" 형식으로 작성합니다
    // ─────────────────────────────────────────────

    // balance BETWEEN min AND max → 양 끝값 포함
    List<Account> findByBalanceBetween(long minBalance, long maxBalance);

    // ORDER BY balance ASC → 전체 조회라 By 뒤에 조건 없이 바로 OrderBy
    List<Account> findAllByOrderByBalanceAsc();

    // WHERE status = ?
    List<Account> findByStatus(String status);

    // WHERE account_type = ?
    List<Account> findByAccountType(String accountType);

    // ─────────────────────────────────────────────
    // JPQL(Java Persistence Query Language): 테이블명 대신 Entity 클래스명을 사용하는 쿼리
    // SQL: SELECT * FROM Account WHERE ...
    // JPQL: SELECT b FROM Account b WHERE ...  ← Book은 클래스명, b는 별칭(alias)
    //
    // @Param("변수명"): 쿼리의 :변수명 자리에 메서드 파라미터 변수명 : 값을 바인딩합니다
    // %:변수명%  → LIKE '%검색어%' 와 동일한 부분 일치 표현
    // ─────────────────────────────────────────────

}
