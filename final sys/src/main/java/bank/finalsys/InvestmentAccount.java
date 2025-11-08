package bank.finalsys;

public class InvestmentAccount extends Account implements InterestBearing {
    private static final double MIN_DEPOSIT = 500.00;
    private static final double INTEREST_RATE = 0.05; // 5% monthly as per PDF

    public InvestmentAccount(String accountNumber, double balance, String branch, BankCustomer customer) {
        super(accountNumber, Math.max(balance, MIN_DEPOSIT), branch, customer);
        if (balance < MIN_DEPOSIT) {
            throw new IllegalArgumentException("Investment account requires minimum deposit of BWP " + MIN_DEPOSIT);
        }
    }

    @Override
    public String getAccountType() {
        return "INVESTMENT";
    }

    @Override
    public void withdraw(double amount) {
        if (amount > 0 && amount <= getBalance()) {
            setBalance(getBalance() - amount);
        } else {
            throw new IllegalArgumentException("Invalid withdrawal amount or insufficient funds");
        }
    }

    @Override
    public void addInterest() {
        double interest = getBalance() * INTEREST_RATE;
        setBalance(getBalance() + interest);
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