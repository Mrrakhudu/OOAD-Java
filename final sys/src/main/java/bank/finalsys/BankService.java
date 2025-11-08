package bank.finalsys;

import java.io.*;
import java.util.*;

public class BankService {
    private List<BankCustomer> customers;
    private List<Account> accounts;
    private List<User> users;
    private int accountCounter;
    private int customerCounter;

    private static final String CUSTOMERS_FILE = "customers.txt";
    private static final String ACCOUNTS_FILE = "accounts.txt";
    private static final String USERS_FILE = "users.txt";
    private static final String TELLERS_FILE = "tellers.txt";

    public BankService() {
        this.customers = new ArrayList<>();
        this.accounts = new ArrayList<>();
        this.users = new ArrayList<>();
        this.accountCounter = 1000;
        this.customerCounter = 1000;
        loadData();

        if (users.stream().noneMatch(u -> u.getRole() == UserRole.TELLER)) {
            BankTeller defaultTeller = new BankTeller("teller", "password", "EMP001", "Default Teller");
            users.add(defaultTeller);
            saveData();
        }
    }

    public User authenticateUser(String username, String password) {
        for (User user : users) {
            if (user.authenticate(username, password)) {
                return user;
            }
        }
        return null;
    }

    public BankCustomer createCustomer(String username, String password, String firstName,
                                       String surname, String address) {
        if (users.stream().anyMatch(u -> u.getUsername().equals(username))) {
            throw new IllegalArgumentException("Username already exists");
        }

        String customerId = "CUST" + (customerCounter++);
        BankCustomer newCustomer = new BankCustomer(username, password, customerId, firstName, surname, address);
        customers.add(newCustomer);
        users.add(newCustomer);
        saveData();
        return newCustomer;
    }

    public BankCustomer findCustomerByUsername(String username) {
        for (User user : users) {
            if (user instanceof BankCustomer && user.getUsername().equals(username)) {
                return (BankCustomer) user;
            }
        }
        return null;
    }

    public void updateCustomer(String username, String firstName, String surname, String address) {
        BankCustomer customer = findCustomerByUsername(username);
        if (customer != null) {
            if (firstName != null && !firstName.isEmpty()) customer.setFirstName(firstName);
            if (surname != null && !surname.isEmpty()) customer.setSurname(surname);
            if (address != null && !address.isEmpty()) customer.setAddress(address);
            saveData();
        }
    }

    public Account openAccount(String customerUsername, String accountType, double deposit,
                               String branch, String employer, String companyAddress) {
        BankCustomer customer = findCustomerByUsername(customerUsername);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found");
        }

        String accountNumber = generateAccountNumber();
        Account account = null;

        switch (accountType.toUpperCase()) {
            case "SAVINGS":
                account = new SavingsAccount(accountNumber, deposit, branch, customer);
                break;
            case "INVESTMENT":
                account = new InvestmentAccount(accountNumber, deposit, branch, customer);
                break;
            case "CHEQUE":
                account = new ChequeAccount(accountNumber, deposit, branch, customer, employer, companyAddress);
                break;
            default:
                throw new IllegalArgumentException("Invalid account type");
        }

        customer.addAccount(account);
        accounts.add(account);
        saveData();
        return account;
    }

    public void deposit(String accountNumber, double amount) {
        Account account = findAccount(accountNumber);
        if (account != null) {
            account.deposit(amount);
            saveData();
        } else {
            throw new IllegalArgumentException("Account not found");
        }
    }

    public void withdraw(String accountNumber, double amount) {
        Account account = findAccount(accountNumber);
        if (account != null) {
            account.withdraw(amount);
            saveData();
        } else {
            throw new IllegalArgumentException("Account not found");
        }
    }

    public void processMonthlyInterest() {
        for (Account account : accounts) {
            if (account instanceof InterestBearing) {
                ((InterestBearing) account).addInterest();
            }
        }
        saveData();
    }

    public List<BankCustomer> getAllCustomers() { return new ArrayList<>(customers); }
    public List<Account> getAllAccounts() { return new ArrayList<>(accounts); }

    public Account findAccount(String accountNumber) {
        return accounts.stream()
                .filter(acc -> acc.getAccountNumber().equals(accountNumber))
                .findFirst()
                .orElse(null);
    }

    // File operations (same implementation as before, but in new package)
    private void loadData() {
        loadTellers();
        loadCustomers();
        loadAccounts();
        updateCounters();
    }

    private void loadTellers() {
        try (Scanner fileScanner = new Scanner(new File(TELLERS_FILE))) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                if (!line.isEmpty()) {
                    BankTeller teller = BankTeller.fromFileString(line);
                    if (teller != null) users.add(teller);
                }
            }
        } catch (FileNotFoundException e) {
            // File doesn't exist yet
        }
    }

    private void loadCustomers() {
        try (Scanner fileScanner = new Scanner(new File(CUSTOMERS_FILE))) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                if (!line.isEmpty()) {
                    BankCustomer customer = BankCustomer.fromFileString(line);
                    if (customer != null) {
                        customers.add(customer);
                        users.add(customer);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            // File doesn't exist yet
        }
    }

    private void loadAccounts() {
        try (Scanner fileScanner = new Scanner(new File(ACCOUNTS_FILE))) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                if (!line.isEmpty()) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 5) {
                        String type = parts[0];
                        String accNumber = parts[1];
                        double balance = Double.parseDouble(parts[2]);
                        String branch = parts[3];
                        String customerId = parts[4];

                        BankCustomer customer = findCustomerById(customerId);
                        if (customer != null) {
                            Account account = createAccountFromData(type, accNumber, balance, branch, customer, parts);
                            if (account != null) {
                                accounts.add(account);
                                customer.addAccount(account);
                            }
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            // File doesn't exist yet
        }
    }

    private Account createAccountFromData(String type, String accNumber, double balance,
                                          String branch, BankCustomer customer, String[] parts) {
        switch (type) {
            case "SAVINGS":
                return new SavingsAccount(accNumber, balance, branch, customer);
            case "INVESTMENT":
                return new InvestmentAccount(accNumber, balance, branch, customer);
            case "CHEQUE":
                if (parts.length >= 7) {
                    return new ChequeAccount(accNumber, balance, branch, customer, parts[5], parts[6]);
                }
                break;
        }
        return null;
    }

    private BankCustomer findCustomerById(String customerId) {
        return customers.stream()
                .filter(c -> c.getCustomerId().equals(customerId))
                .findFirst()
                .orElse(null);
    }

    public void saveData() {
        saveCustomers();
        saveAccounts();
        saveUsers();
        saveTellers();
    }

    private void saveCustomers() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(CUSTOMERS_FILE))) {
            for (BankCustomer customer : customers) {
                writer.println(customer.toFileString());
            }
        } catch (IOException e) {
            System.out.println("Error saving customers: " + e.getMessage());
        }
    }

    private void saveAccounts() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(ACCOUNTS_FILE))) {
            for (Account account : accounts) {
                String line = account.getAccountType() + "|" +
                        account.getAccountNumber() + "|" +
                        account.getBalance() + "|" +
                        account.getBranch() + "|" +
                        account.getCustomer().getCustomerId();

                if (account instanceof ChequeAccount) {
                    ChequeAccount chequeAccount = (ChequeAccount) account;
                    line += "|" + chequeAccount.getEmployer() + "|" + chequeAccount.getCompanyAddress();
                }
                writer.println(line);
            }
        } catch (IOException e) {
            System.out.println("Error saving accounts: " + e.getMessage());
        }
    }

    private void saveUsers() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE))) {
            for (User user : users) {
                if (user instanceof BankCustomer) {
                    writer.println(user.toFileString());
                }
            }
        } catch (IOException e) {
            System.out.println("Error saving users: " + e.getMessage());
        }
    }

    private void saveTellers() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(TELLERS_FILE))) {
            for (User user : users) {
                if (user instanceof BankTeller) {
                    writer.println(user.toFileString());
                }
            }
        } catch (IOException e) {
            System.out.println("Error saving tellers: " + e.getMessage());
        }
    }

    private void updateCounters() {
        int maxAccountNumber = accounts.stream()
                .map(Account::getAccountNumber)
                .map(num -> num.replace("ACC", ""))
                .mapToInt(num -> {
                    try {
                        return Integer.parseInt(num);
                    } catch (NumberFormatException e) {
                        return 1000;
                    }
                })
                .max()
                .orElse(1000);
        accountCounter = maxAccountNumber + 1;

        int maxCustomerNumber = customers.stream()
                .map(BankCustomer::getCustomerId)
                .map(id -> id.replace("CUST", ""))
                .mapToInt(id -> {
                    try {
                        return Integer.parseInt(id);
                    } catch (NumberFormatException e) {
                        return 1000;
                    }
                })
                .max()
                .orElse(1000);
        customerCounter = maxCustomerNumber + 1;
    }

    private String generateAccountNumber() {
        return "ACC" + (accountCounter++);
    }
}