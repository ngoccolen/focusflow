package controller;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import DAO.SongDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import model.Song;

public class SoundController {
    private List<File> listSongs = new ArrayList<>();
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    private boolean isMuted = false;
    private double lastVolume = 0.5;
    private Duration duration;
    private int currentIndex = 0;
    private model.User currentUser; // gán khi đăng nhập

    @FXML private ImageView playIcon, pauseIcon, soundIcon, NoSoundIcon;
    @FXML private Slider volumeSlider, progressSlider;
    @FXML private ListView<AnchorPane> songListView;

    public void initialize() {
        loadSongsToListView(); // chỉ load danh sách bài hát, chưa play
        setupPlayerUI();
        setupListViewClickHandler();
    }

    private void setupPlayerUI() {
        volumeSlider.setValue(50);
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(newVal.doubleValue() / 100.0);
            }
        });

        progressSlider.setOnMousePressed(e -> {
            if (mediaPlayer != null) mediaPlayer.pause();
        });
        progressSlider.setOnMouseReleased(e -> {
            if (mediaPlayer != null && duration != null) {
                mediaPlayer.seek(Duration.seconds(progressSlider.getValue()));
                if (isPlaying) mediaPlayer.play();
            }
        });
    }

    private void setupListViewClickHandler() {
        songListView.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            int selectedIndex = newVal.intValue();
            if (selectedIndex >= 0 && selectedIndex < listSongs.size()) {
                currentIndex = selectedIndex;
                loadSong(listSongs.get(currentIndex), currentIndex);
                mediaPlayer.play();
                isPlaying = true;
                playIcon.setVisible(false);
                pauseIcon.setVisible(true);
            }
        });
    }

    private void loadSongsToListView() {
        URL resource = getClass().getResource("/audio");
        if (resource != null) {
            try {
                File musicDir = new File(resource.toURI());
                File[] files = musicDir.listFiles((dir, name) -> name.toLowerCase().endsWith("mp3"));
                if (files != null) {
                    listSongs = Arrays.asList(files);
                }
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        songListView.getItems().clear();
        for (int i = 0; i < listSongs.size(); i++) {
            File file = listSongs.get(i);
            String durationStr = getDurationString(file);
            int index = i + 1;

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ListSong.fxml"));
                AnchorPane item = loader.load();
                
                // Thêm dòng này để đảm bảo controller được khởi tạo
                ListSongController controller = loader.getController();
                if (controller != null) {
                    controller.setData(index, file.getName(), durationStr);
                } else {
                    System.err.println("Controller is null for item: " + file.getName());
                }

                songListView.getItems().add(item);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!listSongs.isEmpty()) {
            loadSong(listSongs.get(0), 0);
        }
    }

    private String getDurationString(File songFile) {
        Media media = new Media(songFile.toURI().toString());
        final String[] durationStr = {"00:00"};
        CountDownLatch latch = new CountDownLatch(1);

        MediaPlayer player = new MediaPlayer(media);
        player.setOnReady(() -> {
            Duration duration = media.getDuration();
            int minutes = (int) duration.toMinutes();
            int seconds = (int) duration.toSeconds() % 60;
            durationStr[0] = String.format("%02d:%02d", minutes, seconds);
            player.dispose();
            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return durationStr[0];
    }

    private void loadSong(File songFile, int index) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }

        Media media = new Media(songFile.toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setVolume(volumeSlider.getValue() / 100.0);

        mediaPlayer.setOnReady(() -> {
            duration = mediaPlayer.getTotalDuration();
            progressSlider.setMax(duration.toSeconds());
            volumeSlider.setValue(mediaPlayer.getVolume() * 100);

            String durationStr = String.format("%02d:%02d",
                    (int) duration.toMinutes(), (int) duration.toSeconds() % 60);
            int songNumber = index + 1;

            saveSongToDB(songNumber, songFile.getName(), durationStr);

            mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                if (!progressSlider.isValueChanging() && duration.toSeconds() > 0) {
                    progressSlider.setValue(newTime.toSeconds());
                }
            });

            mediaPlayer.setOnEndOfMedia(this::handleNextClick);

            isPlaying = false;
            playIcon.setVisible(true);
            pauseIcon.setVisible(false);
        });
    }

    private void saveSongToDB(int index, String name, String duration) {
        Song song = new Song();
        song.setSong_number(index);
        song.setSong_name(name);
        song.setDuration(duration);
        song.setUser(currentUser);
        SongDAO.save(song);
    }

    @FXML
    public void handlePlayClick() {
        if (mediaPlayer == null) return;

        if (isPlaying) {
            mediaPlayer.pause();
            playIcon.setVisible(true);
            pauseIcon.setVisible(false);
        } else {
            mediaPlayer.play();
            playIcon.setVisible(false);
            pauseIcon.setVisible(true);
        }

        if (pauseIcon.isVisible()) pauseIcon.toFront();
        else playIcon.toFront();

        isPlaying = !isPlaying;
    }

    @FXML
    public void handleNextClick() {
        if (listSongs == null || listSongs.isEmpty()) return;

        currentIndex = (currentIndex + 1) % listSongs.size();
        loadSong(listSongs.get(currentIndex), currentIndex);
        mediaPlayer.play();
        isPlaying = true;
        playIcon.setVisible(false);
        pauseIcon.setVisible(true);
    }

    @FXML
    public void handlePreviousClick() {
        if (listSongs == null || listSongs.isEmpty()) return;

        currentIndex = (currentIndex - 1 + listSongs.size()) % listSongs.size();
        loadSong(listSongs.get(currentIndex), currentIndex);
        mediaPlayer.play();
        isPlaying = true;
        playIcon.setVisible(false);
        pauseIcon.setVisible(true);
    }

    @FXML
    public void handleResetClick() {
        if (mediaPlayer != null) {
            mediaPlayer.seek(Duration.ZERO);
            progressSlider.setValue(0);
        }
    }

    @FXML
    public void handleMuteClick() {
        if (mediaPlayer == null) return;

        if (isMuted) {
            mediaPlayer.setVolume(lastVolume);
            volumeSlider.setValue(lastVolume * 100);
            soundIcon.setVisible(true);
            NoSoundIcon.setVisible(false);
        } else {
            lastVolume = mediaPlayer.getVolume();
            mediaPlayer.setVolume(0.0);
            volumeSlider.setValue(0.0);
            soundIcon.setVisible(false);
            NoSoundIcon.setVisible(true);
        }
        isMuted = !isMuted;
    }
}
