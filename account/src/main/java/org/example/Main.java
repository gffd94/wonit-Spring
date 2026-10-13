package org.example;

public class Main {
    public static void main(String[] args) {
        Account account = new Account("204-234234-546464");
        account.setAccountId(1);

        System.out.println(account.getAccountId());
        System.out.println(account.getAccountNo());
        System.out.println(account.toString());

        Account account1 = new Account("304-233334-523234");
        account1.setAccountId(2);

        System.out.println(account1.getAccountId());
        System.out.println(account1.getAccountNo());
        System.out.println(account1.toString());

        System.out.println(account.equals(account1));
    }
}