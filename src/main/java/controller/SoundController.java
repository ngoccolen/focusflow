package controller;

import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class SoundController {
    @FXML private ImageView playIcon, resetIcon, nextIcon, previousIcon;
    @FXML private Slider progressSlider, volumeSlider;
    @FXML private Label durationLabel, songNameLabel;
   

    private List<String> playList = List.of(
        getClass().getResource("/audio/02. Guren no Yumiya.mp3").toExternalForm(),
        getClass().getResource("/audio/not-lazy-just-chill-268462.mp3").toExternalForm()
    );

    private int currentSongIndex = 0;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;

    @FXML
    public void initialize() {
        loadMedia(currentSongIndex);

        // Cho phép kéo để tua bài
        progressSlider.setOnMousePressed(e -> mediaPlayer.pause());
        progressSlider.setOnMouseReleased(e -> {
            mediaPlayer.seek(Duration.seconds(progressSlider.getValue()));
            if (isPlaying) mediaPlayer.play();
        });
        volumeSlider.setValue(50); // từ 0 đến 100
        mediaPlayer.setVolume(0.5);

        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            mediaPlayer.setVolume(newVal.doubleValue() / 100.0);
        });

        // Gán tên bài hát
        songNameLabel.setText("Ôm trọn nỗi nhớ - RUM");
    }

    private void loadMedia(int index) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }

        Media media = new Media(playList.get(index));
        mediaPlayer = new MediaPlayer(media);

        mediaPlayer.setOnReady(() -> {
            Duration total = media.getDuration();
            progressSlider.setMax(total.toSeconds());
            durationLabel.setText(formatTime(total));
        });

        mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
            if (!progressSlider.isValueChanging()) {
                progressSlider.setValue(newTime.toSeconds());
            }
        });

        mediaPlayer.setOnEndOfMedia(this::handleNextClick);
        
    }

    // PLAY / PAUSE
    @FXML
    private void handlePlayClick() {
        if (mediaPlayer == null) return;

        if (isPlaying) {
            mediaPlayer.pause();
        } else {
            mediaPlayer.play();
        }
        isPlaying = !isPlaying;
    }

    // NEXT
    @FXML
    private void handleNextClick() {
        currentSongIndex = (currentSongIndex + 1) % playList.size();
        loadMedia(currentSongIndex);
        mediaPlayer.play();
        isPlaying = true;
    }

    // PREVIOUS
    @FXML
    private void handlePreviousClick() {
        currentSongIndex = (currentSongIndex - 1 + playList.size()) % playList.size();
        loadMedia(currentSongIndex);
        mediaPlayer.play();
        isPlaying = true;
    }

    // RESET
    @FXML
    private void handleResetClick() {
        if (mediaPlayer != null) {
            mediaPlayer.seek(Duration.ZERO);
            progressSlider.setValue(0);
        }
    }

    private String formatTime(Duration duration) {
        int minutes = (int) duration.toMinutes();
        int seconds = (int) (duration.toSeconds() % 60);
        return String.format("%d:%02d", minutes, seconds);
    }
    
}
