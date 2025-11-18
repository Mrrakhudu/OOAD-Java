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
import java.util.List;

public class TellerDashboardController {
    @FXML private Label welcomeLabel;
    @FXML private TextField menuChoiceField;
    @FXML private VBox contentArea;
    @FXML private Label messageLabel;
    @FXML private Button proceedButton;

    private BankService bankService;
    private User currentUser;
    private Stage primaryStage;

    public TellerDashboardController() {
        this.bankService = new BankService();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + user.getDisplayName());
        }
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    public void initialize() {
        if (currentUser != null && welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getDisplayName());
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
                showCreateCustomerForm();
                break;
            case "2":
                showModifyCustomerForm();
                break;
            case "3":
                showViewCustomerAccountsForm();
                break;
            case "4":
                showCustomerSelectionForAccount();
                break;
            case "5":
                processMonthlyInterest();
                break;
            case "6":
                showAllCustomers();
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

    private void showCreateCustomerForm() {
        VBox form = new VBox(10);
        Label title = new Label("=== CREATE NEW CUSTOMER ===");
        title.setStyle("-fx-font-family: 'Courier New'; -fx-font-weight: bold;");

        TextField firstNameField = new TextField();
        TextField surnameField = new TextField();
        TextField addressField = new TextField();
        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();

        form.getChildren().addAll(
                title,
                createFormField("Enter first name:", firstNameField),
                createFormField("Enter surname:", surnameField),
                createFormField("Enter address:", addressField),
                createFormField("Choose username:", usernameField),
                createFormField("Choose password:", passwordField)
        );

        Button submitButton = new Button("Create Customer");
        submitButton.setStyle("-fx-font-family: 'Courier New'; -fx-background-color: #4CAF50; -fx-text-fill: white;");
        submitButton.setOnAction(e -> {
            try {
                if (firstNameField.getText().isEmpty() || surnameField.getText().isEmpty() ||
                        usernameField.getText().isEmpty() || passwordField.getText().isEmpty()) {
                    messageLabel.setText("Error: All fields are required");
                    return;
                }

                BankCustomer customer = bankService.createCustomer(
                        usernameField.getText(),
                        passwordField.getText(),
                        firstNameField.getText(),
                        surnameField.getText(),
                        addressField.getText()
                );
                messageLabel.setText("Customer created successfully! Customer ID: " + customer.getCustomerId());
                clearForm(form);
            } catch (Exception ex) {
                messageLabel.setText("Error: " + ex.getMessage());
            }
        });

        form.getChildren().add(submitButton);
        contentArea.getChildren().add(form);
    }

    private void showModifyCustomerForm() {
        VBox form = new VBox(10);
        Label title = new Label("=== MODIFY CUSTOMER DATA ===");
        title.setStyle("-fx-font-family: 'Courier New'; -fx-font-weight: bold;");

        TextField usernameField = new TextField();
        TextField firstNameField = new TextField();
        TextField surnameField = new TextField();
        TextField addressField = new TextField();

        form.getChildren().addAll(
                title,
                createFormField("Enter customer username:", usernameField),
                createFormField("New first name (optional):", firstNameField),
                createFormField("New surname (optional):", surnameField),
                createFormField("New address (optional):", addressField)
        );

        Button submitButton = new Button("Update Customer");
        submitButton.setStyle("-fx-font-family: 'Courier New'; -fx-background-color: #2196F3; -fx-text-fill: white;");
        submitButton.setOnAction(e -> {
            try {
                if (usernameField.getText().isEmpty()) {
                    messageLabel.setText("Error: Customer username is required");
                    return;
                }

                bankService.updateCustomer(
                        usernameField.getText(),
                        firstNameField.getText(),
                        surnameField.getText(),
                        addressField.getText()
                );
                messageLabel.setText("Customer data updated successfully!");
                clearForm(form);
            } catch (Exception ex) {
                messageLabel.setText("Error: " + ex.getMessage());
            }
        });

        form.getChildren().add(submitButton);
        contentArea.getChildren().add(form);
    }

    private void showViewCustomerAccountsForm() {
        VBox form = new VBox(10);
        Label title = new Label("=== VIEW CUSTOMER ACCOUNTS ===");
        title.setStyle("-fx-font-family: 'Courier New'; -fx-font-weight: bold;");

        TextField usernameField = new TextField();
        TextArea resultsArea = new TextArea();
        resultsArea.setEditable(false);
        resultsArea.setPrefRowCount(10);
        resultsArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12;");

        form.getChildren().addAll(
                title,
                createFormField("Enter customer username:", usernameField),
                resultsArea
        );

        Button viewButton = new Button("View Accounts");
        viewButton.setStyle("-fx-font-family: 'Courier New'; -fx-background-color: #FF9800; -fx-text-fill: white;");
        viewButton.setOnAction(e -> {
            try {
                if (usernameField.getText().isEmpty()) {
                    resultsArea.setText("Please enter a customer username");
                    return;
                }

                BankCustomer customer = bankService.findCustomerByUsername(usernameField.getText());
                if (customer != null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Customer: ").append(customer.getFullName()).append("\n");
                    sb.append("Customer ID: ").append(customer.getCustomerId()).append("\n");
                    sb.append("Address: ").append(customer.getAddress()).append("\n\n");

                    if (customer.getAccounts().isEmpty()) {
                        sb.append("No accounts found for this customer.");
                    } else {
                        sb.append("Accounts:\n");
                        sb.append("----------------------------------------\n");
                        customer.getAccounts().forEach(account ->
                                sb.append(account.getAccountDetails()).append("\n\n")
                        );
                    }
                    resultsArea.setText(sb.toString());
                } else {
                    resultsArea.setText("Customer not found.");
                }
            } catch (Exception ex) {
                resultsArea.setText("Error: " + ex.getMessage());
            }
        });

        form.getChildren().add(viewButton);
        contentArea.getChildren().add(form);
    }

    private void showCustomerSelectionForAccount() {
        VBox form = new VBox(10);
        Label title = new Label("=== SELECT CUSTOMER FOR ACCOUNT OPENING ===");
        title.setStyle("-fx-font-family: 'Courier New'; -fx-font-weight: bold;");

        // Show all customers for selection
        List<BankCustomer> customers = bankService.getAllCustomers();

        if (customers.isEmpty()) {
            Label noCustomersLabel = new Label("No customers found. Please create a customer first.");
            noCustomersLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            form.getChildren().addAll(title, noCustomersLabel);
        } else {
            Label instruction = new Label("Select a customer from the list below:");
            instruction.setStyle("-fx-font-weight: bold;");

            ComboBox<String> customerComboBox = new ComboBox<>();
            for (BankCustomer customer : customers) {
                customerComboBox.getItems().add(customer.getUsername() + " - " + customer.getFullName());
            }

            Button selectButton = new Button("Open Account for Selected Customer");
            selectButton.setStyle("-fx-font-family: 'Courier New'; -fx-background-color: #4CAF50; -fx-text-fill: white;");

            selectButton.setOnAction(e -> {
                String selected = customerComboBox.getValue();
                if (selected != null) {
                    // Extract username from the selection (format: "username - Full Name")
                    String selectedCustomerUsername = selected.split(" - ")[0];
                    openAccountPopup(selectedCustomerUsername);
                } else {
                    messageLabel.setText("Please select a customer first.");
                }
            });

            form.getChildren().addAll(title, instruction, customerComboBox, selectButton);
        }

        contentArea.getChildren().add(form);
    }

    private void openAccountPopup(String customerUsername) {
        try {
            Stage accountStage = new Stage();
            accountStage.setTitle("Open New Account - Botswana National Bank");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("AccountOpeningView.fxml"));
            Parent root = loader.load();

            AccountOpeningController controller = loader.getController();
            controller.setCurrentCustomer(customerUsername);
            controller.setAccountStage(accountStage);
            controller.setBankService(this.bankService); // ← THIS IS THE KEY LINE

            Scene scene = new Scene(root, 600, 700);
            accountStage.setScene(scene);
            accountStage.initModality(Modality.WINDOW_MODAL);
            accountStage.initOwner(primaryStage);

            accountStage.showAndWait();
            messageLabel.setText("Account opening completed.");

        } catch (Exception e) {
            messageLabel.setText("Error opening account window: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processMonthlyInterest() {
        try {
            int affectedAccounts = 0;
            for (Account account : bankService.getAllAccounts()) {
                if (account instanceof InterestBearing) {
                    affectedAccounts++;
                }
            }

            bankService.processMonthlyInterest();
            messageLabel.setText("Monthly interest processed successfully for " + affectedAccounts + " interest-bearing accounts!");
        } catch (Exception e) {
            messageLabel.setText("Error processing interest: " + e.getMessage());
        }
    }

    private void showAllCustomers() {
        VBox results = new VBox(10);
        Label title = new Label("=== ALL CUSTOMERS ===");
        title.setStyle("-fx-font-family: 'Courier New'; -fx-font-weight: bold;");

        TextArea customersArea = new TextArea();
        customersArea.setEditable(false);
        customersArea.setPrefRowCount(15);
        customersArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12;");

        try {
            List<BankCustomer> customers = bankService.getAllCustomers();
            StringBuilder sb = new StringBuilder();

            if (customers.isEmpty()) {
                sb.append("No customers found in the system.");
            } else {
                sb.append("Total Customers: ").append(customers.size()).append("\n\n");
                for (BankCustomer customer : customers) {
                    sb.append(customer.toString()).append("\n");
                    if (!customer.getAccounts().isEmpty()) {
                        sb.append("  Accounts:\n");
                        customer.getAccounts().forEach(account ->
                                sb.append("    - ").append(account.getAccountDetails()).append("\n")
                        );
                    } else {
                        sb.append("  No accounts\n");
                    }
                    sb.append("\n");
                }
            }
            customersArea.setText(sb.toString());
        } catch (Exception e) {
            customersArea.setText("Error loading customers: " + e.getMessage());
        }

        results.getChildren().addAll(title, customersArea);
        contentArea.getChildren().add(results);
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
            primaryStage.close();
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