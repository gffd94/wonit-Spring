package paymentEx;

public class NaverPayPayment extends Payment implements Refundable {
    public NaverPayPayment(double amount) {
        super(amount);
    }

    @Override
    public void processPayment() {
        displayAmount();
        System.out.println("네이버페이로 " + getAmount() + "원을 결제합니다.");
        increaseCount();
    }

    @Override
    public void refund() {
        System.out.println("네이버페이 결제를 환불합니다.");
    }
}
