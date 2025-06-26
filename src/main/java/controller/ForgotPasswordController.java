package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ForgotPasswordController {
    @FXML private TextField emailField;
    @FXML private Button getOTPButton;
    
    private LogInController parentController;
    
    public void setParentController(LogInController controller) {
        this.parentController = controller;
    }
    
    @FXML
    public void initialize() {
        // Validate email format
        emailField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                emailField.setStyle("-fx-border-color: red;");
            } else {
                emailField.setStyle("");
            }
        });
    }
    
    @FXML
    public void handleGetOTP() {
        String email = emailField.getText();
        if (email.isEmpty()) {
            parentController.showAlert("Please enter your email address.");
            return;
        }
        
        if (parentController.sendOTP(email)) {
            // Mở cửa sổ nhập OTP
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/verify.fxml"));
                Parent root = loader.load();
                
                VerifyController verifyController = loader.getController();
                verifyController.setParentController(parentController);
                
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setTitle("Verify OTP");
                stage.setScene(new Scene(root));
                
                // Đóng cửa sổ hiện tại
                Stage currentStage = (Stage) getOTPButton.getScene().getWindow();
                currentStage.close();
                
                stage.showAndWait();
            } catch (Exception e) {
                e.printStackTrace();
                parentController.showAlert("Failed to open OTP verification window.");
            }
        }
    }
}