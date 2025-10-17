package holiday.bank;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MainController {
    @FXML private Label welcomeLabel;

    public void showUserDashboard(User user) {
        welcomeLabel.setText("Welcome, " + user.getDisplayName());
        // Here you would switch to the appropriate dashboard view
        // based on user role (Teller vs Customer)
    }
}