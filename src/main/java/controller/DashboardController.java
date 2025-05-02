package controller;

import java.io.File;
import java.time.LocalDate;
import java.time.YearMonth;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import model.User;

public class DashboardController {
	@FXML private ImageView GiaoDienChinhIcon;
	@FXML private ImageView LogoutIcon;
	@FXML private Label welcomeLabel;
	@FXML private ImageView avatarIcon;
	private User loggedInUser;
	private Parent root;
	@FXML private GridPane calendarGrid;
	@FXML private Label monthLabel;
	@FXML private Label yearLabel; 
	@FXML private YearMonth currentYearMonth;
	@FXML private ImageView rightArrow;
	@FXML private ImageView leftArrow;
	
	public void setRoot(Parent root) {
	    this.root = root;
	}

	public Parent getRoot() {
	    return root;
	}
	public void initialize() {
        GiaoDienChinhIcon.setCursor(Cursor.HAND);
        LogoutIcon.setCursor(Cursor.HAND);
        avatarIcon.setCursor(Cursor.HAND);
        if (currentYearMonth == null) {
            currentYearMonth = YearMonth.now();
        }
        updateCalendar();
        rightArrow.setCursor(Cursor.HAND);
        leftArrow.setCursor(Cursor.HAND);
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
	    this.loggedInUser = user;
	    setWelcome();
	    if (user.getAvatar() != null) {
	        File avatarFile = new File(user.getAvatar());
	        if (avatarFile.exists()) {
	            avatarIcon.setImage(new Image(avatarFile.toURI().toString()));
	        }
	    }
	}

	public void setWelcome() {
		welcomeLabel.setText("Hi! " + loggedInUser.getUsername() + ", welcome!");
	}
	public void handleAvatarClick() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/hopTrangCaNhan.fxml"));
			Parent root = loader.load();
			AvatarController avatarController = loader.getController();
			avatarController.setUsername(this.loggedInUser);
			avatarController.setDashboardController(this);// truyền cả đối tượng User

			Stage avatarStage = new Stage();
			avatarStage.setScene(new Scene(root));
			avatarStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
		initialize();
	}
	public void updateAvatarImage(String avatarPath) {
	    if (avatarPath != null) {
	        File avatarFile = new File(avatarPath);
	        if (avatarFile.exists()) {
	            avatarIcon.setImage(new Image(avatarFile.toURI().toString()));
	        }
	    }
	}
	public void updateCalendar() {
		ObservableList<Node> children = calendarGrid.getChildren();
		children.removeIf(node -> GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) > 0);
		LocalDate firstDayOfMonth = currentYearMonth.atDay(1);
		int daysInMonth = currentYearMonth.lengthOfMonth();
		int startDayOfWeek = firstDayOfMonth.getDayOfWeek().getValue();
		
		monthLabel.setText(String.valueOf(currentYearMonth.getMonthValue()));
		yearLabel.setText(String.valueOf(currentYearMonth.getYear()));
		int col = (startDayOfWeek % 7);
		int row = 1;
		for (int day = 1; day <= daysInMonth; day++) {
			Label dayLabel = new Label(String.valueOf(day));
			calendarGrid.add(dayLabel, col, row);
			col++;
			if (col == 7) {
				col = 0;
				row++;
			}
		}
	}
	public void handleLeftClick() {
		currentYearMonth = currentYearMonth.minusMonths(1);
		updateCalendar();
	}
	public void handleRightClick() {
		currentYearMonth = currentYearMonth.plusMonths(1);
		updateCalendar();
	}

}
