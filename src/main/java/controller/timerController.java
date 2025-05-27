package controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.AudioClip;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

import java.io.IOException;

public class timerController {
    @FXML private Button startButton;
    @FXML private Button pauseButton;
    @FXML private ImageView resetIcon;
    @FXML private ImageView settingIcon;
    @FXML private Label timeLabel;
    @FXML private Label shortLabel;
    @FXML private Label longLabel;
    @FXML private Label pomodoroLabel;
    
    private int time = 25 * 60;
    private int pomodoroTime = 25;
    private int shortBreakTime = 5;
    private int longBreakTime = 15;
    private Timeline timeline;
    private boolean isRunning = false;

    public void initialize() {
        startButton.setCursor(Cursor.HAND);
        pauseButton.setCursor(Cursor.HAND);
        resetIcon.setCursor(Cursor.HAND);
        shortLabel.setCursor(Cursor.HAND);
        longLabel.setCursor(Cursor.HAND);
        pomodoroLabel.setCursor(Cursor.HAND);
        settingIcon.setCursor(Cursor.HAND);
        
        pauseButton.setVisible(false);
    }

    public void startTimer() {
        if (timeline != null) {
            timeline.stop();
        }
        timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> {
                    time--;
                    updateTimeDisplay();

                    if (time <= 0) {
                        timeline.stop();
                        playAlarm();
                        isRunning = false;
                        toggleButtons();
                    }
                })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        isRunning = true;
        toggleButtons();
    }

    public void pauseTimer() {
        if (timeline != null) {
            timeline.stop();
            isRunning = false;
            toggleButtons();
        }
    }

    private void toggleButtons() {
        startButton.setVisible(!isRunning);
        pauseButton.setVisible(isRunning);
    }

    public void updateTimeDisplay() {
        int minute = time / 60;
        int second = time % 60;
        timeLabel.setText(String.format("%02d:%02d", minute, second));
    }

    public void handleStartClick(MouseEvent e) {
        startTimer();
    }

    public void handlePauseClick(MouseEvent e) {
        pauseTimer();
    }

    public void handleResetClick(MouseEvent e) {
        time = pomodoroTime * 60; // Sử dụng giá trị pomodoroTime mới
        updateTimeDisplay();
        if (timeline != null) {
            timeline.stop();
            isRunning = false;
            toggleButtons();
        }
    }

    public void playAlarm() {
        String path = getClass().getResource("/audio/alarm.mp3").toExternalForm();
        AudioClip clip = new AudioClip(path);
        clip.play();
    }

    public void handleShortClick(MouseEvent e) {
        time = shortBreakTime * 60;
        updateTimeDisplay();
        if (timeline != null) {
            timeline.stop();
            isRunning = false;
            toggleButtons();
        }
        startTimer();
    }

    public void handleLongClick(MouseEvent e) {
        time = longBreakTime * 60;
        updateTimeDisplay();
        if (timeline != null) {
            timeline.stop();
            isRunning = false;
            toggleButtons();
        }
        startTimer();
    }

    public void handlePomodoroClick(MouseEvent e) {
        time = pomodoroTime * 60;
        updateTimeDisplay();
        if (timeline != null) {
            timeline.stop();
            isRunning = false;
            toggleButtons();
        }
        startTimer();
    }

    public void handleSettingsClick(MouseEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SettingDialogs.fxml"));
            AnchorPane settingsPane = loader.load();
            
            TextField pomodoroField = (TextField) settingsPane.lookup("#pomodoroField");
            TextField shortBreakField = (TextField) settingsPane.lookup("#shortBreakField");
            TextField longBreakField = (TextField) settingsPane.lookup("#longBreakField");
            Button saveButton = (Button) settingsPane.lookup("#saveButton");
            
            // Set current values
            pomodoroField.setText(String.valueOf(pomodoroTime));
            shortBreakField.setText(String.valueOf(shortBreakTime));
            longBreakField.setText(String.valueOf(longBreakTime));
            
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Timer Settings");
            dialog.getDialogPane().setContent(settingsPane);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            
            saveButton.setOnAction(event -> {
                try {
                    int newPomodoro = Integer.parseInt(pomodoroField.getText());
                    int newShortBreak = Integer.parseInt(shortBreakField.getText());
                    int newLongBreak = Integer.parseInt(longBreakField.getText());
                    
                    // Cập nhật giá trị mới
                    pomodoroTime = newPomodoro;
                    shortBreakTime = newShortBreak;
                    longBreakTime = newLongBreak;
                    
                    // Cập nhật thời gian hiện tại NGAY LẬP TỨC
                    time = pomodoroTime * 60; // Luôn cập nhật về giá trị Pomodoro mới
                    updateTimeDisplay(); // Cập nhật hiển thị
                    
                    dialog.close();
                } catch (NumberFormatException ex) {
                    showAlert("Invalid Input", "Please enter valid numbers for all fields");
                }
            });
            
            dialog.showAndWait();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}