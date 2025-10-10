import services.Bank;
import models.User;
import models.BankTeller;
import models.BankCustomer;
import enums.UserRole;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Bank bank = new Bank("Botswana National Bank");
        Scanner scanner = new Scanner(System.in);

        boolean exit = false;

        while (!exit) {
            System.out.println("\n=== WELCOME TO " + bank.getName().toUpperCase() + " ===");
            System.out.println("1. Login");
            System.out.println("2. Exit");
            System.out.print("Choose an option (1-2): ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    User user = bank.authenticateUser();
                    if (user != null) {
                        System.out.println("\nLogin successful! Welcome, " + user.getDisplayName());

                        if (user.getRole() == UserRole.TELLER) {
                            bank.showTellerDashboard((BankTeller) user);
                        } else {
                            bank.showCustomerDashboard((BankCustomer) user);
                        }
                    } else {
                        System.out.println("Invalid username or password. Please try again.");
                    }
                    break;
                case 2:
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