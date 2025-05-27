package controller;

import DAO.VideoDAO;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
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
    @FXML private ImageView soundIcon, noSoundIcon, searchIcon;
    @FXML private StackPane stackAll, stackStudy, stackPet, stackNature, stackLofi, stackAnime;
    @FXML private Label selectedLabel;
    @FXML private TextField searchField;

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
    }

    public void initialize() {
        volumeSlider.setValue(50);
        initData();

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
        searchIcon.setOnMouseClicked(e -> handleSearchClick());
    }
    private VBox createVideoBox(MediaPlayer mp, String title) {
        MediaView mediaView = new MediaView(mp);
        mediaView.setFitWidth(140);
        mediaView.setFitHeight(100);
        mediaView.setPreserveRatio(true);
        mediaView.setSmooth(true);

        Rectangle clip = new Rectangle(140, 100);
        clip.setArcWidth(10);
        clip.setArcHeight(10);
        mediaView.setClip(clip);

        Label label = new Label(title);
        label.setStyle("-fx-font-size: 13px; -fx-text-alignment: center;");
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER);

        VBox.setVgrow(mediaView, Priority.NEVER);
        VBox videoBox = new VBox(5, mediaView, label);
        videoBox.setStyle("-fx-background-color: white; -fx-border-radius: 10; -fx-background-radius: 10;");
        videoBox.setAlignment(Pos.CENTER);
        videoBox.setPrefWidth(160);

        mediaView.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (gdchinhController != null) {
                gdchinhController.playVideo(title);
                gdchinhController.setVolume(volumeSlider.getValue() / 100.0);
                gdchinhController.setMuted(isMuted);
            }
            if (selectedLabel != null) selectedLabel.setText(title);
        });

        return videoBox;
    }
    public void initData() {
        List<File> videoFiles = loadVideoFilesFromResource();
        if (videoFiles.isEmpty()) {
            System.err.println("Không tìm thấy video nào trong thư mục /video.");
            return;
        }

        videoContainer.getChildren().clear();
        mediaPlayers.clear();
        List<Video> videos = videoDAO.getAll(); // LẤY VIDEO TỪ DATABASE

        for (int i = 0; i < videos.size(); i += 2) {
            HBox videoRow = new HBox(13);
            videoRow.setAlignment(Pos.CENTER);

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

                    VBox videoBox = createVideoBox(mp, video.getFileName());
                    videoRow.getChildren().add(videoBox);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            videoContainer.getChildren().add(videoRow);
        }

    }

    private void loadVideosByCategory(String category) {
        saveVideosByCategoryIfNotExists(category);

        videoContainer.getChildren().clear();
        mediaPlayers.clear();

        List<Video> videos = videoDAO.getByCategory(category);

        for (int i = 0; i < videos.size(); i += 2) {
            HBox videoRow = new HBox(13);
            videoRow.setAlignment(Pos.CENTER);

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

                    VBox videoBox = createVideoBox(mp, video.getFileName());
                    videoRow.getChildren().add(videoBox);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            videoContainer.getChildren().add(videoRow);
        }

    }

    private void loadAllVideos() {
        saveAllVideosIfNotExists();

        videoContainer.getChildren().clear();
        mediaPlayers.clear();

        List<Video> videos = videoDAO.getAll();

        for (int i = 0; i < videos.size(); i += 2) {
            HBox videoRow = new HBox(13);
            videoRow.setAlignment(Pos.CENTER);

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

                    VBox videoBox = createVideoBox(mp, video.getFileName());
                    videoRow.getChildren().add(videoBox);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            videoContainer.getChildren().add(videoRow);
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
    @FXML
    public void handleSearchClick() {
        String keyword = searchField.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            loadAllVideos(); // Nếu không nhập gì thì hiển thị tất cả
            return;
        }

        videoContainer.getChildren().clear();
        mediaPlayers.clear();

        List<Video> videos = videoDAO.getAll();
        List<Video> filtered = new ArrayList<>();

        for (Video video : videos) {
            if (video.getFileName().toLowerCase().contains(keyword)) {
                filtered.add(video);
            }
        }

        for (int i = 0; i < filtered.size(); i += 2) {
            HBox videoRow = new HBox(13);
            videoRow.setAlignment(Pos.CENTER);

            for (int j = i; j < i + 2 && j < filtered.size(); j++) {
                Video video = filtered.get(j);
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

                    VBox videoBox = createVideoBox(mp, video.getFileName());
                    videoRow.getChildren().add(videoBox);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            videoContainer.getChildren().add(videoRow);
        }
    }

}