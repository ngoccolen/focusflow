package controller;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import DAO.SongDAO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import model.Song;

public class SoundController {
    private ArrayList<File> listSongs = new ArrayList<>();
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    private boolean isMuted = false;
    private double lastVolume = 0.5;
    private Duration duration;
    private int currentIndex = 0;
    private model.User currentUser;

    @FXML private ImageView playIcon, pauseIcon, soundIcon, NoSoundIcon, addIcon;
    @FXML private Slider volumeSlider, progressSlider;
    @FXML private ListView<AnchorPane> songListView;
    @FXML private Label durationMainLabel;

    public void initialize() {
        loadSongsToListView();
        setupPlayerUI();
        setupListViewClickHandler();
        soundIcon.setOnMouseClicked(e -> handleMuteClick());
        NoSoundIcon.setOnMouseClicked(e -> handleMuteClick());
        playIcon.setOnMouseClicked(e -> handlePlayClick());
        pauseIcon.setOnMouseClicked(e -> handlePlayClick());
        addIcon.setOnMouseClicked(e -> handleAddClick());
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
                loadSong(listSongs.get(currentIndex), currentIndex, true);
            }
        });
    }

    private void loadSongsFromDB() {
        List<Song> songsFromDB = SongDAO.getAllSongs();
        for (Song song : songsFromDB) {
            File songFile = new File(song.getFile_path());
            if (songFile.exists() && !containsFile(listSongs, songFile)) {
                listSongs.add(songFile);
            }
        }
    }

    private boolean containsFile(List<File> files, File target) {
        for (File file : files) {
            if (file.getAbsolutePath().equals(target.getAbsolutePath())) {
                return true;
            }
        }
        return false;
    }

    private void loadSongsToListView() {
        // Load từ thư mục /audio
        URL resource = getClass().getResource("/audio");
        if (resource != null) {
            try {
                File musicDir = new File(resource.toURI());
                File[] files = musicDir.listFiles((dir, name) -> name.toLowerCase().endsWith("mp3"));
                if (files != null) {
                    listSongs = new ArrayList<>(Arrays.asList(files));
                }
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        // Load từ database
        loadSongsFromDB();

        // Hiển thị lên ListView
        songListView.getItems().clear();
        for (int i = 0; i < listSongs.size(); i++) {
            File file = listSongs.get(i);
            int index = i + 1;

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ListSong.fxml"));
                AnchorPane item = loader.load();
                ListSongController controller = loader.getController();

                controller.setData(index, file.getName(), "--:--");
                songListView.getItems().add(item);

                // Lấy thời lượng bất đồng bộ
                Media media = new Media(file.toURI().toString());
                MediaPlayer player = new MediaPlayer(media);
                player.setOnReady(() -> {
                    Duration duration = media.getDuration();
                    int minutes = (int) duration.toMinutes();
                    int seconds = (int) duration.toSeconds() % 60;
                    String durationStr = String.format("%02d:%02d", minutes, seconds);

                    Platform.runLater(() -> controller.setData(index, file.getName(), durationStr));
                    player.dispose();
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!listSongs.isEmpty()) {
            loadSong(listSongs.get(0), 0, false);
        }
    }

    private void updatePlayPauseIcons(boolean isNowPlaying) {
        playIcon.setVisible(!isNowPlaying);
        pauseIcon.setVisible(isNowPlaying);
        if (isNowPlaying) pauseIcon.toFront();
        else playIcon.toFront();
    }

    private void loadSong(File songFile, int index, boolean autoPlay) {
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

            String durationStr = String.format("%02d:%02d",
                    (int) duration.toMinutes(), (int) duration.toSeconds() % 60);
            int songNumber = index + 1;

            // Chỉ lưu vào DB nếu chưa tồn tại
            Song existingSong = SongDAO.getSongByPath(songFile.getAbsolutePath());
            if (existingSong == null) {
                saveSongToDB(songNumber, songFile.getName(), durationStr, songFile.getAbsolutePath());
            }

            mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                if (!progressSlider.isValueChanging()) {
                    progressSlider.setValue(newTime.toSeconds());
                    int minutes = (int) newTime.toMinutes();
                    int seconds = (int) newTime.toSeconds() % 60;
                    durationMainLabel.setText(String.format("%02d:%02d", minutes, seconds));
                }
            });

            mediaPlayer.setOnEndOfMedia(this::handleNextClick);

            if (autoPlay) {
                mediaPlayer.play();
                updatePlayPauseIcons(true);
                isPlaying = true;
            } else {
                updatePlayPauseIcons(false);
                isPlaying = false;
            }
        });
    }

    private void saveSongToDB(int index, String name, String duration, String filePath) {
        Song song = new Song();
        song.setSong_number(index);
        song.setSong_name(name);
        song.setDuration(duration);
        song.setFile_path(filePath);
        if (currentUser != null) {
            song.getUsers().add(currentUser);
        }
        SongDAO.save(song);
    }

    // Các phương thức handleClick giữ nguyên như cũ
    @FXML
    public void handlePlayClick() {
        if (mediaPlayer == null) return;

        MediaPlayer.Status status = mediaPlayer.getStatus();
        if (status == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause();
            updatePlayPauseIcons(false);
            isPlaying = false;
        } else {
            mediaPlayer.play();
            updatePlayPauseIcons(true);
            isPlaying = true;
        }
    }

    @FXML
    public void handleNextClick() {
        if (listSongs == null || listSongs.isEmpty()) return;
        currentIndex = (currentIndex + 1) % listSongs.size();
        loadSong(listSongs.get(currentIndex), currentIndex, isPlaying);
    }

    @FXML
    public void handlePreviousClick() {
        if (listSongs == null || listSongs.isEmpty()) return;
        currentIndex = (currentIndex - 1 + listSongs.size()) % listSongs.size();
        loadSong(listSongs.get(currentIndex), currentIndex, isPlaying);
    }

    @FXML
    public void handleAddClick() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Chọn bài hát MP3");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("MP3 Files", "*.mp3"));

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null && !containsFile(listSongs, selectedFile)) {
            listSongs.add(selectedFile);

            int index = listSongs.size();
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ListSong.fxml"));
                AnchorPane item = loader.load();
                ListSongController controller = loader.getController();

                controller.setData(index, selectedFile.getName(), "--:--");
                songListView.getItems().add(item);

                Media media = new Media(selectedFile.toURI().toString());
                MediaPlayer player = new MediaPlayer(media);
                player.setOnReady(() -> {
                    Duration duration = media.getDuration();
                    int minutes = (int) duration.toMinutes();
                    int seconds = (int) duration.toSeconds() % 60;
                    String durationStr = String.format("%02d:%02d", minutes, seconds);

                    Platform.runLater(() -> {
                        controller.setData(index, selectedFile.getName(), durationStr);
                        // Chỉ lưu vào DB nếu chưa tồn tại
                        Song existingSong = SongDAO.getSongByPath(selectedFile.getAbsolutePath());
                        if (existingSong == null) {
                            saveSongToDB(index, selectedFile.getName(), durationStr, selectedFile.getAbsolutePath());
                        }
                    });
                    player.dispose();
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Các phương thức handleResetClick và handleMuteClick giữ nguyên như cũ
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

    public void setCurrentUser(model.User user) {
        this.currentUser = user;
    }
}