package models;

import interfaces.InterestBearing;

// Savings Account class
public class SavingsAccount extends Account implements InterestBearing {
    private static final double INTEREST_RATE = 0.0005; // 0.05% monthly

    public SavingsAccount(String accountNumber, double balance, String branch, BankCustomer customer) {
        super(accountNumber, balance, branch, customer);
    }

    @Override
    public String getAccountType() {
        return "SAVINGS";
    }

    @Override
    public void withdraw(double amount) {
        System.out.println("Withdrawals not allowed from Savings Account");
    }

    @Override
    public void addInterest() {
        double interest = balance * INTEREST_RATE;
        balance += interest;
        System.out.println("Interest of BWP " + String.format("%.2f", interest) +
                " added to Savings Account: " + accountNumber);
    }

    @Override
    public double getInterestRate() {
        return INTEREST_RATE;
    }

    @Override
    public String getAccountDetails() {
        return toString() + ", Interest Rate: " + (INTEREST_RATE * 100) + "% monthly";
    }
}