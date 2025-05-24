package controller;

import DAO.VideoDAO;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.shape.Rectangle;
import model.User;
import model.Video;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class spaceController {

    private GDChinhController gdchinhController;

    @FXML private VBox videoContainer;
    @FXML private Slider volumeSlider;
    @FXML private ImageView soundIcon, noSoundIcon;
    @FXML private StackPane stackAll, stackStudy, stackPet, stackNature, stackLofi, stackAnime;
    @FXML private Label selectedLabel;

    private boolean isMuted = false;
    private List<MediaPlayer> mediaPlayers = new ArrayList<>();
    private VideoDAO videoDAO = new VideoDAO();

    public void handleStudyClick(MouseEvent event) { loadVideosByCategory("study"); }
    public void handleAnimeClick(MouseEvent event) { loadVideosByCategory("anime"); }
    public void handleLofiClick(MouseEvent event) { loadVideosByCategory("lofi"); }
    public void handlePetClick(MouseEvent event) { loadVideosByCategory("pet"); }
    public void handleNatureClick(MouseEvent event) { loadVideosByCategory("nature"); }
    public void handleAllClick(MouseEvent event) { loadAllVideos(); }

    public void setGDChinhController(GDChinhController controller) {
        this.gdchinhController = controller;
        initData();
    }

    public void initialize() {
        volumeSlider.setValue(50);

        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double volume = newVal.doubleValue() / 100.0;
            if (!isMuted) {
                for (MediaPlayer mp : mediaPlayers) mp.setVolume(volume);
                if (gdchinhController != null) gdchinhController.setVolume(volume);
            }
        });

        soundIcon.setOnMouseClicked(e -> {
            isMuted = true;
            volumeSlider.setValue(0);
            for (MediaPlayer mp : mediaPlayers) mp.setMute(true);
            if (gdchinhController != null) {
                gdchinhController.setMuted(true);
                gdchinhController.setVolume(0);
            }
            soundIcon.setVisible(false);
            noSoundIcon.setVisible(true);
        });

        noSoundIcon.setOnMouseClicked(e -> {
            isMuted = false;
            double restoredVolume = 0.5;
            volumeSlider.setValue(restoredVolume * 100);
            for (MediaPlayer mp : mediaPlayers) {
                mp.setMute(false);
                mp.setVolume(restoredVolume);
            }
            if (gdchinhController != null) {
                gdchinhController.setMuted(false);
                gdchinhController.setVolume(restoredVolume);
            }
            noSoundIcon.setVisible(false);
            soundIcon.setVisible(true);
        });
    }

    public void initData() {
        List<File> videoFiles = loadVideoFilesFromResource();
        if (videoFiles.isEmpty()) {
            System.err.println("Không tìm thấy video nào trong thư mục /video.");
            return;
        }

        videoContainer.getChildren().clear();
        mediaPlayers.clear();

        for (int i = 0; i < videoFiles.size(); i += 2) {
            HBox videoRow = new HBox(20);
            HBox labelRow = new HBox(20);
            videoRow.setAlignment(Pos.CENTER);
            labelRow.setAlignment(Pos.CENTER);

            for (int j = i; j < i + 2 && j < videoFiles.size(); j++) {
                File file = videoFiles.get(j);
                String fileUri = file.toURI().toString();
                String fileName = file.getName();
                String title = fileName.substring(0, fileName.lastIndexOf("."));

                Media media = new Media(fileUri);
                MediaPlayer mediaPlayer = new MediaPlayer(media);
                mediaPlayer.setMute(true);
                mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                mediaPlayer.setAutoPlay(false);
                mediaPlayer.pause();
                mediaPlayers.add(mediaPlayer);

                MediaView mediaView = createMediaView(mediaPlayer, fileName);
                mediaView.setFitWidth(140);
                mediaView.setFitHeight(100);

                Label label = new Label(title);
                HBox.setHgrow(label, Priority.ALWAYS);
                label.setAlignment(Pos.CENTER);
                label.setMaxWidth(Double.MAX_VALUE);
                label.setStyle("-fx-font-size: 12px;");

                videoRow.getChildren().add(mediaView);
                labelRow.getChildren().add(label);
            }

            VBox group = new VBox(5);
            group.setAlignment(Pos.CENTER);
            group.getChildren().addAll(videoRow, labelRow);

            videoContainer.getChildren().add(group);
        }
    }

    private void loadVideosByCategory(String category) {
        saveVideosByCategoryIfNotExists(category);

        videoContainer.getChildren().clear();
        mediaPlayers.clear();

        List<Video> videos = videoDAO.getByCategory(category);

        for (int i = 0; i < videos.size(); i += 2) {
            HBox videoRow = new HBox(10);
            HBox labelRow = new HBox(10);
            videoRow.setAlignment(Pos.CENTER);
            labelRow.setAlignment(Pos.CENTER);

            for (int j = i; j < i + 2 && j < videos.size(); j++) {
                Video video = videos.get(j);
                String uri = video.getFilePath();
                try {
                    File f = new File(new URI(uri));
                    if (!f.exists()) continue;

                    Media media = new Media(uri);
                    MediaPlayer mp = new MediaPlayer(media);
                    mp.setMute(true);
                    mp.pause();
                    mp.setCycleCount(MediaPlayer.INDEFINITE);
                    mediaPlayers.add(mp);

                    MediaView mediaView = createMediaView(mp, video.getFileName());
                    HBox.setHgrow(mediaView, Priority.ALWAYS);
                    mediaView.setPreserveRatio(true);
                    mediaView.setFitHeight(90);
                    mediaView.setSmooth(true);

                    Label label = new Label(video.getFileName());
                    HBox.setHgrow(label, Priority.ALWAYS);
                    label.setAlignment(Pos.CENTER);
                    label.setMaxWidth(Double.MAX_VALUE);
                    label.setStyle("-fx-font-size: 12px;");

                    videoRow.getChildren().add(mediaView);
                    labelRow.getChildren().add(label);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            VBox group = new VBox(5);
            group.setAlignment(Pos.CENTER);
            group.getChildren().addAll(videoRow, labelRow);

            videoContainer.getChildren().add(group);
        }
    }

    private void loadAllVideos() {
        saveAllVideosIfNotExists();

        videoContainer.getChildren().clear();
        mediaPlayers.clear();

        List<Video> videos = videoDAO.getAll();

        for (int i = 0; i < videos.size(); i += 2) {
            HBox videoRow = new HBox(10);
            HBox labelRow = new HBox(10);
            videoRow.setAlignment(Pos.CENTER);
            labelRow.setAlignment(Pos.CENTER);

            for (int j = i; j < i + 2 && j < videos.size(); j++) {
                Video video = videos.get(j);
                String uri = video.getFilePath();
                try {
                    File f = new File(new URI(uri));
                    if (!f.exists()) continue;

                    Media media = new Media(uri);
                    MediaPlayer mp = new MediaPlayer(media);
                    mp.setMute(true);
                    mp.pause();
                    mp.setCycleCount(MediaPlayer.INDEFINITE);
                    mediaPlayers.add(mp);

                    MediaView mediaView = createMediaView(mp, video.getFileName());
                    mediaView.setFitHeight(90);

                    Label label = new Label(video.getFileName());
                    HBox.setHgrow(label, Priority.ALWAYS);
                    label.setAlignment(Pos.CENTER);
                    label.setMaxWidth(Double.MAX_VALUE);
                    label.setStyle("-fx-font-size: 12px;");

                    videoRow.getChildren().add(mediaView);
                    labelRow.getChildren().add(label);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            VBox group = new VBox(10);
            group.setAlignment(Pos.CENTER);
            group.getChildren().addAll(videoRow, labelRow);

            videoContainer.getChildren().add(group);
        }
    }

    private void saveVideosByCategoryIfNotExists(String category) {
        User currentUser = gdchinhController != null ? gdchinhController.getCurrentUser() : null;
        if (currentUser == null) {
            System.err.println("User hiện tại chưa sẵn sàng!");
            return;
        }

        List<File> videoFiles = loadVideoFilesFromResource();
        for (File file : videoFiles) {
            String fileName = file.getName();
            String fileCategory = fileName.contains("_") ? fileName.split("_")[0] : "other";
            if (fileCategory.equalsIgnoreCase(category)) {
                String fileUri = file.toURI().toString();
                videoDAO.saveIfNotExists(fileName, fileUri, currentUser, fileCategory);
            }
        }
    }

    private void saveAllVideosIfNotExists() {
        User currentUser = gdchinhController != null ? gdchinhController.getCurrentUser() : null;
        if (currentUser == null) {
            System.err.println("User hiện tại chưa sẵn sàng!");
            return;
        }

        List<File> videoFiles = loadVideoFilesFromResource();
        for (File file : videoFiles) {
            String fileName = file.getName();
            String fileCategory = fileName.contains("_") ? fileName.split("_")[0] : "other";
            String fileUri = file.toURI().toString();
            videoDAO.saveIfNotExists(fileName, fileUri, currentUser, fileCategory);
        }
    }

    private List<File> loadVideoFilesFromResource() {
        List<File> videoFiles = new ArrayList<>();
        try {
            URI uri = getClass().getResource("/video").toURI();
            File videoDir = new File(uri);
            File[] files = videoDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp4"));
            if (files != null) {
                videoFiles = Arrays.asList(files);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return videoFiles;
    }

    private MediaView createMediaView(MediaPlayer mediaPlayer, String videoFileName) {
        MediaView mediaView = new MediaView(mediaPlayer);
        mediaView.setPreserveRatio(true);
        Rectangle clip = new Rectangle(140, 100);
        clip.setArcWidth(10);
        clip.setArcHeight(10);
        mediaView.setClip(clip);

        mediaView.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (gdchinhController != null) {
                gdchinhController.playVideo(videoFileName);
                gdchinhController.setVolume(volumeSlider.getValue() / 100.0);
                gdchinhController.setMuted(isMuted);
            }
            if (selectedLabel != null) {
                selectedLabel.setText(videoFileName);
            }
        });

        return mediaView;
    }
}