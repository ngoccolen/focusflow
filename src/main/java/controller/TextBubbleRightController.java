package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import model.ChatMessage;
import model.User;

import java.io.File;
import java.time.format.DateTimeFormatter;

public class TextBubbleRightController {

    @FXML
    private AnchorPane chatBox;

    @FXML
    private Label messageContent;

    @FXML
    private Label timestamp;


    public void setData(ChatMessage message, User sender) {
        messageContent.setText(message.getContent());
        messageContent.setWrapText(true);            // ✅ Bắt buộc để xuống dòng
        messageContent.setMaxWidth(400); 
        // Hiển thị thời gian
        if (message.getSentAt() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            timestamp.setText(message.getSentAt().format(formatter));
        }
    }

}
