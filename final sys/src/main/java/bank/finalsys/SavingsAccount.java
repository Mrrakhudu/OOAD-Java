package bank.finalsys;

public class SavingsAccount extends Account implements InterestBearing {
    private static final double INTEREST_RATE = 0.0005; // 0.05% monthly as per PDF
    private static final double MIN_DEPOSIT = 50.0;

    public SavingsAccount(String accountNumber, double balance, String branch, BankCustomer customer) {
        super(accountNumber, Math.max(balance, MIN_DEPOSIT), branch, customer);
        if (balance < MIN_DEPOSIT) {
            throw new IllegalArgumentException("Savings account requires minimum deposit of BWP " + MIN_DEPOSIT);
        }
    }

    @Override
    public String getAccountType() {
        return "SAVINGS";
    }

    @Override
    public void withdraw(double amount) {
        throw new UnsupportedOperationException("Withdrawals not allowed from Savings Account");
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
        return toString() + ", Interest Rate: " + (INTEREST_RATE * 100) + "% monthly";
    }
}