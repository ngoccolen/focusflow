package controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import Util.FXMLUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Pair;
import model.User;

public class GDChinhController {

    @FXML private ImageView DashboardIcon;
    @FXML private ImageView LogoutIcon;
    @FXML private ImageView timeIcon;
    @FXML private ImageView spaceIcon;
    @FXML private ImageView avatarIcon;
    @FXML private ImageView soundIcon;
	private User loggedInUser;
	@FXML private MediaView mainVideo;
    private MediaPlayer mediaPlayer;

    public void initialize() {
        DashboardIcon.setCursor(Cursor.HAND);
        LogoutIcon.setCursor(Cursor.HAND);
        timeIcon.setCursor(Cursor.HAND);
        spaceIcon.setCursor(Cursor.HAND);
        avatarIcon.setCursor(Cursor.HAND);
        soundIcon.setCursor(Cursor.HAND);
    }

    public void handleDashboardClick(MouseEvent event) {
        try {
        	//Trả về 1 cặp (Parent: giao diện, Object: controller của FXML đó)
            Pair<Parent, Object> pair = FXMLUtils.loadFXML("/fxml/Dashboard.fxml");
            //Lấy phần Parent từ pair, đây là gốc của giao diện
            Parent root = pair.getKey();
            DashboardController dashboardController = (DashboardController) pair.getValue();
            dashboardController.setLoggedInUser(this.loggedInUser);
            //Lấy cửa sổ hiện tại từ 1 controller trên giao diện hiện tại, lấy scene hiện tại, lấy stage đang hiển thị scene đó
            Stage stage = (Stage) DashboardIcon.getScene().getWindow();
            //Lấy scene mới từ root, thay scene hiện tại bằng scene mới
            stage.setScene(new Scene(root));
            //hiển thị scene mới
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Space.fxml"));
            Parent root = loader.load();

            spaceController controller = loader.getController(); // Lấy controller chuẩn
            controller.setGDChinhController(this);               // Truyền tham chiếu GDChinhController

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void setLoggedInUser(User user) {
	    this.loggedInUser = user;
	    if (user.getAvatar() != null) {
	        File avatarFile = new File(user.getAvatar());
	        if (avatarFile.exists()) {
	            avatarIcon.setImage(new Image(avatarFile.toURI().toString()));
	        }
	    }
	}
    public void handleAvatarClick() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/hopTrangCaNhan.fxml"));
			Parent root = loader.load();
			AvatarController avatarController = loader.getController();
	        avatarController.setUsername(loggedInUser); 
			Stage avatarStage = new Stage();
			avatarStage.setScene(new Scene(root));
			avatarStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
		initialize();
	}
    public void playVideo(String videoFileName) {  // Đổi tên tham số cho rõ ràng
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }
        
        try {
            // Load video từ resources
            URL videoURL = getClass().getResource("/video/" + videoFileName);
            if (videoURL == null) {
                System.err.println("Video not found: " + videoFileName);
                return;
            }
            
            Media media = new Media(videoURL.toExternalForm());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            mainVideo.setMediaPlayer(mediaPlayer);
            mediaPlayer.play();
            
            mediaPlayer.setOnError(() -> {
                System.err.println("Media Error: " + mediaPlayer.getError());
            });
        } catch (Exception e) {
            System.err.println("Error playing video: " + videoFileName);
            e.printStackTrace();
        }
    }
    public void handleSoundClick() {
    	try {
    		Pair<Parent, Object> pair = FXMLUtils.loadFXML("/fxml/sounds.fxml");
    		Parent root = pair.getKey();
            Stage stage = new Stage();
    		stage.setScene(new Scene(root));
    		stage.show();
    		
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

    
}
