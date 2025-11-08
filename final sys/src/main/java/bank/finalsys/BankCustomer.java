package bank.finalsys;

import java.util.ArrayList;
import java.util.List;

public class BankCustomer extends User {
    private String customerId;
    private String firstName;
    private String surname;
    private String address;
    private List<Account> accounts;

    public BankCustomer(String username, String password, String customerId,
                        String firstName, String surname, String address) {
        super(username, password, UserRole.CUSTOMER);
        this.customerId = customerId;
        this.firstName = firstName;
        this.surname = surname;
        this.address = address;
        this.accounts = new ArrayList<>();
    }

    public BankCustomer(String username, String password, String customerId,
                        String firstName, String surname, String address, UserRole role) {
        super(username, password, role);
        this.customerId = customerId;
        this.firstName = firstName;
        this.surname = surname;
        this.address = address;
        this.accounts = new ArrayList<>();
    }

    public void addAccount(Account account) {
        accounts.add(account);
    }

    public String getCustomerId() { return customerId; }
    public String getFirstName() { return firstName; }
    public String getSurname() { return surname; }
    public String getAddress() { return address; }
    public List<Account> getAccounts() { return new ArrayList<>(accounts); }

    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setSurname(String surname) { this.surname = surname; }
    public void setAddress(String address) { this.address = address; }

    public String getFullName() {
        return firstName + " " + surname;
    }

    @Override
    public String getDisplayName() {
        return getFullName() + " (Customer)";
    }

    @Override
    public String toFileString() {
        return getUsername() + "|" + getPassword() + "|" + getRole().toString() + "|" +
                customerId + "|" + firstName + "|" + surname + "|" + address;
    }

    public static BankCustomer fromFileString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length >= 7) {
            return new BankCustomer(parts[0], parts[1], parts[3], parts[4], parts[5], parts[6], UserRole.valueOf(parts[2]));
        }
        return null;
    }

    @Override
    public String toString() {
        return "Customer [ID: " + customerId +
                ", Name: " + getFullName() +
                ", Address: " + address +
                ", Number of Accounts: " + accounts.size() + "]";
    }
}