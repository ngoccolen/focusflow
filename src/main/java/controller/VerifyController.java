package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class VerifyController {
    @FXML private HBox otpBox;
    @FXML private TextField otpField1, otpField2, otpField3, otpField4, otpField5, otpField6;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button verifyButton;
    @FXML private Button saveButton;
    
    private LogInController parentController;
    
    public void setParentController(LogInController controller) {
        this.parentController = controller;
        setupOTPFields();
    }
    
    @FXML
    public void initialize() {
        // Giới hạn độ dài OTP fields
        setupOTPFieldLength(otpField1);
        setupOTPFieldLength(otpField2);
        setupOTPFieldLength(otpField3);
        setupOTPFieldLength(otpField4);
        setupOTPFieldLength(otpField5);
        setupOTPFieldLength(otpField6);
    }
    
    private void setupOTPFieldLength(TextField field) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 1) {
                field.setText(newVal.substring(0, 1));
            }
        });
    }
    
    private void setupOTPFields() {
        // Tự động chuyển focus giữa các OTP field
        otpField1.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() == 1) otpField2.requestFocus();
        });
        otpField2.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() == 1) otpField3.requestFocus();
        });
        otpField3.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() == 1) otpField4.requestFocus();
        });
        otpField4.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() == 1) otpField5.requestFocus();
        });
        otpField5.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() == 1) otpField6.requestFocus();
        });
    }
    
    @FXML
    public void handleVerifyOTP() {
        StringBuilder otp = new StringBuilder();
        otp.append(otpField1.getText());
        otp.append(otpField2.getText());
        otp.append(otpField3.getText());
        otp.append(otpField4.getText());
        otp.append(otpField5.getText());
        otp.append(otpField6.getText());
        
        if (otp.length() != 6) {
            parentController.showAlert("Please enter a complete 6-digit OTP.");
            return;
        }
        
        if (parentController.verifyOTP(otp.toString())) {
            newPasswordField.setDisable(false);
            confirmPasswordField.setDisable(false);
            saveButton.setDisable(false);
            parentController.showAlert("OTP verified. Please enter your new password.");
        } else {
            parentController.showAlert("Invalid OTP. Please try again.");
            clearOTPFields();
        }
    }
    
    @FXML
    public void handleSavePassword() {
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            parentController.showAlert("Please fill in both password fields.");
            return;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            parentController.showAlert("Passwords do not match.");
            return;
        }
        
        if (newPassword.length() < 6) {
            parentController.showAlert("Password must be at least 6 characters long.");
            return;
        }
        
        if (parentController.updatePassword(newPassword)) {
            parentController.showAlert("Password updated successfully!");
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.close();
        } else {
            parentController.showAlert("Failed to update password. Please try again.");
        }
    }
    
    private void clearOTPFields() {
        otpField1.clear();
        otpField2.clear();
        otpField3.clear();
        otpField4.clear();
        otpField5.clear();
        otpField6.clear();
        otpField1.requestFocus();
    }
}