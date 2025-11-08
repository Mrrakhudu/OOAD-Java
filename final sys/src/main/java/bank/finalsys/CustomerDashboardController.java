package bank.finalsys;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.Modality;

public class CustomerDashboardController {
    @FXML private Label welcomeLabel;
    @FXML private TextField menuChoiceField;
    @FXML private VBox contentArea;
    @FXML private Label messageLabel;
    @FXML private Button proceedButton;

    private BankService bankService;
    private BankCustomer currentUser;
    private Stage primaryStage;

    public CustomerDashboardController() {
        this.bankService = new BankService();
    }

    public void setCurrentUser(BankCustomer user) {
        this.currentUser = user;
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + user.getFullName());
        }
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    public void initialize() {
        if (currentUser != null && welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getFullName());
        }

        // Only allow numbers 1-7 in menu choice field
        menuChoiceField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("[1-7]?")) {
                menuChoiceField.setText(oldVal);
            }
        });

        // Enable/disable proceed button based on input
        menuChoiceField.textProperty().addListener((obs, oldVal, newVal) -> {
            proceedButton.setDisable(newVal == null || newVal.isEmpty());
        });
    }

    @FXML
    private void handleProceed() {
        String choice = menuChoiceField.getText();
        if (choice == null || choice.isEmpty()) {
            messageLabel.setText("Please enter a menu choice (1-7)");
            return;
        }

        messageLabel.setText("");
        contentArea.getChildren().clear();

        switch (choice) {
            case "1":
                showViewAllAccountsPopup();
                break;
            case "2":
                showDepositPopup();
                break;
            case "3":
                showWithdrawPopup();
                break;
            case "4":
                showAccountDetailsPopup();
                break;
            case "5":
                showTransactionHistoryPopup();
                break;
            case "6":
                showCustomerInfoPopup();
                break;
            case "7":
                logout();
                break;
            default:
                messageLabel.setText("Please enter a valid option (1-7)");
        }

        // Clear the menu choice field after processing
        menuChoiceField.clear();
        proceedButton.setDisable(true);
    }

    private void showViewAllAccountsPopup() {
        try {
            Stage accountsStage = new Stage();
            accountsStage.setTitle("My Accounts - Botswana National Bank");

            // Create a simple view for accounts
            VBox root = new VBox(15);
            root.setPadding(new javafx.geometry.Insets(20));

            Label title = new Label("MY ACCOUNTS");
            title.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2E8B57;");

            TextArea accountsArea = new TextArea();
            accountsArea.setEditable(false);
            accountsArea.setPrefRowCount(15);
            accountsArea.setPrefWidth(500);
            accountsArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12;");

            // Load account data
            StringBuilder sb = new StringBuilder();
            if (currentUser.getAccounts().isEmpty()) {
                sb.append("No accounts found.\n\nPlease visit a branch to open an account.");
            } else {
                sb.append("Total Accounts: ").append(currentUser.getAccounts().size()).append("\n\n");
                currentUser.getAccounts().forEach(account -> {
                    sb.append("════════════════════════════════════════\n");
                    sb.append(account.getAccountDetails()).append("\n\n");
                });
            }
            accountsArea.setText(sb.toString());

            Button closeButton = new Button("Close");
            closeButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
            closeButton.setOnAction(e -> accountsStage.close());

            root.getChildren().addAll(title, accountsArea, closeButton);

            Scene scene = new Scene(root, 600, 500);
            accountsStage.setScene(scene);
            accountsStage.initModality(Modality.WINDOW_MODAL);
            accountsStage.initOwner(primaryStage);
            accountsStage.setX(primaryStage.getX() + 100);
            accountsStage.setY(primaryStage.getY() + 50);

            accountsStage.showAndWait();

        } catch (Exception e) {
            messageLabel.setText("Error loading accounts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showDepositPopup() {
        try {
            Stage depositStage = new Stage();
            depositStage.setTitle("Deposit Funds - Botswana National Bank");

            VBox root = new VBox(15);
            root.setPadding(new javafx.geometry.Insets(20));

            Label title = new Label("DEPOSIT FUNDS");
            title.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2E8B57;");

            Label instruction = new Label("Enter deposit details below:");
            instruction.setStyle("-fx-font-weight: bold;");

            ComboBox<String> accountComboBox = new ComboBox<>();
            TextField amountField = new TextField();
            Label messageLabel = new Label();
            messageLabel.setStyle("-fx-font-weight: bold; -fx-wrap-text: true;");
            messageLabel.setPrefHeight(60);

            // Populate account combo box
            if (currentUser.getAccounts().isEmpty()) {
                accountComboBox.setPromptText("No accounts available");
                accountComboBox.setDisable(true);
            } else {
                currentUser.getAccounts().forEach(account -> {
                    accountComboBox.getItems().add(account.getAccountNumber() + " - " + account.getAccountType() +
                            " (BWP " + String.format("%.2f", account.getBalance()) + ")");
                });
            }

            HBox accountBox = new HBox(10);
            accountBox.getChildren().addAll(new Label("Select Account:"), accountComboBox);

            HBox amountBox = new HBox(10);
            amountBox.getChildren().addAll(new Label("Amount (BWP):"), amountField);

            HBox buttonBox = new HBox(15);
            Button depositButton = new Button("Deposit");
            depositButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");

            Button closeButton = new Button("Close");
            closeButton.setStyle("-fx-background-color: #666; -fx-text-fill: white;");

            depositButton.setOnAction(e -> {
                try {
                    if (accountComboBox.getValue() == null) {
                        messageLabel.setText("Error: Please select an account");
                        messageLabel.setStyle("-fx-text-fill: red;");
                        return;
                    }

                    if (amountField.getText().isEmpty()) {
                        messageLabel.setText("Error: Please enter an amount");
                        messageLabel.setStyle("-fx-text-fill: red;");
                        return;
                    }

                    double amount = Double.parseDouble(amountField.getText());
                    if (amount <= 0) {
                        messageLabel.setText("Error: Amount must be positive");
                        messageLabel.setStyle("-fx-text-fill: red;");
                        return;
                    }

                    // Extract account number from combo box (format: "ACC1001 - SAVINGS (BWP 1000.00)")
                    String accountNumber = accountComboBox.getValue().split(" - ")[0];

                    bankService.deposit(accountNumber, amount);

                    messageLabel.setText("✓ Deposit successful!\nAmount: BWP " + String.format("%.2f", amount) +
                            "\nNew balance will be updated.");
                    messageLabel.setStyle("-fx-text-fill: green;");

                    // Clear fields
                    amountField.clear();

                } catch (NumberFormatException ex) {
                    messageLabel.setText("Error: Please enter a valid amount");
                    messageLabel.setStyle("-fx-text-fill: red;");
                } catch (Exception ex) {
                    messageLabel.setText("Error: " + ex.getMessage());
                    messageLabel.setStyle("-fx-text-fill: red;");
                }
            });

            closeButton.setOnAction(e -> depositStage.close());

            buttonBox.getChildren().addAll(depositButton, closeButton);
            root.getChildren().addAll(title, instruction, accountBox, amountBox, buttonBox, messageLabel);

            Scene scene = new Scene(root, 500, 350);
            depositStage.setScene(scene);
            depositStage.initModality(Modality.WINDOW_MODAL);
            depositStage.initOwner(primaryStage);
            depositStage.setX(primaryStage.getX() + 150);
            depositStage.setY(primaryStage.getY() + 100);

            depositStage.showAndWait();

        } catch (Exception e) {
            messageLabel.setText("Error opening deposit window: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showWithdrawPopup() {
        try {
            Stage withdrawStage = new Stage();
            withdrawStage.setTitle("Withdraw Funds - Botswana National Bank");

            VBox root = new VBox(15);
            root.setPadding(new javafx.geometry.Insets(20));

            Label title = new Label("WITHDRAW FUNDS");
            title.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2E8B57;");

            Label instruction = new Label("Enter withdrawal details below:");
            instruction.setStyle("-fx-font-weight: bold;");

            ComboBox<String> accountComboBox = new ComboBox<>();
            TextField amountField = new TextField();
            Label messageLabel = new Label();
            messageLabel.setStyle("-fx-font-weight: bold; -fx-wrap-text: true;");
            messageLabel.setPrefHeight(60);

            // Populate account combo box with withdrawable accounts
            if (currentUser.getAccounts().isEmpty()) {
                accountComboBox.setPromptText("No accounts available");
                accountComboBox.setDisable(true);
            } else {
                currentUser.getAccounts().forEach(account -> {
                    // Only show accounts that allow withdrawals (not Savings)
                    if (!(account instanceof SavingsAccount)) {
                        accountComboBox.getItems().add(account.getAccountNumber() + " - " + account.getAccountType() +
                                " (BWP " + String.format("%.2f", account.getBalance()) + ")");
                    }
                });

                if (accountComboBox.getItems().isEmpty()) {
                    accountComboBox.setPromptText("No withdrawable accounts");
                    accountComboBox.setDisable(true);
                }
            }

            HBox accountBox = new HBox(10);
            accountBox.getChildren().addAll(new Label("Select Account:"), accountComboBox);

            HBox amountBox = new HBox(10);
            amountBox.getChildren().addAll(new Label("Amount (BWP):"), amountField);

            HBox buttonBox = new HBox(15);
            Button withdrawButton = new Button("Withdraw");
            withdrawButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold;");

            Button closeButton = new Button("Close");
            closeButton.setStyle("-fx-background-color: #666; -fx-text-fill: white;");

            withdrawButton.setOnAction(e -> {
                try {
                    if (accountComboBox.getValue() == null) {
                        messageLabel.setText("Error: Please select an account");
                        messageLabel.setStyle("-fx-text-fill: red;");
                        return;
                    }

                    if (amountField.getText().isEmpty()) {
                        messageLabel.setText("Error: Please enter an amount");
                        messageLabel.setStyle("-fx-text-fill: red;");
                        return;
                    }

                    double amount = Double.parseDouble(amountField.getText());
                    if (amount <= 0) {
                        messageLabel.setText("Error: Amount must be positive");
                        messageLabel.setStyle("-fx-text-fill: red;");
                        return;
                    }

                    // Extract account number from combo box
                    String accountNumber = accountComboBox.getValue().split(" - ")[0];

                    bankService.withdraw(accountNumber, amount);

                    messageLabel.setText("✓ Withdrawal successful!\nAmount: BWP " + String.format("%.2f", amount) +
                            "\nNew balance will be updated.");
                    messageLabel.setStyle("-fx-text-fill: green;");

                    // Clear fields
                    amountField.clear();

                } catch (NumberFormatException ex) {
                    messageLabel.setText("Error: Please enter a valid amount");
                    messageLabel.setStyle("-fx-text-fill: red;");
                } catch (Exception ex) {
                    messageLabel.setText("Error: " + ex.getMessage());
                    messageLabel.setStyle("-fx-text-fill: red;");
                }
            });

            closeButton.setOnAction(e -> withdrawStage.close());

            buttonBox.getChildren().addAll(withdrawButton, closeButton);
            root.getChildren().addAll(title, instruction, accountBox, amountBox, buttonBox, messageLabel);

            Scene scene = new Scene(root, 500, 350);
            withdrawStage.setScene(scene);
            withdrawStage.initModality(Modality.WINDOW_MODAL);
            withdrawStage.initOwner(primaryStage);
            withdrawStage.setX(primaryStage.getX() + 150);
            withdrawStage.setY(primaryStage.getY() + 100);

            withdrawStage.showAndWait();

        } catch (Exception e) {
            messageLabel.setText("Error opening withdrawal window: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAccountDetailsPopup() {
        try {
            Stage detailsStage = new Stage();
            detailsStage.setTitle("Account Details - Botswana National Bank");

            VBox root = new VBox(15);
            root.setPadding(new javafx.geometry.Insets(20));

            Label title = new Label("ACCOUNT DETAILS");
            title.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2E8B57;");

            ComboBox<String> accountComboBox = new ComboBox<>();
            TextArea detailsArea = new TextArea();
            detailsArea.setEditable(false);
            detailsArea.setPrefRowCount(10);
            detailsArea.setPrefWidth(500);
            detailsArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12;");

            // Populate account combo box
            if (currentUser.getAccounts().isEmpty()) {
                accountComboBox.setPromptText("No accounts available");
                accountComboBox.setDisable(true);
                detailsArea.setText("No accounts found.");
            } else {
                currentUser.getAccounts().forEach(account -> {
                    accountComboBox.getItems().add(account.getAccountNumber() + " - " + account.getAccountType());
                });

                // Show first account details by default
                if (!currentUser.getAccounts().isEmpty()) {
                    accountComboBox.setValue(accountComboBox.getItems().get(0));
                    Account firstAccount = currentUser.getAccounts().get(0);
                    detailsArea.setText(firstAccount.getAccountDetails());
                }
            }

            // Add listener to update details when account selection changes
            accountComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    String accountNumber = newVal.split(" - ")[0];
                    Account account = bankService.findAccount(accountNumber);
                    if (account != null) {
                        detailsArea.setText(account.getAccountDetails());
                    }
                }
            });

            HBox accountBox = new HBox(10);
            accountBox.getChildren().addAll(new Label("Select Account:"), accountComboBox);

            Button closeButton = new Button("Close");
            closeButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
            closeButton.setOnAction(e -> detailsStage.close());

            root.getChildren().addAll(title, accountBox, detailsArea, closeButton);

            Scene scene = new Scene(root, 600, 450);
            detailsStage.setScene(scene);
            detailsStage.initModality(Modality.WINDOW_MODAL);
            detailsStage.initOwner(primaryStage);
            detailsStage.setX(primaryStage.getX() + 100);
            detailsStage.setY(primaryStage.getY() + 75);

            detailsStage.showAndWait();

        } catch (Exception e) {
            messageLabel.setText("Error loading account details: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showTransactionHistoryPopup() {
        try {
            Stage historyStage = new Stage();
            historyStage.setTitle("Transaction History - Botswana National Bank");

            VBox root = new VBox(15);
            root.setPadding(new javafx.geometry.Insets(20));

            Label title = new Label("TRANSACTION HISTORY");
            title.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2E8B57;");

            TextArea historyArea = new TextArea();
            historyArea.setEditable(false);
            historyArea.setPrefRowCount(15);
            historyArea.setPrefWidth(500);
            historyArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12;");

            // For now, show a placeholder message
            String historyText = "Transaction History Feature\n" +
                    "════════════════════════════\n\n" +
                    "This feature will be implemented in the next phase.\n\n" +
                    "It will include:\n" +
                    "• All deposits and withdrawals\n" +
                    "• Interest payments\n" +
                    "• Date and time stamps\n" +
                    "• Running balances\n" +
                    "• Transaction IDs\n\n" +
                    "Coming soon...";

            historyArea.setText(historyText);

            Button closeButton = new Button("Close");
            closeButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
            closeButton.setOnAction(e -> historyStage.close());

            root.getChildren().addAll(title, historyArea, closeButton);

            Scene scene = new Scene(root, 600, 500);
            historyStage.setScene(scene);
            historyStage.initModality(Modality.WINDOW_MODAL);
            historyStage.initOwner(primaryStage);
            historyStage.setX(primaryStage.getX() + 100);
            historyStage.setY(primaryStage.getY() + 50);

            historyStage.showAndWait();

        } catch (Exception e) {
            messageLabel.setText("Error loading transaction history: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showCustomerInfoPopup() {
        try {
            Stage infoStage = new Stage();
            infoStage.setTitle("My Information - Botswana National Bank");

            VBox root = new VBox(15);
            root.setPadding(new javafx.geometry.Insets(20));

            Label title = new Label("MY INFORMATION");
            title.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2E8B57;");

            TextArea infoArea = new TextArea();
            infoArea.setEditable(false);
            infoArea.setPrefRowCount(12);
            infoArea.setPrefWidth(500);
            infoArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12;");

            // Build customer information
            StringBuilder sb = new StringBuilder();
            sb.append("CUSTOMER PROFILE\n");
            sb.append("════════════════════════════\n\n");
            sb.append("Full Name: ").append(currentUser.getFullName()).append("\n");
            sb.append("Customer ID: ").append(currentUser.getCustomerId()).append("\n");
            sb.append("Username: ").append(currentUser.getUsername()).append("\n");
            sb.append("Address: ").append(currentUser.getAddress()).append("\n\n");
            sb.append("Account Summary:\n");
            sb.append("────────────────\n");
            sb.append("Total Accounts: ").append(currentUser.getAccounts().size()).append("\n");

            if (!currentUser.getAccounts().isEmpty()) {
                double totalBalance = currentUser.getAccounts().stream()
                        .mapToDouble(Account::getBalance)
                        .sum();
                sb.append("Total Balance: BWP ").append(String.format("%.2f", totalBalance)).append("\n\n");

                sb.append("Accounts Breakdown:\n");
                currentUser.getAccounts().forEach(account -> {
                    sb.append("• ").append(account.getAccountType())
                            .append(": BWP ").append(String.format("%.2f", account.getBalance()))
                            .append(" (").append(account.getAccountNumber()).append(")\n");
                });
            } else {
                sb.append("No accounts opened yet.\n");
            }

            infoArea.setText(sb.toString());

            Button closeButton = new Button("Close");
            closeButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
            closeButton.setOnAction(e -> infoStage.close());

            root.getChildren().addAll(title, infoArea, closeButton);

            Scene scene = new Scene(root, 550, 500);
            infoStage.setScene(scene);
            infoStage.initModality(Modality.WINDOW_MODAL);
            infoStage.initOwner(primaryStage);
            infoStage.setX(primaryStage.getX() + 125);
            infoStage.setY(primaryStage.getY() + 50);

            infoStage.showAndWait();

        } catch (Exception e) {
            messageLabel.setText("Error loading customer information: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void logout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("LoginView.fxml"));
            Parent root = loader.load();

            LoginController loginController = loader.getController();
            loginController.setPrimaryStage(primaryStage);

            Scene scene = new Scene(root, 800, 600);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Botswana National Bank - Login");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HBox createFormField(String label, Control field) {
        HBox hbox = new HBox(10);
        Label fieldLabel = new Label(label);
        fieldLabel.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12;");
        field.setStyle("-fx-font-family: 'Courier New'; -fx-pref-width: 200;");
        hbox.getChildren().addAll(fieldLabel, field);
        return hbox;
    }

    private void clearForm(VBox form) {
        form.getChildren().stream()
                .filter(node -> node instanceof HBox)
                .map(node -> (HBox) node)
                .forEach(hbox -> {
                    hbox.getChildren().stream()
                            .filter(node -> node instanceof TextInputControl)
                            .map(node -> (TextInputControl) node)
                            .forEach(TextInputControl::clear);
                });
    }
}