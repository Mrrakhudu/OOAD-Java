package holiday.bank;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.layout.HBox;
import javafx.scene.control.TextInputControl;

public class CustomerDashboardController {
    @FXML private Label welcomeLabel;
    @FXML private TextField menuChoiceField;
    @FXML private VBox contentArea;
    @FXML private Label messageLabel;

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

        menuChoiceField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("[1-7]?")) {
                menuChoiceField.setText(oldVal);
            }
        });
    }

    @FXML
    private void handleMenuChoice() {
        String choice = menuChoiceField.getText();
        messageLabel.setText("");
        contentArea.getChildren().clear();

        switch (choice) {
            case "1":
                viewAllAccounts();
                break;
            case "2":
                showDepositForm();
                break;
            case "3":
                showWithdrawForm();
                break;
            case "4":
                showAccountDetailsForm();
                break;
            case "5":
                showTransactionHistory();
                break;
            case "6":
                viewCustomerInfo();
                break;
            case "7":
                logout();
                break;
            default:
                messageLabel.setText("Please enter a valid option (1-7)");
        }
    }

    private void viewAllAccounts() {
        VBox results = new VBox(10);

        Label title = new Label("=== YOUR ACCOUNTS ===");
        title.setStyle("-fx-font-family: 'Courier New'; -fx-font-weight: bold;");

        TextArea accountsArea = new TextArea();
        accountsArea.setEditable(false);
        accountsArea.setPrefRowCount(10);
        accountsArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12;");

        try {
            if (currentUser.getAccounts().isEmpty()) {
                accountsArea.setText("No accounts found.");
            } else {
                StringBuilder sb = new StringBuilder();
                currentUser.getAccounts().forEach(account ->
                        sb.append(account.toString()).append("\n\n")
                );
                accountsArea.setText(sb.toString());
            }
        } catch (Exception e) {
            accountsArea.setText("Error loading accounts: " + e.getMessage());
        }

        results.getChildren().addAll(title, accountsArea);
        contentArea.getChildren().add(results);
    }

    private void showDepositForm() {
        VBox form = new VBox(10);

        Label title = new Label("=== DEPOSIT FUNDS ===");
        title.setStyle("-fx-font-family: 'Courier New'; -fx-font-weight: bold;");

        TextField accountNumberField = new TextField();
        TextField amountField = new TextField();

        form.getChildren().addAll(
                title,
                createFormField("Enter account number:", accountNumberField),
                createFormField("Enter amount to deposit:", amountField)
        );

        Button submitButton = new Button("Deposit");
        submitButton.setStyle("-fx-font-family: 'Courier New'; -fx-background-color: #4CAF50; -fx-text-fill: white;");
        submitButton.setOnAction(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText());
                bankService.deposit(accountNumberField.getText(), amount);
                messageLabel.setText("Deposit successful!");
                clearForm(form);
            } catch (Exception ex) {
                messageLabel.setText("Error: " + ex.getMessage());
            }
        });

        form.getChildren().add(submitButton);
        contentArea.getChildren().add(form);
    }

    private void showWithdrawForm() {
        VBox form = new VBox(10);

        Label title = new Label("=== WITHDRAW FUNDS ===");
        title.setStyle("-fx-font-family: 'Courier New'; -fx-font-weight: bold;");

        TextField accountNumberField = new TextField();
        TextField amountField = new TextField();

        form.getChildren().addAll(
                title,
                createFormField("Enter account number:", accountNumberField),
                createFormField("Enter amount to withdraw:", amountField)
        );

        Button submitButton = new Button("Withdraw");
        submitButton.setStyle("-fx-font-family: 'Courier New'; -fx-background-color: #FF9800; -fx-text-fill: white;");
        submitButton.setOnAction(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText());
                bankService.withdraw(accountNumberField.getText(), amount);
                messageLabel.setText("Withdrawal successful!");
                clearForm(form);
            } catch (Exception ex) {
                messageLabel.setText("Error: " + ex.getMessage());
            }
        });

        form.getChildren().add(submitButton);
        contentArea.getChildren().add(form);
    }

    private void showAccountDetailsForm() {
        VBox form = new VBox(10);

        Label title = new Label("=== VIEW ACCOUNT DETAILS ===");
        title.setStyle("-fx-font-family: 'Courier New'; -fx-font-weight: bold;");

        TextField accountNumberField = new TextField();
        TextArea resultsArea = new TextArea();
        resultsArea.setEditable(false);
        resultsArea.setPrefRowCount(5);
        resultsArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12;");

        form.getChildren().addAll(
                title,
                createFormField("Enter account number:", accountNumberField),
                resultsArea
        );

        Button viewButton = new Button("View Details");
        viewButton.setStyle("-fx-font-family: 'Courier New'; -fx-background-color: #2196F3; -fx-text-fill: white;");
        viewButton.setOnAction(e -> {
            try {
                var account = bankService.findAccount(accountNumberField.getText());
                if (account != null && account.getCustomer().getCustomerId().equals(currentUser.getCustomerId())) {
                    resultsArea.setText(account.getAccountDetails());
                } else {
                    resultsArea.setText("Account not found or doesn't belong to you.");
                }
            } catch (Exception ex) {
                resultsArea.setText("Error: " + ex.getMessage());
            }
        });

        form.getChildren().add(viewButton);
        contentArea.getChildren().add(form);
    }

    private void showTransactionHistory() {
        VBox results = new VBox(10);

        Label title = new Label("=== TRANSACTION HISTORY ===");
        title.setStyle("-fx-font-family: 'Courier New'; -fx-font-weight: bold;");

        TextArea historyArea = new TextArea();
        historyArea.setEditable(false);
        historyArea.setPrefRowCount(10);
        historyArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12;");
        historyArea.setText("Transaction history feature will be implemented in the next phase.\nThis will show all deposits, withdrawals, and interest payments.");

        results.getChildren().addAll(title, historyArea);
        contentArea.getChildren().add(results);
    }

    private void viewCustomerInfo() {
        VBox results = new VBox(10);

        Label title = new Label("=== YOUR INFORMATION ===");
        title.setStyle("-fx-font-family: 'Courier New'; -fx-font-weight: bold;");

        TextArea infoArea = new TextArea();
        infoArea.setEditable(false);
        infoArea.setPrefRowCount(8);
        infoArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12;");
        infoArea.setText(currentUser.toString());

        results.getChildren().addAll(title, infoArea);
        contentArea.getChildren().add(results);
    }

    private void logout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/view/LoginView.fxml"));
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