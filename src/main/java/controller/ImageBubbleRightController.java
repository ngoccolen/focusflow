package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.ChatMessage;

import java.io.File;
import java.time.format.DateTimeFormatter;

public class ImageBubbleRightController {
    @FXML private ImageView imageContent;
    @FXML private Label timestamp;

    public void setData(ChatMessage msg) {
        try {
            File imageFile = new File(msg.getFilePath());

            if (imageFile.exists()) {
                Image image = new Image(imageFile.toURI().toString(), 200, 0, true, true); // 200px width, auto height
                imageContent.setImage(image);
                imageContent.setPreserveRatio(true);
            } else {
                System.err.println("❌ Image not found (right): " + imageFile.getAbsolutePath());
            }

            // Hiển thị thời gian
            if (msg.getSentAt() != null) {
                timestamp.setText(msg.getSentAt().format(DateTimeFormatter.ofPattern("HH:mm")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
