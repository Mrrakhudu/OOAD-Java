package services;

import models.*;
import enums.UserRole;
import interfaces.InterestBearing;

import java.util.*;
import java.io.*;

public class Bank {
    private String name;
    private List<BankCustomer> customers;
    private List<Account> accounts;
    private List<User> users;
    private int accountCounter;
    private int customerCounter;
    private Scanner scanner;
    private static final String CUSTOMERS_FILE = "customers.txt";
    private static final String ACCOUNTS_FILE = "accounts.txt";
    private static final String USERS_FILE = "users.txt";
    private static final String TELLERS_FILE = "tellers.txt";

    public Bank(String name) {
        this.name = name;
        this.customers = new ArrayList<>();
        this.accounts = new ArrayList<>();
        this.users = new ArrayList<>();
        this.accountCounter = 1000;
        this.customerCounter = 1000;
        this.scanner = new Scanner(System.in);
        loadData();

        // Create a default teller if none exists
        if (users.stream().noneMatch(u -> u.getRole() == UserRole.TELLER)) {
            BankTeller defaultTeller = new BankTeller("teller", "password", "EMP001", "Default Teller");
            users.add(defaultTeller);
            saveData();
            System.out.println("Default teller created: username='teller', password='password'");
        }
    }

    // Save data to text files
    public void saveData() {
        saveCustomers();
        saveAccounts();
        saveUsers();
        saveTellers();
        System.out.println("Data saved successfully to text files.");
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

    // Load data from text files
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
                    if (teller != null) {
                        users.add(teller);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            // File doesn't exist yet, that's okay
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
            // File doesn't exist yet, that's okay
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
                            Account account = null;

                            switch (type) {
                                case "SAVINGS":
                                    account = new SavingsAccount(accNumber, balance, branch, customer);
                                    break;
                                case "INVESTMENT":
                                    account = new InvestmentAccount(accNumber, balance, branch, customer);
                                    break;
                                case "CHEQUE":
                                    if (parts.length >= 7) {
                                        String employer = parts[5];
                                        String companyAddress = parts[6];
                                        account = new ChequeAccount(accNumber, balance, branch, customer, employer, companyAddress);
                                    }
                                    break;
                            }

                            if (account != null) {
                                accounts.add(account);
                                customer.addAccount(account);
                            }
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            // File doesn't exist yet, that's okay
        }
    }

    private void updateCounters() {
        // Update account counter
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

        // Update customer counter
        int maxCustomerNumber = 1000;
        for (BankCustomer customer : customers) {
            String custId = customer.getCustomerId().replace("CUST", "");
            try {
                int num = Integer.parseInt(custId);
                if (num > maxCustomerNumber) {
                    maxCustomerNumber = num;
                }
            } catch (NumberFormatException e) {
                // Skip non-numeric customer IDs
            }
        }
        customerCounter = maxCustomerNumber + 1;
    }

    private BankCustomer findCustomerById(String customerId) {
        for (BankCustomer customer : customers) {
            if (customer.getCustomerId().equals(customerId)) {
                return customer;
            }
        }
        return null;
    }

    private BankCustomer findCustomerByUsername(String username) {
        for (User user : users) {
            if (user instanceof BankCustomer && user.getUsername().equals(username)) {
                return (BankCustomer) user;
            }
        }
        return null;
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

    // Method to create a new customer (only for tellers)
    public void createNewCustomer(BankTeller teller) {
        System.out.println("\n=== CREATE NEW CUSTOMER ===");
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

        String customerId = "CUST" + (customerCounter++);
        BankCustomer newCustomer = new BankCustomer(username, password, customerId, firstName, surname, address);
        customers.add(newCustomer);
        users.add(newCustomer);

        System.out.println("Customer created successfully! Customer ID: " + customerId);
        saveData();
    }

    // Method to modify customer data (only for tellers)
    public void modifyCustomerData(BankTeller teller) {
        System.out.println("\n=== MODIFY CUSTOMER DATA ===");
        System.out.print("Enter customer username: ");
        String username = scanner.nextLine();

        BankCustomer customer = findCustomerByUsername(username);
        if (customer == null) {
            System.out.println("Customer not found.");
            return;
        }

        System.out.println("Current customer data:");
        System.out.println("First Name: " + customer.getFirstName());
        System.out.println("Surname: " + customer.getSurname());
        System.out.println("Address: " + customer.getAddress());

        System.out.println("\nEnter new data (press Enter to keep current value):");
        System.out.print("New first name: ");
        String newFirstName = scanner.nextLine();
        if (!newFirstName.isEmpty()) {
            customer.setFirstName(newFirstName);
        }

        System.out.print("New surname: ");
        String newSurname = scanner.nextLine();
        if (!newSurname.isEmpty()) {
            customer.setSurname(newSurname);
        }

        System.out.print("New address: ");
        String newAddress = scanner.nextLine();
        if (!newAddress.isEmpty()) {
            customer.setAddress(newAddress);
        }

        System.out.println("Customer data updated successfully!");
        saveData();
    }

    // Method to view customer accounts (only for tellers)
    public void viewCustomerAccounts(BankTeller teller) {
        System.out.println("\n=== VIEW CUSTOMER ACCOUNTS ===");
        System.out.print("Enter customer username: ");
        String username = scanner.nextLine();

        BankCustomer customer = findCustomerByUsername(username);
        if (customer == null) {
            System.out.println("Customer not found.");
            return;
        }

        System.out.println("Customer: " + customer.getFullName());
        List<Account> customerAccounts = customer.getAccounts();
        if (customerAccounts.isEmpty()) {
            System.out.println("No accounts found for this customer.");
        } else {
            System.out.println("Accounts:");
            for (Account account : customerAccounts) {
                System.out.println("  " + account.getAccountDetails());
            }
        }
    }

    // Method to open a new account for a customer (only for tellers)
    public void openAccountForCustomer(BankTeller teller) {
        System.out.println("\n=== OPEN NEW ACCOUNT FOR CUSTOMER ===");
        System.out.print("Enter customer username: ");
        String username = scanner.nextLine();

        BankCustomer customer = findCustomerByUsername(username);
        if (customer == null) {
            System.out.println("Customer not found.");
            return;
        }

        System.out.println("Customer: " + customer.getFullName());
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
            String accountNumber = generateAccountNumber();
            Account account = null;

            switch (typeChoice) {
                case 1:
                    account = new SavingsAccount(accountNumber, deposit, branch, customer);
                    break;
                case 2:
                    account = new InvestmentAccount(accountNumber, deposit, branch, customer);
                    break;
                case 3:
                    System.out.print("Enter employer: ");
                    String employer = scanner.nextLine();
                    System.out.print("Enter company address: ");
                    String companyAddress = scanner.nextLine();
                    account = new ChequeAccount(accountNumber, deposit, branch, customer, employer, companyAddress);
                    break;
                default:
                    System.out.println("Invalid account type.");
                    return;
            }

            if (account != null) {
                customer.addAccount(account);
                accounts.add(account);
                System.out.println("Account created successfully! Account Number: " + accountNumber);
                saveData();
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Teller dashboard
    public void showTellerDashboard(BankTeller teller) {
        boolean exit = false;

        while (!exit) {
            System.out.println("\n=== TELLER DASHBOARD ===");
            System.out.println("Welcome, " + teller.getDisplayName());
            System.out.println("1. Create New Customer");
            System.out.println("2. Modify Customer Data");
            System.out.println("3. View Customer Accounts");
            System.out.println("4. Open Account for Customer");
            System.out.println("5. Process Monthly Interest");
            System.out.println("6. View All Customers");
            System.out.println("7. Logout");
            System.out.print("Choose an option (1-7): ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    createNewCustomer(teller);
                    break;
                case 2:
                    modifyCustomerData(teller);
                    break;
                case 3:
                    viewCustomerAccounts(teller);
                    break;
                case 4:
                    openAccountForCustomer(teller);
                    break;
                case 5:
                    processMonthlyInterest();
                    break;
                case 6:
                    displayAllCustomers();
                    break;
                case 7:
                    exit = true;
                    System.out.println("Logging out... Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    // Customer dashboard
    public void showCustomerDashboard(BankCustomer customer) {
        boolean exit = false;

        while (!exit) {
            System.out.println("\n=== CUSTOMER DASHBOARD ===");
            System.out.println("Welcome, " + customer.getFullName());
            System.out.println("1. View All Accounts");
            System.out.println("2. Deposit Funds");
            System.out.println("3. Withdraw Funds");
            System.out.println("4. View Account Details");
            System.out.println("5. View Customer Information");
            System.out.println("6. Logout");
            System.out.print("Choose an option (1-6): ");

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
                    viewAccountDetails(customer);
                    break;
                case 5:
                    viewCustomerInfo(customer);
                    break;
                case 6:
                    exit = true;
                    System.out.println("Logging out... Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void viewAllAccounts(BankCustomer customer) {
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

    private void depositFunds(BankCustomer customer) {
        System.out.print("Enter account number: ");
        String accNumber = scanner.nextLine();
        System.out.print("Enter amount to deposit: ");
        double amount = scanner.nextDouble();
        scanner.nextLine();

        deposit(accNumber, amount);
    }

    private void withdrawFunds(BankCustomer customer) {
        System.out.print("Enter account number: ");
        String accNumber = scanner.nextLine();
        System.out.print("Enter amount to withdraw: ");
        double amount = scanner.nextDouble();
        scanner.nextLine();

        withdraw(accNumber, amount);
    }

    private void viewAccountDetails(BankCustomer customer) {
        System.out.print("Enter account number: ");
        String accNumber = scanner.nextLine();
        Account account = findAccount(accNumber);

        if (account != null && account.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
            System.out.println(account.getAccountDetails());
        } else {
            System.out.println("Account not found or doesn't belong to you.");
        }
    }

    private void viewCustomerInfo(BankCustomer customer) {
        System.out.println(customer);
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

    // Method to display all customers and their accounts
    public void displayAllCustomers() {
        System.out.println("\n=== ALL CUSTOMERS ===");
        for (BankCustomer customer : customers) {
            System.out.println(customer);
            List<Account> customerAccounts = customer.getAccounts();
            if (!customerAccounts.isEmpty()) {
                System.out.println("  Accounts:");
                for (Account account : customerAccounts) {
                    System.out.println("    " + account.getAccountDetails());
                }
            }
            System.out.println();
        }
    }

    // Helper method to generate unique account numbers
    private String generateAccountNumber() {
        return "ACC" + (accountCounter++);
    }

    // Getter methods
    public String getName() { return name; }
    public List<BankCustomer> getCustomers() { return new ArrayList<>(customers); }
    public List<Account> getAccounts() { return new ArrayList<>(accounts); }
}