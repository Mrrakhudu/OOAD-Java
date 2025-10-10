package models;

import interfaces.AccountOperations;
import java.util.Date;

// Abstract Account class
public abstract class Account implements AccountOperations {
    protected String accountNumber;
    protected double balance;
    protected String branch;
    protected BankCustomer customer;
    protected Date dateOpened;

    public Account(String accountNumber, double balance, String branch, BankCustomer customer) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.branch = branch;
        this.customer = customer;
        this.dateOpened = new Date();
    }

    @Override
    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            System.out.println("Deposited: BWP " + amount + " to account: " + accountNumber);
            System.out.println("New balance: BWP " + balance);
        } else {
            System.out.println("Invalid deposit amount");
        }
    }

    @Override
    public double getBalance() {
        return balance;
    }

    public String getAccountNumber() { return accountNumber; }
    public String getBranch() { return branch; }
    public BankCustomer getCustomer() { return customer; }
    public Date getDateOpened() { return dateOpened; }

    protected void setBalance(double balance) { this.balance = balance; }

    public abstract String getAccountType();
    public abstract String getAccountDetails();

    @Override
    public String toString() {
        return getAccountType() + " Account [" +
                "Account Number: " + accountNumber +
                ", Balance: BWP " + String.format("%.2f", balance) +
                ", Branch: " + branch +
                ", Customer: " + customer.getFullName() + "]";
    }
}