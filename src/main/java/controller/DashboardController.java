package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import model.User;

public class DashboardController {
	@FXML private ImageView GiaoDienChinhIcon;
	@FXML private ImageView LogoutIcon;
	@FXML private Label welcomeLabel;
	private String username;
	public void handleGDChinhClick (MouseEvent event){
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/GiaoDienChinh.fxml"));
			Parent root = loader.load();
			Stage stage = (Stage) GiaoDienChinhIcon.getScene().getWindow();
			stage.setScene(new Scene(root));
			stage.show();
		} catch (Exception e) {
			e.printStackTrace();
            System.out.println("Cannot switch to Giao Dien Chinh");
		}
	}
	public void handleLogoutClick (MouseEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SignUp.fxml"));
			Parent root = loader.load();
			Stage stage = (Stage) LogoutIcon.getScene().getWindow();
			stage.setScene(new Scene(root));
			stage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void setLoggedInUser(User user) {
		this.username = user.getUsername();
		setWelcome();
	}
	public void setWelcome() {
		welcomeLabel.setText("Hi! " + username + ", welcome!");
	}

}
