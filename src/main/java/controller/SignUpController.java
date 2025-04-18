package controller;

import java.io.IOException;

import org.hibernate.Session;
import org.hibernate.Transaction;

import Util.HibernateUtil;
import Util.PasswordUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.User;

public class SignUpController {
    @FXML private TextField nameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField emailField;

    @FXML
    public void handleSignUp() {
        String username = nameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || email.isEmpty() || password.length() < 8) {
            showAlert("Please fill all fields and use a strong password (at least 8 characters).");
            return;
        }
        if (!email.matches("^[a-zA-Z0-9._%+-]+@gmail\\.com$")) {
            showAlert("Please use a valid Gmail address");
            return;
        }

        String hashedPassword = PasswordUtils.hashPassword(password);
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(hashedPassword);

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();
            session.save(user);
            transaction.commit();
            showAlert("Account created successfully.");
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            showAlert("Error saving user: " + e.getMessage());
        }
    }

    @FXML
    public void handleGoogleSignUp() {
        showAlert("Google sign up is not implemented yet.");
    }

    @FXML
    public void handleLoginRedirect() {
        try {
            Parent loginView = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.setScene(new Scene(loginView));
        } catch (IOException e) {
            showAlert("Failed to load login view.");
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
