package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class GDChinhController {
	@FXML private ImageView DashboardIcon;
	@FXML private ImageView LogoutIcon;
	@FXML private ImageView timeIcon;
	public void initialize() {
        DashboardIcon.setCursor(Cursor.HAND);
        LogoutIcon.setCursor(Cursor.HAND);
        timeIcon.setCursor(Cursor.HAND);
    }
	public void handleDashboardClick (MouseEvent event){
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Dashboard.fxml"));
			Parent root = loader.load();
			Stage stage = (Stage) DashboardIcon.getScene().getWindow();
			stage.setScene(new Scene(root));
			stage.show();
		} catch (Exception e) {
			e.printStackTrace();
            System.out.println("Cannot switch to Dashboard");
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
	public void handleTimeClick (MouseEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Timer.fxml"));
			Parent root = loader.load();
			 Stage timerStage = new Stage();
	            timerStage.setTitle("Timer");
	            timerStage.setScene(new Scene(root));
	            timerStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
