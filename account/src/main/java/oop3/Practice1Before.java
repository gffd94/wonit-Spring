//package oop3;
///**
// * Practice1Before: JAVA 심화 문법 실습.
// *
// * record          : 계좌 데이터를 묶는다.
// * Optional        : 조회 결과가 없을 수 있음을 표현한다.
// * Custom Exception: 실패 이유를 업무 이름으로 표현한다.
// * Lambda / Stream : 목록을 짧고 읽기 좋게 처리한다.
// * try-with-resources: 연결을 자동으로 닫는다.
// */
//import java.util.List;
//public class Practice1Before {
//    static final List<Account> ACCOUNTS = List.of(
//            new Account("1002-345-678901", "입출금", 1_523_000L, "지급정지"),
//            new Account("1002-345-112233", "적금", 1_099_500L, "정상"),
//            new Account("1002-345-998877", "적금", 497_000L, "휴면"));
//
//    static Account findAccount(String accountNo) {
//        FakeConnection conn = new FakeConnection();                  // (가)
//        conn.query("SELECT * FROM account WHERE account_no = ?");
//        for (Account a : ACCOUNTS) {
//            if (a.accountNo().equals(accountNo)) {
//                return a;
//            }
//        }
//        conn.close();
//        return null;                                                 // (나)
//    }
//
//    static boolean withdraw(Account a, long amount) {
//        if (a.status().equals("지급정지")) return false;              // (다)
//        if (a.balance() < amount) return false;                      // (다)
//        return true;
//    }
//
//    public static void main(String[] args) {
//        Account a = findAccount("1002-345-678901");
//        System.out.println("출금 결과: " + withdraw(a, 100_000L));
//        Account none = findAccount("9999-000-000000");
//        System.out.println("없는 계좌 잔액: " + none.balance());
//    }
//}
////record Account(String accountNo, String accountType, long balance, String status) { }
////
////class FakeConnection implements AutoCloseable {
////    FakeConnection() { System.out.println("  연결 열림"); }
////    void query(String sql) { System.out.println("  쿼리 실행"); }
////    @Override public void close() { System.out.println("  연결 닫힘"); }
////}
