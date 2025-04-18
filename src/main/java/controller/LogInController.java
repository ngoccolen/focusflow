package controller;

import org.hibernate.Session;
import org.hibernate.query.Query;

import Util.HibernateUtil;
import Util.PasswordUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import model.User;

public class LogInController {
	@FXML private TextField usernameField;
	@FXML private PasswordField passwordField;
	@FXML
	public void handleLogin() {
		String username = usernameField.getText();
		String password = passwordField.getText();
		if (username.isEmpty() || password.isEmpty()) {
			showAlert("Please fill in all fields");
			return;
		}
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			Query<User> query = session.createQuery(
				    "FROM User WHERE username = :username AND password = :password", User.class
				);
			query.setParameter("username", username);
			query.setParameter("password", password);
			User user = query.uniqueResult();
			if (user == null) {
				showAlert("Account not found");
				return;
			}
			if (PasswordUtils.verifyPassword(password, user.getPassword())) {
				showAlert("Account not found");
				return;
			} else {
				showAlert("Incorrect password");
			}
		} catch (Exception e) {
			e.printStackTrace();
			showAlert("Error: " + e.getMessage());
		}
	}
	private void showAlert(String message) {
		// TODO Auto-generated method stub
		 Alert alert = new Alert(Alert.AlertType.INFORMATION);
	     alert.setContentText(message);
	     alert.showAndWait();
	}
}
