import java.util.*;
import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;

// Interface for interest-bearing accounts
interface InterestBearing {
    void addInterest();
}

// User class for authentication
class User {
    private String username;
    private String password;
    private String customerId;

    public User(String username, String password, String customerId) {
        this.username = username;
        this.password = password;
        this.customerId = customerId;
    }

    public boolean authenticate(String inputUsername, String inputPassword) {
        return this.username.equals(inputUsername) && this.password.equals(inputPassword);
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getUsername() {
        return username;
    }

    public String toFileString() {
        return username + "|" + password + "|" + customerId;
    }

    public static User fromFileString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length == 3) {
            return new User(parts[0], parts[1], parts[2]);
        }
        return null;
    }
}

// Abstract Account class
abstract class Account {
    protected String accountNumber;
    protected double balance;
    protected String branch;
    protected String customerId;

    public Account(String accountNumber, double balance, String branch, String customerId) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.branch = branch;
        this.customerId = customerId;
    }

    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            System.out.println("Deposited: BWP " + amount + " to account: " + accountNumber);
            System.out.println("New balance: BWP " + balance);
        } else {
            System.out.println("Invalid deposit amount");
        }
    }

    public abstract void withdraw(double amount);

    // Getter methods
    public String getAccountNumber() { return accountNumber; }
    public double getBalance() { return balance; }
    public String getBranch() { return branch; }
    public String getCustomerId() { return customerId; }

    // Setter for balance (protected for subclasses)
    protected void setBalance(double balance) { this.balance = balance; }

    public abstract String getAccountType();

    public String toFileString() {
        return getAccountType() + "|" + accountNumber + "|" + balance + "|" + branch + "|" + customerId;
    }

    public static Account fromFileString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length >= 5) {
            String type = parts[0];
            String accNumber = parts[1];
            double balance = Double.parseDouble(parts[2]);
            String branch = parts[3];
            String customerId = parts[4];

            switch (type) {
                case "SAVINGS":
                    return new SavingsAccount(accNumber, balance, branch, customerId);
                case "INVESTMENT":
                    return new InvestmentAccount(accNumber, balance, branch, customerId);
                case "CHEQUE":
                    String employer = parts.length > 5 ? parts[5] : "";
                    String companyAddress = parts.length > 6 ? parts[6] : "";
                    return new ChequeAccount(accNumber, balance, branch, customerId, employer, companyAddress);
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" +
                "Account Number: " + accountNumber +
                ", Balance: BWP " + String.format("%.2f", balance) +
                ", Branch: " + branch + "]";
    }
}

// Savings Account class
class SavingsAccount extends Account implements InterestBearing {
    public SavingsAccount(String accountNumber, double balance, String branch, String customerId) {
        super(accountNumber, balance, branch, customerId);
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
        double interest = balance * 0.0005;
        balance += interest;
        System.out.println("Interest of BWP " + String.format("%.2f", interest) +
                " added to Savings Account: " + accountNumber);
    }

    @Override
    public String toFileString() {
        return super.toFileString();
    }
}

// Investment Account class
class InvestmentAccount extends Account implements InterestBearing {
    private static final double MIN_DEPOSIT = 500.00;

    public InvestmentAccount(String accountNumber, double balance, String branch, String customerId) {
        super(accountNumber, Math.max(balance, MIN_DEPOSIT), branch, customerId);
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
        double interest = balance * 0.05;
        balance += interest;
        System.out.println("Interest of BWP " + String.format("%.2f", interest) +
                " added to Investment Account: " + accountNumber);
    }

    @Override
    public String toFileString() {
        return super.toFileString();
    }
}

// Cheque Account class
class ChequeAccount extends Account {
    private String employer;
    private String companyAddress;

    public ChequeAccount(String accountNumber, double balance, String branch, String customerId,
                         String employer, String companyAddress) {
        super(accountNumber, balance, branch, customerId);
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
    public String toFileString() {
        return super.toFileString() + "|" + employer + "|" + companyAddress;
    }

    @Override
    public String toString() {
        return super.toString() +
                ", Employer: " + employer +
                ", Company Address: " + companyAddress;
    }
}

// Customer class
class Customer {
    private static AtomicInteger customerCounter = new AtomicInteger(1000);
    private String customerId;
    private String firstName;
    private String surname;
    private String address;
    private List<Account> accounts;

    public Customer(String firstName, String surname, String address) {
        this.customerId = "CUST" + customerCounter.getAndIncrement();
        this.firstName = firstName;
        this.surname = surname;
        this.address = address;
        this.accounts = new ArrayList<>();
    }

    public Customer(String customerId, String firstName, String surname, String address) {
        this.customerId = customerId;
        this.firstName = firstName;
        this.surname = surname;
        this.address = address;
        this.accounts = new ArrayList<>();
    }

    public void addAccount(Account account) {
        accounts.add(account);
        System.out.println("Account " + account.getAccountNumber() + " added to customer: " + getFullName());
    }

    public String getFullName() {
        return firstName + " " + surname;
    }

    // Getter methods
    public String getCustomerId() { return customerId; }
    public String getFirstName() { return firstName; }
    public String getSurname() { return surname; }
    public String getAddress() { return address; }
    public List<Account> getAccounts() { return new ArrayList<>(accounts); }

    public String toFileString() {
        return customerId + "|" + firstName + "|" + surname + "|" + address;
    }

    public static Customer fromFileString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length == 4) {
            return new Customer(parts[0], parts[1], parts[2], parts[3]);
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

// Bank class to manage the system
class Bank {
    private String name;
    private List<Customer> customers;
    private List<Account> accounts;
    private List<User> users;
    private int accountCounter;
    private Scanner scanner;
    private static final String CUSTOMERS_FILE = "customers.txt";
    private static final String ACCOUNTS_FILE = "accounts.txt";
    private static final String USERS_FILE = "users.txt";

    public Bank(String name) {
        this.name = name;
        this.customers = new ArrayList<>();
        this.accounts = new ArrayList<>();
        this.users = new ArrayList<>();
        this.accountCounter = 1000;
        this.scanner = new Scanner(System.in);
        loadData(); // Load existing data when bank is created
    }

    // Save data to text files
    public void saveData() {
        saveCustomers();
        saveAccounts();
        saveUsers();
        System.out.println("Data saved successfully to text files.");
    }

    private void saveCustomers() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(CUSTOMERS_FILE))) {
            for (Customer customer : customers) {
                writer.println(customer.toFileString());
            }
        } catch (IOException e) {
            System.out.println("Error saving customers: " + e.getMessage());
        }
    }

    private void saveAccounts() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(ACCOUNTS_FILE))) {
            for (Account account : accounts) {
                writer.println(account.toFileString());
            }
        } catch (IOException e) {
            System.out.println("Error saving accounts: " + e.getMessage());
        }
    }

    private void saveUsers() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE))) {
            for (User user : users) {
                writer.println(user.toFileString());
            }
        } catch (IOException e) {
            System.out.println("Error saving users: " + e.getMessage());
        }
    }

    // Load data from text files
    private void loadData() {
        loadCustomers();
        loadAccounts();
        loadUsers();
        updateAccountCounter();
    }

    private void loadCustomers() {
        try (Scanner fileScanner = new Scanner(new File(CUSTOMERS_FILE))) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                if (!line.isEmpty()) {
                    Customer customer = Customer.fromFileString(line);
                    if (customer != null) {
                        customers.add(customer);
                    }
                }
            }
        } catch (FileNotFoundException e) {

        }
    }

    private void loadAccounts() {
        try (Scanner fileScanner = new Scanner(new File(ACCOUNTS_FILE))) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                if (!line.isEmpty()) {
                    Account account = Account.fromFileString(line);
                    if (account != null) {
                        accounts.add(account);
                        // Link account to customer
                        Customer customer = findCustomerById(account.getCustomerId());
                        if (customer != null) {
                            customer.addAccount(account);
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {

        }
    }

    private void loadUsers() {
        try (Scanner fileScanner = new Scanner(new File(USERS_FILE))) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                if (!line.isEmpty()) {
                    User user = User.fromFileString(line);
                    if (user != null) {
                        users.add(user);
                    }
                }
            }
        } catch (FileNotFoundException e) {

        }
    }

    private void updateAccountCounter() {
        int maxAccountNumber = 1000;
        for (Account account : accounts) {
            String accNumber = account.getAccountNumber().replace("ACC", "");
            try {
                int num = Integer.parseInt(accNumber);
                if (num > maxAccountNumber) {
                    maxAccountNumber = num;
                }
            } catch (NumberFormatException e) {
                // Skip non-numeric account numbers
            }
        }
        accountCounter = maxAccountNumber + 1;
    }

    private Customer findCustomerById(String customerId) {
        for (Customer customer : customers) {
            if (customer.getCustomerId().equals(customerId)) {
                return customer;
            }
        }
        return null;
    }

    // Method to add a customer to the bank
    public Customer addCustomer(String firstName, String surname, String address) {
        Customer customer = new Customer(firstName, surname, address);
        customers.add(customer);
        return customer;
    }

    // Method to create user account
    public void createUserAccount(Customer customer, String username, String password) {
        User user = new User(username, password, customer.getCustomerId());
        users.add(user);
        System.out.println("User account created successfully for: " + customer.getFullName());
        saveData();
    }

    // Method to authenticate user
    public User authenticateUser() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        for (User user : users) {
            if (user.authenticate(username, password)) {
                return user;
            }
        }
        return null;
    }

    public Customer getCustomerByUser(User user) {
        return findCustomerById(user.getCustomerId());
    }

    // Method to open a new account for a customer
    public Account openAccount(Customer customer, String accountType, double initialDeposit, String branch) {
        String accountNumber = generateAccountNumber();
        Account account;

        switch(accountType.toLowerCase()) {
            case "savings":
                account = new SavingsAccount(accountNumber, initialDeposit, branch, customer.getCustomerId());
                break;
            case "investment":
                account = new InvestmentAccount(accountNumber, initialDeposit, branch, customer.getCustomerId());
                break;
            default:
                throw new IllegalArgumentException("Invalid account type. Use 'savings' or 'investment'");
        }

        customer.addAccount(account);
        accounts.add(account);
        saveData();
        return account;
    }

    // Overloaded method for opening a cheque account
    public Account openAccount(Customer customer, String accountType, double initialDeposit, String branch,
                               String employer, String companyAddress) {
        if (!accountType.equalsIgnoreCase("cheque")) {
            throw new IllegalArgumentException("This method is only for cheque accounts");
        }

        String accountNumber = generateAccountNumber();
        Account account = new ChequeAccount(accountNumber, initialDeposit, branch, customer.getCustomerId(), employer, companyAddress);

        customer.addAccount(account);
        accounts.add(account);
        saveData();
        return account;
    }

    // Method to process monthly interest for all interest-bearing accounts
    public void processMonthlyInterest() {
        System.out.println("\n=== PROCESSING MONTHLY INTEREST ===");
        for (Account account : accounts) {
            if (account instanceof InterestBearing) {
                ((InterestBearing) account).addInterest();
            }
        }
        saveData();
    }

    // Method to find account by account number
    public Account findAccount(String accountNumber) {
        for (Account account : accounts) {
            if (account.getAccountNumber().equals(accountNumber)) {
                return account;
            }
        }
        return null;
    }

    // Method to deposit to a specific account
    public void deposit(String accountNumber, double amount) {
        Account account = findAccount(accountNumber);
        if (account != null) {
            account.deposit(amount);
            saveData();
        } else {
            System.out.println("Account not found: " + accountNumber);
        }
    }

    // Method to withdraw from a specific account
    public void withdraw(String accountNumber, double amount) {
        Account account = findAccount(accountNumber);
        if (account != null) {
            account.withdraw(amount);
            saveData();
        } else {
            System.out.println("Account not found: " + accountNumber);
        }
    }

    // Create new customer account
    public void createNewAccount() {
        System.out.println("\n=== CREATE NEW ACCOUNT ===");
        System.out.print("Enter first name: ");
        String firstName = scanner.nextLine();
        System.out.print("Enter surname: ");
        String surname = scanner.nextLine();
        System.out.print("Enter address: ");
        String address = scanner.nextLine();
        System.out.print("Choose username: ");
        String username = scanner.nextLine();
        System.out.print("Choose password: ");
        String password = scanner.nextLine();

        // Check if username already exists
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                System.out.println("Username already exists. Please choose a different username.");
                return;
            }
        }

        Customer newCustomer = addCustomer(firstName, surname, address);
        createUserAccount(newCustomer, username, password);

        System.out.println("Account created successfully! You can now login with your credentials.");
    }

    // User dashboard
    public void showUserDashboard(User user) {
        Customer customer = getCustomerByUser(user);
        if (customer == null) {
            System.out.println("Customer not found for this user.");
            return;
        }

        boolean exit = false;

        while (!exit) {
            System.out.println("\n=== WELCOME TO YOUR DASHBOARD, " + customer.getFullName().toUpperCase() + " ===");
            System.out.println("1. View All Accounts");
            System.out.println("2. Deposit Funds");
            System.out.println("3. Withdraw Funds");
            System.out.println("4. Open New Account");
            System.out.println("5. View Account Details");
            System.out.println("6. Process Monthly Interest");
            System.out.println("7. View Customer Information");
            System.out.println("8. Logout");
            System.out.print("Choose an option (1-8): ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    viewAllAccounts(customer);
                    break;
                case 2:
                    depositFunds(customer);
                    break;
                case 3:
                    withdrawFunds(customer);
                    break;
                case 4:
                    openNewAccount(customer);
                    break;
                case 5:
                    viewAccountDetails(customer);
                    break;
                case 6:
                    processMonthlyInterest();
                    break;
                case 7:
                    viewCustomerInfo(customer);
                    break;
                case 8:
                    exit = true;
                    System.out.println("Logging out... Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void viewAllAccounts(Customer customer) {
        System.out.println("\n=== YOUR ACCOUNTS ===");
        List<Account> customerAccounts = customer.getAccounts();
        if (customerAccounts.isEmpty()) {
            System.out.println("No accounts found.");
        } else {
            for (Account account : customerAccounts) {
                System.out.println(account);
            }
        }
    }

    private void depositFunds(Customer customer) {
        System.out.print("Enter account number: ");
        String accNumber = scanner.nextLine();
        System.out.print("Enter amount to deposit: ");
        double amount = scanner.nextDouble();
        scanner.nextLine();

        deposit(accNumber, amount);
    }

    private void withdrawFunds(Customer customer) {
        System.out.print("Enter account number: ");
        String accNumber = scanner.nextLine();
        System.out.print("Enter amount to withdraw: ");
        double amount = scanner.nextDouble();
        scanner.nextLine();

        withdraw(accNumber, amount);
    }

    private void openNewAccount(Customer customer) {
        System.out.println("\n=== OPEN NEW ACCOUNT ===");
        System.out.println("1. Savings Account");
        System.out.println("2. Investment Account");
        System.out.println("3. Cheque Account");
        System.out.print("Choose account type (1-3): ");
        int typeChoice = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Enter initial deposit: ");
        double deposit = scanner.nextDouble();
        scanner.nextLine();

        System.out.print("Enter branch: ");
        String branch = scanner.nextLine();

        try {
            switch (typeChoice) {
                case 1:
                    openAccount(customer, "savings", deposit, branch);
                    break;
                case 2:
                    openAccount(customer, "investment", deposit, branch);
                    break;
                case 3:
                    System.out.print("Enter employer: ");
                    String employer = scanner.nextLine();
                    System.out.print("Enter company address: ");
                    String companyAddress = scanner.nextLine();
                    openAccount(customer, "cheque", deposit, branch, employer, companyAddress);
                    break;
                default:
                    System.out.println("Invalid account type.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void viewAccountDetails(Customer customer) {
        System.out.print("Enter account number: ");
        String accNumber = scanner.nextLine();
        Account account = findAccount(accNumber);

        if (account != null && account.getCustomerId().equals(customer.getCustomerId())) {
            System.out.println(account);
        } else {
            System.out.println("Account not found or doesn't belong to you.");
        }
    }

    private void viewCustomerInfo(Customer customer) {
        System.out.println(customer);
    }

    // Helper method to generate unique account numbers
    private String generateAccountNumber() {
        return "ACC" + (accountCounter++);
    }

    // Getter methods
    public String getName() { return name; }
    public List<Customer> getCustomers() { return new ArrayList<>(customers); }
    public List<Account> getAccounts() { return new ArrayList<>(accounts); }
}

// Main class
public class BankManagement {
    public static void main(String[] args) {
        Bank bank = new Bank("Botswana National Bank");
        Scanner scanner = new Scanner(System.in);

        boolean exit = false;

        while (!exit) {
            System.out.println("\n=== WELCOME TO " + bank.getName().toUpperCase() + " ===");
            System.out.println("1. Login");
            System.out.println("2. Create New Account");
            System.out.println("3. Exit");
            System.out.print("Choose an option (1-3): ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    User user = bank.authenticateUser();
                    if (user != null) {
                        Customer customer = bank.getCustomerByUser(user);
                        if (customer != null) {
                            System.out.println("\nLogin successful! Welcome, " + customer.getFullName());
                            bank.showUserDashboard(user);
                        }
                    } else {
                        System.out.println("Invalid username or password. Please try again.");
                    }
                    break;
                case 2:
                    bank.createNewAccount();
                    break;
                case 3:
                    exit = true;
                    System.out.println("Thank you for using our banking system. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
        scanner.close();
    }

}