package paymentEx;

public class Main {
    public static void main(String[] args) {
        // 신용카드와 NaverPay 결제 객체 생성
        Payment creditCard = new CreditCardPayment(50000);
        Payment naverPay = new NaverPayPayment(30000);

        // 결제 처리 (정적 메서드 사용)
        PaymentProcessor.process(creditCard);
        PaymentProcessor.process(naverPay);

        System.out.println();

        // 환불 기능 테스트 (다운캐스팅 사용)
        // instanceof 연산자는 객체가 null인 경우 false를 반환합니다.
        // instanceof 연산자를 사용하여 타입을 확인한 후, 안전하게 다운캐스팅할 수 있습니다.
        if (creditCard instanceof Refundable) {
            ((Refundable) creditCard).refund();
        }

        if (naverPay instanceof Refundable) {
            ((Refundable) naverPay).refund();
        }

        System.out.println();

        // 전체 결제 건수 출력 (정적 메서드 사용)
        System.out.println("전체 결제 건수: " + Payment.getCount() + "건");
    }
}