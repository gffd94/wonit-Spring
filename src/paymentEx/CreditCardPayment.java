package paymentEx;

public class CreditCardPayment extends Payment implements Refundable {
    public CreditCardPayment(double amount) {
        super(amount);
    }

    @Override
    public void processPayment() {
        displayAmount();
        System.out.println("신용카드로 " + getAmount() + "원을 결제합니다.");
        increaseCount();
    }

    @Override
    public void refund() {
        System.out.println("신용카드 결제를 환불합니다.");
    }
}
