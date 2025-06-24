package controller;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.ChatMessage;
import model.User;

import java.io.File;
import java.time.format.DateTimeFormatter;
public class ImageBubbleLeftController {
    @FXML private ImageView avatarImage;
    @FXML private Label senderName;
    @FXML private ImageView imageContent;
    @FXML private Label timestamp;

    public void setData(ChatMessage msg, User sender) {
        senderName.setText(sender.getUsername());

        // Avatar
        if (sender.getAvatar() != null) {
            File avatarFile = new File(sender.getAvatar());
            if (avatarFile.exists()) {
                avatarImage.setImage(new Image(avatarFile.toURI().toString()));
            }
        }

        // Ảnh trong chat - sử dụng đường dẫn tuyệt đối từ message
        if (msg.getFilePath() != null) {
            File imageFile = new File(msg.getFilePath());
            if (imageFile.exists()) {
                Image image = new Image(imageFile.toURI().toString(), 200, 0, true, true);
                imageContent.setImage(image);
                imageContent.setPreserveRatio(true);
            } else {
                System.err.println("Image not found (left): " + imageFile.getAbsolutePath());
            }
        }

        // Thời gian gửi
        if (msg.getSentAt() != null) {
            timestamp.setText(msg.getSentAt().format(DateTimeFormatter.ofPattern("HH:mm")));
        }
    }

}

