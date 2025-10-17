package holiday.bank;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.control.ToggleGroup;

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

    private BankService bankService;
    private String currentCustomerUsername;

    // Minimum deposit constants
    private static final double MIN_SAVINGS_DEPOSIT = 50.0;
    private static final double MIN_INVESTMENT_DEPOSIT = 500.0;

    public AccountOpeningController() {
        this.bankService = new BankService();
    }

    public void setCurrentCustomer(String username) {
        this.currentCustomerUsername = username;
    }

    @FXML
    public void initialize() {
        // Set up radio button group
        ToggleGroup customerTypeGroup = new ToggleGroup();
        individualRadio.setToggleGroup(customerTypeGroup);
        businessRadio.setToggleGroup(customerTypeGroup);

        // Set default account type
        accountTypeComboBox.setValue("Savings");

        // Set up listeners for dynamic UI changes
        setupEventListeners();
        updateMinDepositLabel();
        updateFormVisibility();
    }

    private void setupEventListeners() {
        individualRadio.selectedProperty().addListener((obs, oldVal, newVal) -> updateFormVisibility());
        businessRadio.selectedProperty().addListener((obs, oldVal, newVal) -> updateFormVisibility());

        accountTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateFormVisibility();
            updateMinDepositLabel();
        });

        // Validate deposit field to only allow numbers
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

        boolean showEmployerInfo = isIndividual && "Cheque".equals(accountType);
        employerInfoSection.setVisible(showEmployerInfo);
        employerInfoSection.setManaged(showEmployerInfo);

        businessInfoSection.setVisible(isBusiness);
        businessInfoSection.setManaged(isBusiness);
    }

    private void updateMinDepositLabel() {
        String accountType = accountTypeComboBox.getValue();
        if ("Savings".equals(accountType)) {
            minDepositLabel.setText("* Minimum deposit: " + MIN_SAVINGS_DEPOSIT + " BWP");
        } else if ("Investment".equals(accountType)) {
            minDepositLabel.setText("* Minimum deposit: " + MIN_INVESTMENT_DEPOSIT + " BWP");
        } else {
            minDepositLabel.setText("* Minimum deposit: None");
        }
    }

    @FXML
    private void handleSubmit() {
        messageLabel.setText("");

        if (!validateInputs()) {
            return;
        }

        String customerType = individualRadio.isSelected() ? "Individual" : "Business";
        String accountType = accountTypeComboBox.getValue();
        double initialDeposit = Double.parseDouble(initialDepositField.getText());
        String branch = "Main Branch"; // Default branch

        try {
            // For now, using current customer username - in real scenario, you'd select customer
            if (currentCustomerUsername == null) {
                messageLabel.setText("Error: No customer selected");
                return;
            }

            String employer = individualRadio.isSelected() && "Cheque".equals(accountType) ?
                    employerInfoTextArea.getText() : "";
            String companyAddress = "";

            bankService.openAccount(currentCustomerUsername, accountType.toUpperCase(),
                    initialDeposit, branch, employer, companyAddress);

            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("✓ Account opened successfully!");
            clearForm();

        } catch (Exception e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Error: " + e.getMessage());
        }
    }

    private boolean validateInputs() {
        if (accountTypeComboBox.getValue() == null) {
            messageLabel.setText("Error: Please select an account type");
            return false;
        }

        try {
            double deposit = Double.parseDouble(initialDepositField.getText());
            if (deposit <= 0) {
                messageLabel.setText("Error: Initial deposit must be positive");
                return false;
            }

            // Validate minimum deposits
            String accountType = accountTypeComboBox.getValue();
            if ("Savings".equals(accountType) && deposit < MIN_SAVINGS_DEPOSIT) {
                messageLabel.setText("Error: Savings account requires minimum deposit of " + MIN_SAVINGS_DEPOSIT + " BWP");
                return false;
            }

            if ("Investment".equals(accountType) && deposit < MIN_INVESTMENT_DEPOSIT) {
                messageLabel.setText("Error: Investment account requires minimum deposit of " + MIN_INVESTMENT_DEPOSIT + " BWP");
                return false;
            }

        } catch (NumberFormatException e) {
            messageLabel.setText("Error: Please enter a valid deposit amount");
            return false;
        }

        // Validate conditional fields
        if (individualRadio.isSelected() && "Cheque".equals(accountTypeComboBox.getValue()) &&
                (employerInfoTextArea.getText() == null || employerInfoTextArea.getText().trim().isEmpty())) {
            messageLabel.setText("Error: Employer information is required for Cheque accounts");
            return false;
        }

        if (businessRadio.isSelected() &&
                (businessRegistrationField.getText().trim().isEmpty() || businessAddressField.getText().trim().isEmpty())) {
            messageLabel.setText("Error: Business registration and address are required for business customers");
            return false;
        }

        return true;
    }

    private void clearForm() {
        initialDepositField.clear();
        employerInfoTextArea.clear();
        businessRegistrationField.clear();
        businessAddressField.clear();
        contactPersonField.clear();
        accountTypeComboBox.setValue("Savings");
        individualRadio.setSelected(true);
    }

    @FXML
    private void handleBack() {
        // Navigation will be handled by main controller
    }
}