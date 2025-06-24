package controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import org.hibernate.Session;
import org.hibernate.query.Query;

import Util.EmailUtil;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.User;

public class LogInController {
	@FXML private TextField usernameField, visiblePasswordField;
	@FXML private PasswordField passwordField;
	@FXML private Button loginButton;
	@FXML private ImageView eyeIcon, noEyeIcon;
	@FXML private Label signUpLabel;
	private boolean isPasswordVisible = false;
	private String currentOTP;
	private String currentEmail;
    private LocalDateTime otpExpirationTime;

	
	public interface LoginCallback {
        void onSuccess(String username);
    }

    private LoginCallback loginCallback;

    // Thêm setter cho callback
    public void setOnLoginSuccess(LoginCallback callback) {
        this.loginCallback = callback;
    }

	
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
	            if (loginCallback != null) {
	                loginCallback.onSuccess(username); // Truyền username về Main
	            }
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
	public void showAlert(String message) {
		// TODO Auto-generated method stub
		 Alert alert = new Alert(Alert.AlertType.INFORMATION);
	     alert.setContentText(message);
	     alert.showAndWait();
	}
	  @FXML
	    public void handleForgotPasswordClick() {
	        try {
	            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/forgotPassword.fxml"));
	            Parent root = loader.load();
	            
	            // Lấy controller của forgotPassword
	            ForgotPasswordController forgotPasswordController = loader.getController();
	            forgotPasswordController.setParentController(this);
	            
	            Stage stage = new Stage();
	            stage.initModality(Modality.APPLICATION_MODAL);
	            stage.setTitle("Forgot Password");
	            stage.setScene(new Scene(root));
	            stage.showAndWait();
	        } catch (IOException e) {
	            e.printStackTrace();
	            showAlert("Failed to open forgot password window.");
	        }
	    }
	  public boolean sendOTP(String email) {
	        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
	            // Kiểm tra email có tồn tại
	            Query<User> query = session.createQuery("FROM User WHERE email = :email", User.class);
	            query.setParameter("email", email);
	            query.setMaxResults(1);
	            List<User> users = query.getResultList();

	            if (users.isEmpty()) {
	                showAlert("Email not found in our system.");
	                return false;
	            }

	            // Tạo OTP 6 chữ số
	            Random random = new Random();
	            currentOTP = String.format("%06d", random.nextInt(999999));
	            currentEmail = email;
	            otpExpirationTime = LocalDateTime.now().plusMinutes(5); // OTP hết hạn sau 5 phút

	            // Gửi email
	            String subject = "Password Reset OTP";
	            String content = "Your OTP is: " + currentOTP + "\nExpires in 5 minutes.";
	            
	            if (EmailUtil.sendEmail(email, subject, content)) {
	                showAlert("OTP sent to your email!");
	                return true;
	            } else {
	                showAlert("Failed to send OTP. Please try again.");
	                return false;
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	            showAlert("Error: " + e.getMessage());
	            return false;
	        }
	    }
	    
	  public boolean verifyOTP(String enteredOTP) {
	        if (LocalDateTime.now().isAfter(otpExpirationTime)) {
	            showAlert("OTP has expired. Please request a new one.");
	            return false;
	        }
	        return enteredOTP.equals(currentOTP);
	    }

	    public boolean updatePassword(String newPassword) {
	        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
	            session.beginTransaction();
	            
	            Query<User> query = session.createQuery("FROM User WHERE email = :email", User.class);
	            query.setParameter("email", currentEmail);
	            User user = query.uniqueResult();
	            
	            if (user != null) {
	                user.setPassword(PasswordUtils.hashPassword(newPassword));
	                session.update(user);
	                session.getTransaction().commit();
	                return true;
	            }
	            return false;
	        } catch (Exception e) {
	            e.printStackTrace();
	            showAlert("Error updating password: " + e.getMessage());
	            return false;
	        }
	    }
	    
	    public boolean resendOTP() {
	        if (currentEmail == null) return false;
	        return sendOTP(currentEmail);
	    }


	
}
