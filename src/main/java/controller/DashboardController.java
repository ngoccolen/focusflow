package controller;

import java.io.File;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import model.User;

public class DashboardController {
	@FXML private ImageView GiaoDienChinhIcon;
	@FXML private ImageView LogoutIcon;
	@FXML private Label welcomeLabel;
	@FXML private ImageView avatarIcon;
	private String username;
	@FXML private Label emailLabel;
	public void initialize() {
        GiaoDienChinhIcon.setCursor(Cursor.HAND);
        LogoutIcon.setCursor(Cursor.HAND);
        avatarIcon.setCursor(Cursor.HAND);
    }
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
		initialize();
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
		initialize();
	}
	public void setLoggedInUser(User user) {
		this.username = user.getUsername();
		setWelcome();
		if (user.getAvatar() != null) {
	        File avatarFile = new File(user.getAvatar());
	        if (avatarFile.exists()) {
	            avatarIcon.setImage(new Image(avatarFile.toURI().toString()));
	        }
	    }
		emailLabel.setText(user.getEmail());
	}
	public void setWelcome() {
		welcomeLabel.setText("Hi! " + username + ", welcome!");
	}
	public void handleAvatarClick() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/hopTrangCaNhan.fxml"));
			Parent root = loader.load();
			Stage timerStage = new Stage();
	        timerStage.setScene(new Scene(root));
	        timerStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
		initialize();
	}

}
