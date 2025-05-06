package controller;

import Util.FXMLUtils;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Pair;

public class GDChinhController {

    @FXML private ImageView DashboardIcon;
    @FXML private ImageView LogoutIcon;
    @FXML private ImageView timeIcon;
    @FXML private ImageView spaceIcon;

    public void initialize() {
        DashboardIcon.setCursor(Cursor.HAND);
        LogoutIcon.setCursor(Cursor.HAND);
        timeIcon.setCursor(Cursor.HAND);
        spaceIcon.setCursor(Cursor.HAND);
    }

    public void handleDashboardClick(MouseEvent event) {
        try {
            Pair<Parent, Object> pair = FXMLUtils.loadFXML("/fxml/Dashboard.fxml");
            Parent root = pair.getKey();
            Stage stage = (Stage) DashboardIcon.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Cannot switch to Dashboard");
        }
    }

    public void handleLogoutClick(MouseEvent event) {
        try {
            Pair<Parent, Object> pair = FXMLUtils.loadFXML("/fxml/SignUp.fxml");
            Parent root = pair.getKey();
            Stage stage = (Stage) LogoutIcon.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void handleTimeClick(MouseEvent event) {
        try {
            Pair<Parent, Object> pair = FXMLUtils.loadFXML("/fxml/Timer.fxml");
            Parent root = pair.getKey();
            Stage timerStage = new Stage();
            timerStage.setTitle("Timer");
            timerStage.setScene(new Scene(root));
            timerStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void handleSpaceClick(MouseEvent event) {
        try {
            Pair<Parent, Object> pair = FXMLUtils.loadFXML("/fxml/Space.fxml");
            Parent root = pair.getKey();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
