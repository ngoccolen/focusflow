package controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.AudioClip;
import javafx.util.Duration;

public class timerController {
	@FXML private Button startButton;
	@FXML private ImageView resetIcon;
	@FXML private Label timeLabel;
	@FXML private Label shortLabel;
	@FXML private Label longLabel;
	@FXML private Label pomodoroLabel;
	private int time = 20 * 60;
	private Timeline timeline;
	public void initialize() {
		startButton.setCursor(Cursor.HAND);
		resetIcon.setCursor(Cursor.HAND);
		shortLabel.setCursor(Cursor.HAND);
		longLabel.setCursor(Cursor.HAND);
		pomodoroLabel.setCursor(Cursor.HAND);
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
	                }
	            })
	        );
		timeline.setCycleCount(Timeline.INDEFINITE);
		timeline.play();
	}
	public void updateTimeDisplay() {
		int minute = time / 60;
		int second = time % 60;
		timeLabel.setText(String.format("%02d:%02d", minute, second));
	}
	public void handleStartClick (MouseEvent e) {
		startTimer();
	}
	public void handleResetClick (MouseEvent e) {
		time = 20 * 60;
		updateTimeDisplay();
		if (timeline != null) {
			timeline.stop();
		}
	}
	public void playAlarm() {
		String path = getClass().getResource("/audio/alarm.mp3").toExternalForm();
        AudioClip clip = new AudioClip(path);
        clip.play();
	}
	public void handleShortClick (MouseEvent e) {
		time = 5 * 60;
		updateTimeDisplay();
		if (timeline != null) {
			timeline.stop();
		}
		startTimer();
	}
	public void handleLongClick (MouseEvent e) {
		time = 15 * 60;
		updateTimeDisplay();
		if (timeline != null) {
			timeline.stop();
		}
		startTimer();
	}
	public void handlePomodoroClick (MouseEvent e) {
		time = 20*60;
		updateTimeDisplay();
		if (timeline != null) {
			timeline.stop();
		}
		startTimer();
	}
	
}
