package controller;

import java.net.URL;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.input.MouseEvent;

public class spaceController {  

    private GDChinhController gdchinhController;

    public void setGDChinhController(GDChinhController controller) {
        this.gdchinhController = controller;
    }

    private final List<String> videoResources = List.of(
        "/video/941e10c8c92ed5b3e59d83ec1415938a.mp4",
        "/video/1e0247f1828b92b4086b9a522b3228e0.mp4"
    );

    @FXML
    private GridPane videoGrid;

    @FXML
    public void initialize() {
        int col = 0;
        int row = 0;
        
        for (String resourcePath : videoResources) {
            try {
                URL resource = getClass().getResource(resourcePath);
                if (resource == null) {
                    System.err.println("Video resource not found: " + resourcePath);
                    continue;
                }

                Media media = new Media(resource.toExternalForm());
                MediaPlayer mediaPlayer = new MediaPlayer(media);
                mediaPlayer.setMute(true);
                mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                
                // Chỉ truyền tên file, không truyền full URI
                String videoFileName = resourcePath.substring(resourcePath.lastIndexOf("/") + 1);
                MediaView mediaView = createMediaView(mediaPlayer, videoFileName);
                
                videoGrid.add(mediaView, col, row);

                mediaPlayer.setOnError(() -> {
                    System.err.println("MediaPlayer Error: " + mediaPlayer.getError());
                });

                mediaPlayer.play();

                col++;
                if (col >= 2) {
                    col = 0;
                    row++;
                }
            } catch (Exception e) {
                System.err.println("Error loading video: " + resourcePath);
                e.printStackTrace();
            }
        }
    }

    private MediaView createMediaView(MediaPlayer mediaPlayer, String videoFileName) {
        MediaView mediaView = new MediaView(mediaPlayer);
        mediaView.setFitWidth(140);
        mediaView.setFitHeight(100);
        mediaView.setPreserveRatio(true);
        
        mediaView.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (gdchinhController != null) {
                gdchinhController.playVideo(videoFileName);
            }
        });
        
        return mediaView;
    }
}