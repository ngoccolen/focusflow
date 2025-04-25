package controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

public class timerController {
	@FXML private Button startButton;
	@FXML private ImageView resetIcon;
	@FXML private Label timeLabel;
	private int time = 20 * 60;
	private Timeline timeline;
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
	public void 
	
}
