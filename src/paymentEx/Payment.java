package paymentEx;

public abstract class Payment {
    private static int count;
    private double amount;

    public Payment(double amount) {
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }

    public void displayAmount() {
        System.out.println("결제 금액: " + amount + "원");
    }

    public abstract void processPayment();

    protected static void increaseCount() {
        count++;
    }

    public static int getCount() {
        return count;
    }
}
