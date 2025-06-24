package controller;

import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.ChatConversation;
import model.Friendship;
import model.User;
import model.ChatMember;
import model.ChatMemberId;
import java.io.File;
import java.time.LocalDateTime;

import org.hibernate.Session;
import Util.HibernateUtil;

public class FriendRequestItemController {

    @FXML
    private Label nameLabel;

    @FXML
    private ImageView avatarImage;

    @FXML
    private Button acceptButton;

    @FXML
    private Button deleteButton;
    private CommunityController parentController;
    private Friendship friendship;
    private User loggedInUser;
    public void initialize() {
    	acceptButton.setCursor(Cursor.HAND);
    	deleteButton.setCursor(Cursor.HAND);
    }
    public void setData(Friendship friendship, User currentUser) {
        this.friendship = friendship;
        this.loggedInUser = currentUser;

        User sender = friendship.getUser(); // người gửi lời mời
        nameLabel.setText(sender.getUsername());

        // Nếu có ảnh đại diện và file tồn tại thì hiển thị
        if (sender.getAvatar() != null && !sender.getAvatar().isEmpty()) {
            File avatarFile = new File(sender.getAvatar());
            if (avatarFile.exists()) {
                avatarImage.setImage(new Image(avatarFile.toURI().toString()));
            } else {
                avatarImage.setImage(null); // Xoá ảnh nếu không tồn tại file
            }
        } else {
            avatarImage.setImage(null); // Không hiển thị gì nếu không có avatar
        }
    }
    public void setParentController(CommunityController controller) {
        this.parentController = controller;
    }

    @FXML
    private void handleAccept() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();

        // ✅ 1. Cập nhật trạng thái lời mời kết bạn
        friendship.setStatus("accepted");
        session.update(friendship);

        // ✅ 2. Tạo mới một cuộc trò chuyện
        ChatConversation chat = new ChatConversation();
        chat.setCreatedAt(LocalDateTime.now());
        session.save(chat); // Lúc này chat đã có ID để gán cho ChatMember

        // ✅ 3. Tạo ChatMember cho loggedInUser (người nhận lời mời)
        ChatMember member1 = new ChatMember();
        member1.setChat(chat);
        member1.setUser(loggedInUser);
        member1.setJoinedAt(LocalDateTime.now());
        session.save(member1);

        // ✅ 4. Tạo ChatMember cho người gửi lời mời
        ChatMember member2 = new ChatMember();
        member2.setChat(chat);
        member2.setUser(friendship.getUser()); // người gửi lời mời
        member2.setJoinedAt(LocalDateTime.now());
        session.save(member2);

        // ✅ Hoàn tất
        session.getTransaction().commit();
        session.close();

        acceptButton.setText("Accepted");
        acceptButton.setDisable(true);
        deleteButton.setDisable(true);
        if (parentController != null) {
            parentController.loadFriendList(); // 🔥 load lại danh sách messenger
        }
    }


    @FXML
    private void handleDelete() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        session.remove(friendship);
        session.getTransaction().commit();
        session.close();

        acceptButton.setText("Deleted");
        acceptButton.setDisable(true);
        deleteButton.setDisable(true);
    }
}
