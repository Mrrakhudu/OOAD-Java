package bank.finalsys;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AccountOpeningController {
    @FXML private RadioButton individualRadio;
    @FXML private RadioButton businessRadio;
    @FXML private ComboBox<String> accountTypeComboBox;
    @FXML private TextField initialDepositField;
    @FXML private Label minDepositLabel;
    @FXML private VBox employerInfoSection;
    @FXML private TextArea employerInfoTextArea;
    @FXML private VBox businessInfoSection;
    @FXML private TextField businessRegistrationField;
    @FXML private TextField businessAddressField;
    @FXML private TextField contactPersonField;
    @FXML private Label messageLabel;
    @FXML private Label selectedCustomerLabel;

    private BankService bankService;
    private String currentCustomerUsername;
    private Stage accountStage;

    // Minimum deposit constants
    private static final double MIN_SAVINGS_DEPOSIT = 50.0;
    private static final double MIN_INVESTMENT_DEPOSIT = 500.0;

    // REMOVE THIS CONSTRUCTOR:
    // public AccountOpeningController() {
    //     this.bankService = new BankService();
    // }

    // ADD THESE SETTER METHODS:
    public void setBankService(BankService bankService) {
        this.bankService = bankService;
        // Update customer display if customer was already set
        if (currentCustomerUsername != null) {
            updateSelectedCustomerDisplay();
        }
    }

    public void setCurrentCustomer(String username) {
        this.currentCustomerUsername = username;
        updateSelectedCustomerDisplay();
    }

    public void setAccountStage(Stage accountStage) {
        this.accountStage = accountStage;
    }

    @FXML
    public void initialize() {
        // Set up radio button group
        ToggleGroup customerTypeGroup = new ToggleGroup();
        individualRadio.setToggleGroup(customerTypeGroup);
        businessRadio.setToggleGroup(customerTypeGroup);
        individualRadio.setSelected(true);

        // Set up account type combo box
        accountTypeComboBox.getItems().addAll("Savings", "Investment", "Cheque");
        accountTypeComboBox.setValue("Savings");

        // Set up listeners for dynamic UI changes
        setupEventListeners();
        updateMinDepositLabel();
        updateFormVisibility();

        // Initialize selected customer display
        if (selectedCustomerLabel != null) {
            selectedCustomerLabel.setText("No customer selected");
        }
    }

    private void setupEventListeners() {
        // Customer type radio button listeners
        individualRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) updateFormVisibility();
        });

        businessRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) updateFormVisibility();
        });

        // Account type combo box listener
        accountTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateFormVisibility();
            updateMinDepositLabel();
        });

        // Validate deposit field to only allow numbers and decimal point
        initialDepositField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d*)?")) {
                initialDepositField.setText(oldVal);
            }
        });
    }

    private void updateFormVisibility() {
        boolean isIndividual = individualRadio.isSelected();
        boolean isBusiness = businessRadio.isSelected();
        String accountType = accountTypeComboBox.getValue();

        // Show employer info only for individual customers opening cheque accounts
        boolean showEmployerInfo = isIndividual && "Cheque".equals(accountType);
        employerInfoSection.setVisible(showEmployerInfo);
        employerInfoSection.setManaged(showEmployerInfo);

        // Show business info only for business customers
        businessInfoSection.setVisible(isBusiness);
        businessInfoSection.setManaged(isBusiness);
    }

    private void updateMinDepositLabel() {
        String accountType = accountTypeComboBox.getValue();
        if ("Savings".equals(accountType)) {
            minDepositLabel.setText("* Minimum deposit: BWP " + MIN_SAVINGS_DEPOSIT);
            minDepositLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
        } else if ("Investment".equals(accountType)) {
            minDepositLabel.setText("* Minimum deposit: BWP " + MIN_INVESTMENT_DEPOSIT);
            minDepositLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
        } else {
            minDepositLabel.setText("* No minimum deposit required");
            minDepositLabel.setStyle("-fx-text-fill: #666;");
        }
    }

    private void updateSelectedCustomerDisplay() {
        if (currentCustomerUsername != null && selectedCustomerLabel != null && bankService != null) {
            BankCustomer customer = bankService.findCustomerByUsername(currentCustomerUsername);
            if (customer != null) {
                selectedCustomerLabel.setText("Opening account for: " + customer.getFullName() + " (" + customer.getCustomerId() + ")");
                selectedCustomerLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            } else {
                selectedCustomerLabel.setText("Customer not found: " + currentCustomerUsername);
                selectedCustomerLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            }
        }
    }

    @FXML
    private void handleSubmit() {
        messageLabel.setText("");

        // Check if bankService is available
        if (bankService == null) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Error: System not properly initialized. Please close and try again.");
            return;
        }

        // Validate that a customer is selected
        if (currentCustomerUsername == null) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Error: No customer selected.");
            return;
        }

        // Validate all inputs
        if (!validateInputs()) {
            return;
        }

        try {
            String accountType = accountTypeComboBox.getValue();
            double initialDeposit = Double.parseDouble(initialDepositField.getText());
            String branch = "Main Branch";

            String employer = "";
            String companyAddress = "";

            // Collect additional information based on account type and customer type
            if (individualRadio.isSelected() && "Cheque".equals(accountType)) {
                employer = employerInfoTextArea.getText().trim();
                if (employer.isEmpty()) {
                    messageLabel.setStyle("-fx-text-fill: red;");
                    messageLabel.setText("Error: Employer information is required for individual cheque accounts.");
                    return;
                }
            } else if (businessRadio.isSelected()) {
                companyAddress = businessAddressField.getText().trim();
            }

            // Open the account
            Account newAccount = bankService.openAccount(
                    currentCustomerUsername,
                    accountType.toUpperCase(),
                    initialDeposit,
                    branch,
                    employer,
                    companyAddress
            );

            // Success message
            messageLabel.setStyle("-fx-text-fill: green;");
            String successMessage = String.format(
                    "✓ Account opened successfully!\n\n" +
                            "Account Details:\n" +
                            "• Account Number: %s\n" +
                            "• Account Type: %s\n" +
                            "• Initial Deposit: BWP %.2f\n" +
                            "• Customer: %s\n\n" +
                            "Click 'Done' to return to main menu.",
                    newAccount.getAccountNumber(),
                    newAccount.getAccountType(),
                    initialDeposit,
                    newAccount.getCustomer().getFullName()
            );
            messageLabel.setText(successMessage);

        } catch (Exception e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDone() {
        // Close the popup window and return to teller dashboard
        if (accountStage != null) {
            accountStage.close();
        }
    }

    @FXML
    private void handleClear() {
        clearForm();
        messageLabel.setText("");
    }

    private boolean validateInputs() {
        // Validate account type
        if (accountTypeComboBox.getValue() == null) {
            messageLabel.setText("Error: Please select an account type");
            return false;
        }

        // Validate initial deposit
        if (initialDepositField.getText().isEmpty()) {
            messageLabel.setText("Error: Please enter an initial deposit amount");
            return false;
        }

        try {
            double deposit = Double.parseDouble(initialDepositField.getText());

            // Validate positive amount
            if (deposit <= 0) {
                messageLabel.setText("Error: Initial deposit must be positive");
                return false;
            }

            // Validate minimum deposits for specific account types
            String accountType = accountTypeComboBox.getValue();
            if ("Savings".equals(accountType) && deposit < MIN_SAVINGS_DEPOSIT) {
                messageLabel.setText("Error: Savings account requires minimum deposit of BWP " + MIN_SAVINGS_DEPOSIT);
                return false;
            }

            if ("Investment".equals(accountType) && deposit < MIN_INVESTMENT_DEPOSIT) {
                messageLabel.setText("Error: Investment account requires minimum deposit of BWP " + MIN_INVESTMENT_DEPOSIT);
                return false;
            }

        } catch (NumberFormatException e) {
            messageLabel.setText("Error: Please enter a valid deposit amount");
            return false;
        }

        // Validate conditional fields for business customers
        if (businessRadio.isSelected()) {
            if (businessRegistrationField.getText().trim().isEmpty()) {
                messageLabel.setText("Error: Business registration number is required for business customers");
                return false;
            }
            if (businessAddressField.getText().trim().isEmpty()) {
                messageLabel.setText("Error: Business address is required for business customers");
                return false;
            }
        }

        return true;
    }

    private void clearForm() {
        // Clear all input fields
        initialDepositField.clear();
        employerInfoTextArea.clear();
        businessRegistrationField.clear();
        businessAddressField.clear();
        contactPersonField.clear();

        // Reset to default values
        accountTypeComboBox.setValue("Savings");
        individualRadio.setSelected(true);

        // Update UI elements
        updateMinDepositLabel();
        updateFormVisibility();
    }
}