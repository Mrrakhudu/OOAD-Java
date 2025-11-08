package bank.finalsys;

public interface AccountOperations {
    void deposit(double amount);
    void withdraw(double amount);
    double getBalance();
}