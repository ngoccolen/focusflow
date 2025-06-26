package controller;

import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.User;
import model.Friendship;
import org.hibernate.Session;
import Util.HibernateUtil;

import java.io.File;
import java.time.LocalDateTime;

public class FindFriendItemController {

    @FXML
    private Label nameLabel;

    @FXML
    private ImageView avatarImage;

    @FXML
    private Button AddFriendButton;
    private User targetUser;
    private User loggedInUser;
    public void initialize() {
    	AddFriendButton.setCursor(Cursor.HAND);
    }
    public void setData(User targetUser, User currentUser) {
        this.targetUser = targetUser;
        this.loggedInUser = currentUser;

        nameLabel.setText(targetUser.getUsername());

        // Nếu có ảnh đại diện và file tồn tại thì hiển thị
        if (targetUser.getAvatar() != null && !targetUser.getAvatar().isEmpty()) {
            File avatarFile = new File(targetUser.getAvatar());
            if (avatarFile.exists()) {
                Image avatar = new Image(avatarFile.toURI().toString());
                avatarImage.setImage(avatar);
            }
        }

        // 🔍 Kiểm tra nếu đã có lời mời kết bạn (pending)
        Session session = HibernateUtil.getSessionFactory().openSession();

        Friendship existing = session.createQuery(
            "FROM Friendship f WHERE " +
            "((f.user.id = :currentId AND f.friend.id = :targetId) " +
            "OR (f.user.id = :targetId AND f.friend.id = :currentId)) " +
            "AND f.status = 'pending'", Friendship.class)
            .setParameter("currentId", currentUser.getId())
            .setParameter("targetId", targetUser.getId())
            .uniqueResult();

        session.close();

        if (existing != null) {
            AddFriendButton.setText("Sent");
            AddFriendButton.setDisable(true);
        }
    }



    @FXML
    private void handleAddFriend() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();

        Friendship friendship = new Friendship();
        friendship.setUser(loggedInUser);
        friendship.setFriend(targetUser);
        friendship.setStatus("pending");
        friendship.setCreatedAt(LocalDateTime.now());

        session.save(friendship);
        session.getTransaction().commit();
        session.close();

        AddFriendButton.setText("Sent");
        AddFriendButton.setDisable(true);
    }
}
