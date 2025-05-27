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
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Pair;
import model.User;
import service.StudySessionService; // Thêm import cho service

public class GDChinhController {

    @FXML private ImageView DashboardIcon;
    @FXML private ImageView LogoutIcon;
    @FXML private ImageView timeIcon;
    @FXML private ImageView spaceIcon;
    @FXML private ImageView avatarIcon;
    @FXML private ImageView soundIcon, quoteIcon;
    @FXML private MediaView mainVideo;
    @FXML private ImageView taskIcon;
    @FXML private ImageView calendarIcon;
    @FXML private ImageView noteIcon;
    
    private User loggedInUser;
    private MediaPlayer mediaPlayer;
    private StudySessionService studySessionService; // Thêm service
    private Stage quotesStage;

    public void initialize() {
        // Khởi tạo service
        studySessionService = StudySessionService.getInstance();
        
        // Thiết lập cursor
        DashboardIcon.setCursor(Cursor.HAND);
        LogoutIcon.setCursor(Cursor.HAND);
        timeIcon.setCursor(Cursor.HAND);
        spaceIcon.setCursor(Cursor.HAND);
        avatarIcon.setCursor(Cursor.HAND);
        soundIcon.setCursor(Cursor.HAND);
        quoteIcon.setCursor(Cursor.HAND);
        taskIcon.setCursor(Cursor.HAND);
        calendarIcon.setCursor(Cursor.HAND);
        noteIcon.setCursor(Cursor.HAND);
        
        // Bắt đầu tính thời gian học khi vào giao diện
        if (loggedInUser != null) {
            studySessionService.startStudySession(loggedInUser);
        }
    }

    @FXML
    public void handleDashboardClick(MouseEvent event) {
        try {
            // Kết thúc phiên học khi chuyển sang Dashboard
            studySessionService.endStudySession();
            
            Pair<Parent, Object> pair = FXMLUtils.loadFXML("/fxml/Dashboard.fxml");
            Parent root = pair.getKey();
            DashboardController dashboardController = (DashboardController) pair.getValue();
            dashboardController.setLoggedInUser(this.loggedInUser);
            
            Stage stage = (Stage) DashboardIcon.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Cannot switch to Dashboard");
        }
    }

    @FXML
    public void handleLogoutClick(MouseEvent event) {
        try {
            // Kết thúc phiên học khi logout
            studySessionService.endStudySession();
            
            Pair<Parent, Object> pair = FXMLUtils.loadFXML("/fxml/SignUp.fxml");
            Parent root = pair.getKey();
            Stage stage = (Stage) LogoutIcon.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
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

    @FXML
    public void handleSpaceClick(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Space.fxml"));
            Parent root = loader.load();

            spaceController controller = loader.getController();
            controller.setGDChinhController(this);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleQuotesClick(MouseEvent event) {
        try {
            // Nếu đang hiển thị thì không làm gì cả (hoặc có thể toFront())
            if (quotesStage != null && quotesStage.isShowing()) {
                return;
            }

            Pair<Parent, Object> pair = FXMLUtils.loadFXML("/fxml/quotes.fxml");
            Parent root = pair.getKey();
            QuotesController quotesController = (QuotesController) pair.getValue();
            
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            
            quotesStage = new Stage();
            quotesStage.initStyle(StageStyle.TRANSPARENT);
            quotesStage.setScene(scene);
            
            // Truyền stage vào controller
            quotesController.setStage(quotesStage);
            
            // Hiển thị ở vị trí chuột
            quotesStage.setX(event.getScreenX());
            quotesStage.setY(event.getScreenY());
            
            // Quan trọng: Xử lý đóng cửa sổ
            quotesStage.setOnCloseRequest(e -> {
                quotesStage = null;
            });
            
            quotesStage.show();
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
        
        // Bắt đầu tính giờ học nếu chưa bắt đầu
        if (studySessionService != null && !studySessionService.isActive()) {
            studySessionService.startStudySession(user);
        }
    }

    @FXML
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
    }

    @FXML
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

    public void playVideo(String videoFileName) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }
        
        try {
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

    public User getCurrentUser() {
        return loggedInUser;
    }

    public void setVolume(double volume) {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(volume);
        }
    }

    public void setMuted(boolean muted) {
        if (mediaPlayer != null) {
            mediaPlayer.setMute(muted);
        }
    }
    public void handleTaskClick(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Task.fxml"));
            Parent root = loader.load();

            // Lấy controller và truyền User
            TaskController taskController = loader.getController();
            taskController.setLoggedInUser(loggedInUser);

            Stage stage = new Stage();
            stage.setTitle("Task Manager");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Cannot load Task.fxml");
        }
    }
    public void handleCalendarClick(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/calendar.fxml"));
            Parent root = loader.load();

            // Lấy controller của Cal.fxml
            CalendarController calendarController = loader.getController();

            // Truyền user đang đăng nhập vào CalendarController
            calendarController.setLoggedInUser(loggedInUser);

            Stage stage = new Stage();
            stage.setTitle("Calendar");
            stage.setScene(new Scene(root));
            stage.show();

           
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Cannot load Cal.fxml");
        }
    }
    @FXML
    public void handleNoteClick(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Notes.fxml"));
            Parent root = loader.load();

            NoteController noteController = loader.getController();
            noteController.setLoggedInUser(loggedInUser); 

            Stage stage = new Stage();
            stage.setTitle("Notes");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Cannot load Notes.fxml");
        }
    }
}