package controller;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import model.ChatMessage;
import model.ChatConversation;
import model.ChatMember;
import model.User;
import java.io.File;
import javafx.scene.image.Image;
import controller.CommunityController;
import java.time.format.DateTimeFormatter;

public class MessItemController {
    @FXML private Label FriendName;
    @FXML private Label SendTime;
    @FXML private Label SenderLabel;
    @FXML private Label messContent;
    @FXML private ImageView FriendAvatar;
    private CommunityController parentController;
    private User friend;
    
    public void setData(ChatMessage msg, User currentUser, CommunityController parentController) {
        this.parentController = parentController;

        ChatConversation chat = msg.getChat();
        boolean isGroupChat = chat.isGroup(); // ⚠️ Dùng đúng cờ isGroup

        User friend = (msg.getSender().getId() == currentUser.getId())
                      ? getReceiverFromChat(chat, currentUser)
                      : msg.getSender();

        // ✅ Hiển thị tên nhóm nếu là group, tên bạn nếu là chat riêng
        FriendName.setText(isGroupChat ? chat.getGroupName() : friend.getUsername());

        messContent.setText(msg.getContent());
        SendTime.setText(msg.getSentAt().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));

        // ✅ Hiển thị "You:" hoặc tên người gửi
        if ("Start chatting...".equals(msg.getContent())) {
            SenderLabel.setText(""); // Tin nhắn mặc định → không hiển thị
        } else {
            SenderLabel.setText(
                msg.getSender().getId() == currentUser.getId()
                ? "You:"
                : msg.getSender().getUsername() + ":"
            );
        }

        // ✅ Avatar:
        if (isGroupChat) {
            // 👉 Dùng avatar nhóm mặc định
            FriendAvatar.setImage(new Image(getClass().getResource("/images/group_icon.png").toExternalForm()));
        } else if (friend.getAvatar() != null) {
            File avatarFile = new File(friend.getAvatar());
            if (avatarFile.exists()) {
                FriendAvatar.setImage(new Image(avatarFile.toURI().toString()));
            }
        }

        // ✅ Khi click vào thì load đúng đoạn chat
        FriendName.getParent().setOnMouseClicked(e -> {
            parentController.setSelectedChat(chat);
            if (isGroupChat) {
                parentController.loadGroupMessages(chat, currentUser);
            } else {
                parentController.loadMessagesBetweenUsers(currentUser, friend);
            }
        });
    }

    // Lấy người còn lại trong cuộc chat (khác currentUser)
    private User getReceiverFromChat(ChatConversation chat, User currentUser) {
        if (chat.getMembers() == null) return null;

        for (ChatMember member : chat.getMembers()) {
            if (member.getUser().getId() != currentUser.getId()) {
                return member.getUser();
            }
        }
        return null;
    }


}
