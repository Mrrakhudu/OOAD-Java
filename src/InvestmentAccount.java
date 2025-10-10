package models;

import interfaces.InterestBearing;

// Investment Account class
public class InvestmentAccount extends Account implements InterestBearing {
    private static final double MIN_DEPOSIT = 500.00;
    private static final double INTEREST_RATE = 0.05; // 5% monthly

    public InvestmentAccount(String accountNumber, double balance, String branch, BankCustomer customer) {
        super(accountNumber, Math.max(balance, MIN_DEPOSIT), branch, customer);
        if (balance < MIN_DEPOSIT) {
            System.out.println("Initial deposit increased to minimum required BWP " + MIN_DEPOSIT);
        }
    }

    @Override
    public String getAccountType() {
        return "INVESTMENT";
    }

    @Override
    public void withdraw(double amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
            System.out.println("Withdrew: BWP " + amount + " from Investment Account: " + accountNumber);
            System.out.println("New balance: BWP " + balance);
        } else {
            System.out.println("Invalid withdrawal amount or insufficient funds");
        }
    }

    @Override
    public void addInterest() {
        double interest = balance * INTEREST_RATE;
        balance += interest;
        System.out.println("Interest of BWP " + String.format("%.2f", interest) +
                " added to Investment Account: " + accountNumber);
    }

    @Override
    public double getInterestRate() {
        return INTEREST_RATE;
    }

    @Override
    public String getAccountDetails() {
        return toString() + ", Interest Rate: " + (INTEREST_RATE * 100) + "% monthly, Minimum Deposit: BWP " + MIN_DEPOSIT;
    }
}