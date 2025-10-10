package interfaces;

// Interface for account operations
public interface AccountOperations {
    void deposit(double amount);
    void withdraw(double amount);
    double getBalance();
}