package bank.finalsys;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {
    @FXML private TextField menuChoiceField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private javafx.scene.layout.VBox loginFields;
    @FXML private Button exitButton;
    @FXML private Label messageLabel;

    private BankService bankService;
    private Stage primaryStage;

    public LoginController() {
        this.bankService = new BankService();
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    public void initialize() {
        menuChoiceField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("[12]?")) {
                menuChoiceField.setText(oldVal);
            } else {
                updateLoginFieldsVisibility();
            }
        });
    }

    private void updateLoginFieldsVisibility() {
        String choice = menuChoiceField.getText();
        if ("1".equals(choice)) {
            loginFields.setVisible(true);
            exitButton.setVisible(false);
        } else if ("2".equals(choice)) {
            loginFields.setVisible(false);
            exitButton.setVisible(true);
        } else {
            loginFields.setVisible(false);
            exitButton.setVisible(false);
        }
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter both username and password");
            return;
        }

        try {
            User user = bankService.authenticateUser(username, password);
            if (user != null) {
                messageLabel.setText("Login successful! Welcome, " + user.getDisplayName());
                loadUserDashboard(user);
            } else {
                messageLabel.setText("Invalid username or password");
            }
        } catch (Exception e) {
            messageLabel.setText("Login error: " + e.getMessage());
        }
    }

    @FXML
    private void handleExit() {
        System.exit(0);
    }

    private void loadUserDashboard(User user) {
        try {
            FXMLLoader loader;
            switch (user.getRole()) {
                case TELLER:
                    loader = new FXMLLoader(getClass().getResource("TellerDashboardView.fxml"));
                    break;
                case CUSTOMER:
                    loader = new FXMLLoader(getClass().getResource("CustomerDashboardView.fxml"));
                    break;
                default:
                    throw new IllegalStateException("Unknown user role: " + user.getRole());
            }

            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof TellerDashboardController) {
                ((TellerDashboardController) controller).setCurrentUser(user);
                ((TellerDashboardController) controller).setPrimaryStage(primaryStage);
            } else if (controller instanceof CustomerDashboardController) {
                ((CustomerDashboardController) controller).setCurrentUser((BankCustomer) user);
                ((CustomerDashboardController) controller).setPrimaryStage(primaryStage);
            }

            Scene scene = new Scene(root, 800, 600);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Botswana National Bank - " + user.getRole() + " Dashboard");

        } catch (Exception e) {
            messageLabel.setText("Error loading dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }
}