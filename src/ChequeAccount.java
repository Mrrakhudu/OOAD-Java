package models;

// Cheque Account class
public class ChequeAccount extends Account {
    private String employer;
    private String companyAddress;

    public ChequeAccount(String accountNumber, double balance, String branch, BankCustomer customer,
                         String employer, String companyAddress) {
        super(accountNumber, balance, branch, customer);

        if (employer == null || employer.trim().isEmpty()) {
            throw new IllegalArgumentException("Employer information is required for cheque account");
        }

        this.employer = employer;
        this.companyAddress = companyAddress;
    }

    @Override
    public String getAccountType() {
        return "CHEQUE";
    }

    @Override
    public void withdraw(double amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
            System.out.println("Withdrew: BWP " + amount + " from Cheque Account: " + accountNumber);
            System.out.println("New balance: BWP " + balance);
        } else {
            System.out.println("Invalid withdrawal amount or insufficient funds");
        }
    }

    public String getEmployer() { return employer; }
    public String getCompanyAddress() { return companyAddress; }

    @Override
    public String getAccountDetails() {
        return toString() + ", Employer: " + employer + ", Company Address: " + companyAddress;
    }

    @Override
    public String toString() {
        return super.toString() +
                ", Employer: " + employer +
                ", Company Address: " + companyAddress;
    }
}