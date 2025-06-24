package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.ChatMessage;
import model.User;

import java.io.File;
import java.time.format.DateTimeFormatter;

public class TextBubbleLeftController {

    @FXML
    private ImageView avatarImage;

    @FXML
    private Label senderName;

    @FXML
    private Label messageContent;

    @FXML
    private Label timestamp;

    /**
     * Hàm gán dữ liệu cho bong bóng tin nhắn bên trái (người khác gửi)
     */
    public void setData(ChatMessage message, User sender) {
        // Gán nội dung tin nhắn
        messageContent.setText(message.getContent());
        messageContent.setWrapText(true);            // ✅ Bắt buộc để xuống dòng
        messageContent.setMaxWidth(400); 
        // Gán tên người gửi
        senderName.setText(sender.getUsername());

        // Gán avatar nếu có
        if (sender.getAvatar() != null && !sender.getAvatar().isEmpty()) {
            File avatarFile = new File(sender.getAvatar());
            if (avatarFile.exists()) {
                avatarImage.setImage(new Image(avatarFile.toURI().toString()));
            }
        }

        // Gán thời gian gửi (định dạng: HH:mm)
        if (message.getSentAt() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            timestamp.setText(message.getSentAt().format(formatter));
        } else {
            timestamp.setText("");
        }
    }
}
