package controller;

import java.io.IOException;

import org.hibernate.Session;
import org.hibernate.query.Query;

import Util.HibernateUtil;
import Util.PasswordUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import model.User;

public class LogInController {
	@FXML private TextField usernameField, visiblePasswordField;
	@FXML private PasswordField passwordField;
	@FXML private Button loginButton;
	@FXML private ImageView eyeIcon, noEyeIcon;
	@FXML private Label signUpLabel;
	private boolean isPasswordVisible = false;
	
	public void handleLogin() {
	    String username = usernameField.getText();
	    String password = passwordField.getText();
	    if (username.isEmpty() || password.isEmpty()) {
	        showAlert("Please fill in all fields");
	        return;
	    }
	    try (Session session = HibernateUtil.getSessionFactory().openSession()) {
	        Query<User> query = session.createQuery(
	            "FROM User WHERE username = :username", User.class
	        );
	        query.setParameter("username", username);
	        query.setMaxResults(1); // Giới hạn kết quả
	        java.util.List<User> users = query.getResultList();

	        if (users.isEmpty()) {
	            showAlert("Account not found");
	            return;
	        }

	        User user = users.get(0); 

	        if (PasswordUtils.verifyPassword(password, user.getPassword())) {
	            showAlert("Login successful");
	            try {
	            	 FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Dashboard.fxml"));
	                 Parent root = loader.load();
	                 DashboardController dashboardController = loader.getController();
	                 dashboardController.setLoggedInUser(user);
	                 Stage stage = (Stage) loginButton.getScene().getWindow();
	                 stage.setScene(new Scene(root));
	                 stage.show();
	            } catch (IOException e) {
	                e.printStackTrace();
	                System.out.println("Cannot switch to Home");
	            }
	        } else {
	            showAlert("Incorrect password");
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        showAlert("Error: " + e.getMessage());
	    }
	}

	public void initialize() {
	    visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());
	    visiblePasswordField.setVisible(false);
	    visiblePasswordField.setManaged(false);
	    eyeIcon.setVisible(false);
	    noEyeIcon.setVisible(true);
	}
	public void togglePasswordVisibility() {
	    isPasswordVisible = !isPasswordVisible;
	    passwordField.setVisible(!isPasswordVisible);
	    passwordField.setManaged(!isPasswordVisible);
	    visiblePasswordField.setVisible(isPasswordVisible);
	    visiblePasswordField.setManaged(isPasswordVisible);
	    eyeIcon.setVisible(isPasswordVisible);     // icon con mắt hiện khi password hiện
	    noEyeIcon.setVisible(!isPasswordVisible);  // icon gạch mắt hiện khi password ẩn
	}

	public void handleSignUpClick() {
		try {
			Parent loginView = FXMLLoader.load(getClass().getResource("/fxml/SignUp.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(loginView));
		} catch (Exception e) {
            showAlert("Failed to load signup view.");
		}
	}
	private void showAlert(String message) {
		// TODO Auto-generated method stub
		 Alert alert = new Alert(Alert.AlertType.INFORMATION);
	     alert.setContentText(message);
	     alert.showAndWait();
	}
}
