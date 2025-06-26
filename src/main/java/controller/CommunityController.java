package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Pair;

import org.hibernate.Session;
import org.hibernate.query.Query;

import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;

import Util.HibernateUtil;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;

import javafx.scene.image.Image;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

// Import các model liên quan
import model.ChatConversation;
import model.ChatMember;
import model.ChatMessage;
import model.Message;
import model.User;
import client.ChatClient;
// Controller con của mỗi item tin nhắn
import controller.MessItemController;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ListView;


public class CommunityController {
    @FXML
    private ImageView DashboardIcon, GiaoDienChinhIcon;
    @FXML
    private ImageView LogoutIcon;
    @FXML
    private ImageView avatarIcon;
    @FXML
    private Label MessengerLabel;
    @FXML
    private Button createGroupButton;
    @FXML
    private Label MyFriendsLabel;
    @FXML private Popup emojiPopup;
    @FXML private GridPane emojiGrid;
    @FXML private Button emojiButton;
    @FXML
    private VBox messList;
    @FXML private ScrollPane scrollPane;

    private ChatConversation selectedChat;
    private User loggedInUser;
    private User selectedFriend;


    public void setSelectedChat(ChatConversation chat) {
        this.selectedChat = chat;
    }
    private ChatClient chatClient;
    @FXML private VBox MessPane;
    @FXML private Label FriendName;
    @FXML private ImageView FriendAvatar;
    @FXML private TextField MessContent;
    @FXML private ImageView FileIcon;
    

    public void handleGDChinhClick (MouseEvent event){
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/GiaoDienChinh.fxml"));
			Parent root = loader.load();
		    GDChinhController gdchinhController = loader.getController();
		    gdchinhController.setLoggedInUser(this.loggedInUser);
			Stage stage = (Stage) GiaoDienChinhIcon.getScene().getWindow();
			stage.setScene(new Scene(root));
			stage.show();
		} catch (Exception e) {
			e.printStackTrace();
            System.out.println("Cannot switch to Giao Dien Chinh");
		}
	}
    @FXML
    public void initialize() {
    	 emojiGrid = new GridPane(); 
    	    emojiGrid.setHgap(5);
    	    emojiGrid.setVgap(5);
    	    emojiGrid.setPadding(new Insets(10));
    	    emojiGrid.setStyle("-fx-background-color: white; -fx-border-color: gray; -fx-border-radius: 5px;");

    	    try {
    	        String[] emojiFileNames = {
    	            "1f600.png", "1f602.png", "1f603.png", "1f604.png", "1f60d.png",
    	            "1f618.png", "1f622.png", "1f62d.png", "1f609.png", "2764-fe0f.png"
    	        };

    	        int col = 0, row = 0;
    	        for (String fileName : emojiFileNames) {
    	            InputStream is = getClass().getResourceAsStream("/emojis/" + fileName);
    	            if (is == null) {
    	                System.err.println("❌ Không tìm thấy emoji: " + fileName);
    	                continue;
    	            }

    	            Image img = new Image(is, 32, 32, true, true);
    	            ImageView view = new ImageView(img);

    	            String finalName = fileName; // phải final để dùng trong lambda
    	            view.setOnMouseClicked(e -> {
    	                sendEmojiAsMessage(finalName);
    	                emojiPopup.hide();
    	            });

    	            emojiGrid.add(view, col++, row);
    	            if (col >= 10) {
    	                col = 0;
    	                row++;
    	            }
    	        }

    	        // Gán emojiGrid vào popup
    	        emojiPopup = new Popup();
    	        emojiPopup.setAutoHide(true);
    	        emojiPopup.getContent().add(emojiGrid);

    	        emojiButton.setOnAction(e -> {
    	            emojiPopup.show(emojiButton,
    	                emojiButton.localToScreen(0, 0).getX(),
    	                emojiButton.localToScreen(0, 0).getY() + emojiButton.getHeight()
    	            );
    	        });

    	    } catch (Exception e) {
    	        e.printStackTrace();
    	        System.err.println("❌ Lỗi khi load emoji: " + e.getMessage());
    	    }

        // ⚠️ Dọn thư mục tạm chứa ảnh
        File imageTempDir = new File("temp_images");
        if (!imageTempDir.exists()) {
            imageTempDir.mkdir();
        } else {
            for (File file : imageTempDir.listFiles()) {
                file.delete();
            }
        }

        // ⚠️ Dọn thư mục tạm chứa file
        File fileTempDir = new File("temp_files");
        if (!fileTempDir.exists()) {
            fileTempDir.mkdir();
        } else {
            for (File file : fileTempDir.listFiles()) {
                file.delete();
            }
        }
        DashboardIcon.setCursor(Cursor.HAND);
        LogoutIcon.setCursor(Cursor.HAND);
        MessengerLabel.setCursor(Cursor.HAND);
        MyFriendsLabel.setCursor(Cursor.HAND);
        createGroupButton.setCursor(Cursor.HAND);
        FileIcon.setCursor(Cursor.HAND);
        try {
            chatClient = new ChatClient();

            // ✅ Gán callback để hiển thị tin nhắn khi nhận được
            chatClient.setOnMessageReceived(msg -> {
                if (selectedChat != null && msg.getChatId() == selectedChat.getChatId()) {
                    javafx.application.Platform.runLater(() -> {
                        try {
                            boolean isMine = msg.getSenderName().equals(loggedInUser.getUsername());

                            ChatMessage chatMsg = new ChatMessage();
                            chatMsg.setContent(msg.getContent());
                            chatMsg.setSentAt(msg.getSentAt());

                            // Gán người gửi tạm
                            Session session = HibernateUtil.getSessionFactory().openSession();
                            session.beginTransaction();

                            // ✅ Load người gửi từ DB
                            Query<User> query = session.createQuery("FROM User WHERE username = :uname", User.class);
                            query.setParameter("uname", msg.getSenderName());
                            User sender = query.uniqueResult();

                            if (sender == null) {
                                System.err.println("❌ Không tìm thấy người gửi trong DB: " + msg.getSenderName());
                                session.getTransaction().rollback();
                                session.close();
                                return;
                            }

                            // ✅ Gán vào chatMsg
                            chatMsg.setSender(sender);

                            chatMsg.setChat(selectedChat);
                            chatMsg.setType(msg.getType());
                            chatMsg.setFilePath(msg.getFilePath());
                            session.save(chatMsg);
                            session.getTransaction().commit();
                            session.close();


                            FXMLLoader loader;
                            AnchorPane bubble;

                            if (isMine) {
                                // 👉 Nếu là tin nhắn của mình thì dùng bubble bên phải
                                switch (msg.getType()) {
                                    case "text":
                                        loader = new FXMLLoader(getClass().getResource("/fxml/textBubble_right.fxml"));
                                        bubble = loader.load();
                                        TextBubbleRightController rightTextCtrl = loader.getController();
                                        rightTextCtrl.setData(chatMsg, loggedInUser);
                                        break;
                                    case "image":
                                        loader = new FXMLLoader(getClass().getResource("/fxml/imageBubble_right.fxml"));
                                        bubble = loader.load();
                                        ImageBubbleRightController rightImageCtrl = loader.getController();
                                        rightImageCtrl.setData(chatMsg);
                                        break;
                                     // Trong phần xử lý tin nhắn file
                                    case "file":
                                        loader = new FXMLLoader(getClass().getResource("/fxml/fileBubble_left.fxml"));
                                        bubble = loader.load();
                                        FileBubbleLeftController leftFileCtrl = loader.getController();
                                        // Đảm bảo truyền đủ thông tin file
                                        leftFileCtrl.setData(chatMsg, sender);
                                        break;
                                    default:
                                        System.out.println("Unsupported message type: " + msg.getType());
                                        return;
                                }
                            } else {
                                switch (msg.getType()) {
                                    case "text":
                                        loader = new FXMLLoader(getClass().getResource("/fxml/textBubble_left.fxml"));
                                        bubble = loader.load();
                                        TextBubbleLeftController leftTextCtrl = loader.getController();
                                        leftTextCtrl.setData(chatMsg, sender);
                                        break;
                                    case "image":
                                        loader = new FXMLLoader(getClass().getResource("/fxml/imageBubble_left.fxml"));
                                        bubble = loader.load();
                                        ImageBubbleLeftController leftImageCtrl = loader.getController();
                                        leftImageCtrl.setData(chatMsg, sender);
                                        break;
                                    case "file":
                                        loader = new FXMLLoader(getClass().getResource("/fxml/fileBubble_left.fxml"));
                                        bubble = loader.load();
                                        FileBubbleLeftController leftFileCtrl = loader.getController();
                                        leftFileCtrl.setData(chatMsg, sender);
                                        break;
                                    default:
                                        System.out.println("Unsupported message type: " + msg.getType());
                                        return;
                                }
                            }

                            MessPane.getChildren().add(bubble);
                            Platform.runLater(() -> scrollPane.setVvalue(1.0));

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Sự kiện nhấn Enter để gửi tin nhắn
        MessContent.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER:
                    handleSendByEnter();
                    break;
                default:
                    break;
            }
        });
    }
    private void sendEmojiAsMessage(String emojiFileName) {
        if (selectedChat == null || loggedInUser == null) {
            showAlert("Lỗi", "Vui lòng chọn cuộc trò chuyện trước khi gửi emoji.");
            return;
        }

        try {
            File emojiFile = new File("src/main/resources/emojis/" + emojiFileName);
            if (!emojiFile.exists()) {
                System.out.println("❌ Emoji file not found: " + emojiFile.getAbsolutePath());
                return;
            }

            Message msg = new Message();
            msg.setSenderName(loggedInUser.getUsername());
            msg.setSenderAvatar(loggedInUser.getAvatar());
            msg.setType("image"); // hoặc "emoji"
            msg.setSentAt(LocalDateTime.now());
            msg.setContent(emojiFileName);
            msg.setFilePath(emojiFile.getAbsolutePath());
            msg.setFileData(Files.readAllBytes(emojiFile.toPath()));
            msg.setFileSize(emojiFile.length());
            msg.setChatId(selectedChat.getChatId()); // ✅ dùng selectedChat
            
            chatClient.sendMessage(msg);
        } catch (IOException e) {
            showAlert("Lỗi", "Không thể gửi emoji: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public void updateAvatarImage(String path) {
        if (path != null) {
            File avatarFile = new File(path);
            if (avatarFile.exists()) {
                avatarIcon.setImage(new Image(avatarFile.toURI().toString()));
            }
        }
    }


    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
         loadFriendList();
         loadGroupChats();
         updateAvatarImage(user.getAvatar()); // Thêm dòng này

    }

    public void handleDashboardClick(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Dashboard.fxml"));
            Parent root = loader.load();
            DashboardController dashboardController = loader.getController();
            dashboardController.setLoggedInUser(this.loggedInUser);
            Stage stage = (Stage) DashboardIcon.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Cannot switch to Dashboard");
        }
    }

    public void handleLogoutClick(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SignUp.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) LogoutIcon.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleAvatarClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/hopTrangCaNhan.fxml"));
            Parent root = loader.load();
            AvatarController avatarController = loader.getController();
            avatarController.setUsername(loggedInUser);
            Stage avatarStage = new Stage();
            avatarStage.setScene(new Scene(root));
            avatarStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        initialize();
    }

    public List<User> getAcceptedFriends(User loggedInUser) {
        Session session = HibernateUtil.getSessionFactory().openSession();

        List<User> friendsAsUser = session.createQuery(
            "SELECT f.friend FROM Friendship f WHERE f.user = :user AND f.status = 'accepted'", User.class)
            .setParameter("user", loggedInUser)
            .list();

        List<User> friendsAsFriend = session.createQuery(
            "SELECT f.user FROM Friendship f WHERE f.friend = :user AND f.status = 'accepted'", User.class)
            .setParameter("user", loggedInUser)
            .list();

        session.close();

        // Gộp 2 danh sách lại
        friendsAsUser.addAll(friendsAsFriend);
        return friendsAsUser;
    }

    public void loadFriendList() {
        messList.getChildren().clear();

        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();

        List<User> friends = getAcceptedFriends(loggedInUser);

        for (User friend : friends) {
            try {
                // Tìm hoặc tạo Chat 1-1
                ChatConversation chat = session.createQuery(
                    "SELECT DISTINCT c FROM ChatConversation c " +
                    "JOIN FETCH c.members m1 " +
                    "JOIN FETCH c.members m2 " +
                    "WHERE c.isGroup = false AND m1.user = :u1 AND m2.user = :u2", ChatConversation.class)
                    .setParameter("u1", loggedInUser)
                    .setParameter("u2", friend)
                    .uniqueResult();

                if (chat == null) {
                    chat = new ChatConversation();
                    chat.setGroup(false);
                    chat.setCreatedAt(LocalDateTime.now());
                    session.save(chat);

                    ChatMember m1 = new ChatMember();
                    m1.setChat(chat);
                    m1.setUser(loggedInUser);
                    m1.setJoinedAt(LocalDateTime.now());
                    session.save(m1);

                    ChatMember m2 = new ChatMember();
                    m2.setChat(chat);
                    m2.setUser(friend);
                    m2.setJoinedAt(LocalDateTime.now());
                    session.save(m2);
                }

                // Hiển thị
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/messItem.fxml"));
                AnchorPane item = loader.load();
                MessItemController controller = loader.getController();

                ChatMessage preview = new ChatMessage();
                preview.setChat(chat);
                preview.setSender(friend);
                preview.setContent("Start chatting...");
                preview.setSentAt(LocalDateTime.now());

                controller.setData(preview, loggedInUser, this);
                messList.getChildren().add(item);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        session.getTransaction().commit();
        session.close();
    }

    public void loadGroupChats() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();

        List<ChatConversation> groups = session.createQuery(
            "SELECT DISTINCT c FROM ChatConversation c " +
            "JOIN FETCH c.members m " +
            "WHERE c.isGroup = true AND m.user.id = :uid", ChatConversation.class)
            .setParameter("uid", loggedInUser.getId())
            .getResultList();

        for (ChatConversation chat : groups) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/messItem.fxml"));
                AnchorPane item = loader.load();
                MessItemController controller = loader.getController();

                ChatMessage preview = new ChatMessage();
                preview.setChat(chat);
                preview.setSender(loggedInUser); // người gửi không quan trọng ở đây
                preview.setContent("Nhóm: " + chat.getGroupName());
                preview.setSentAt(LocalDateTime.now());

                controller.setData(preview, loggedInUser, this);
                messList.getChildren().add(item);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        session.getTransaction().commit();
        session.close();
    }

    @FXML
    private void handleCreateGroupClick() {
        Dialog<Pair<String, List<User>>> dialog = new Dialog<>();
        dialog.setTitle("Tạo nhóm mới");

        // Nút OK / Cancel
        ButtonType createButtonType = new ButtonType("Tạo nhóm", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // Giao diện nhập tên + chọn bạn
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField groupNameField = new TextField();
        groupNameField.setPromptText("Nhập tên nhóm");

        ListView<CheckBox> friendListView = new ListView<>();
        List<User> acceptedFriends = getAcceptedFriends(loggedInUser);
        for (User friend : acceptedFriends) {
            CheckBox cb = new CheckBox(friend.getUsername());
            cb.setUserData(friend); // gắn đối tượng User vào checkbox
            friendListView.getItems().add(cb);
        }

        friendListView.setPrefHeight(200);

        grid.add(new Label("Tên nhóm:"), 0, 0);
        grid.add(groupNameField, 1, 0);
        grid.add(new Label("Chọn thành viên:"), 0, 1);
        grid.add(friendListView, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Kết quả khi bấm "Tạo nhóm"
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                List<User> selected = new ArrayList<>();
                for (CheckBox cb : friendListView.getItems()) {
                    if (cb.isSelected()) {
                        selected.add((User) cb.getUserData());
                    }
                }
                return new Pair<>(groupNameField.getText(), selected);
            }
            return null;
        });

        Optional<Pair<String, List<User>>> result = dialog.showAndWait();

        result.ifPresent(groupInfo -> {
            String groupName = groupInfo.getKey();
            List<User> selectedUsers = groupInfo.getValue();

            if (groupName == null || groupName.trim().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Vui lòng nhập tên nhóm!");
                alert.showAndWait();
                return;
            }

            List<Integer> memberIds = selectedUsers.stream().map(User::getId).collect(Collectors.toList());
            memberIds.add(loggedInUser.getId()); // Thêm chính mình

            try {
                // 1. Gửi request tạo nhóm
                chatClient.createGroupChat(groupName, memberIds);

                // 2. ⏱️ Chờ một chút rồi load lại danh sách (gợi ý: 300ms)
                new Thread(() -> {
                    try {
                        Thread.sleep(300); // Đợi server xử lý tạo nhóm xong
                        Platform.runLater(() -> {
                            loadFriendList(); // ✅ Load lại danh sách bạn bè + nhóm để hiển thị nhóm mới
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void handleMessengerClick(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Community.fxml"));
            Parent root = loader.load();
            CommunityController communityController = loader.getController();
            communityController.setLoggedInUser(loggedInUser);

            Stage stage = (Stage) MessengerLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Cannot switch to Community (Messenger)");
        }
    }

    public void handleMyFriendsClick(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/FindFriend.fxml"));
            Parent root = loader.load();
            FindFriendController findFriendController = loader.getController();
            findFriendController.setLoggedInUser(loggedInUser);

            Stage stage = (Stage) MyFriendsLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Cannot reload FindFriend.fxml");
        }
    }

    public void loadMessages(ChatConversation chat, User currentUser) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<ChatMessage> messages = session
            .createQuery("FROM ChatMessage WHERE chat.chatId = :chatId ORDER BY sentAt", ChatMessage.class)
            .setParameter("chatId", chat.getChatId())
            .list();
        session.close();

        MessPane.getChildren().clear(); // Xóa nội dung cũ

        for (ChatMessage msg : messages) {
            try {
                FXMLLoader loader;
                AnchorPane bubble;

                if (msg.getType().equals("image")) {
                    // 🖼️ Nếu là tin nhắn hình ảnh
                    if (msg.getSender().getId() == currentUser.getId()) {
                        loader = new FXMLLoader(getClass().getResource("/fxml/imageBubble_right.fxml"));
                        bubble = loader.load();
                        ImageBubbleRightController controller = loader.getController();
                        controller.setData(msg);
                    } else {
                        loader = new FXMLLoader(getClass().getResource("/fxml/imageBubble_left.fxml"));
                        bubble = loader.load();
                        ImageBubbleLeftController controller = loader.getController();
                        controller.setData(msg, msg.getSender());
                    }
                } else {
                    // 💬 Nếu là tin nhắn văn bản
                    if (msg.getSender().getId() == currentUser.getId()) {
                        loader = new FXMLLoader(getClass().getResource("/fxml/textBubble_right.fxml"));
                        bubble = loader.load();
                        TextBubbleRightController controller = loader.getController();
                        controller.setData(msg, currentUser);
                    } else {
                        loader = new FXMLLoader(getClass().getResource("/fxml/textBubble_left.fxml"));
                        bubble = loader.load();
                        TextBubbleLeftController controller = loader.getController();
                        controller.setData(msg, msg.getSender());
                    }
                }

                MessPane.getChildren().add(bubble);
                Platform.runLater(() -> scrollPane.setVvalue(1.0)); // auto scroll xuống dưới

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadMessagesBetweenUsers(User currentUser, User friend) {
        this.selectedFriend = friend;

        FriendName.setText(friend.getUsername());
        if (friend.getAvatar() != null && !friend.getAvatar().isEmpty()) {
            File avatarFile = new File(friend.getAvatar());
            if (avatarFile.exists()) {
                FriendAvatar.setImage(new Image(avatarFile.toURI().toString()));
            }
        }

        Session session = HibernateUtil.getSessionFactory().openSession();

        List<ChatConversation> results = session.createQuery(
            "SELECT DISTINCT c FROM ChatConversation c " +
            "JOIN c.members m1 " +
            "JOIN c.members m2 " +
            "WHERE m1.user = :user1 AND m2.user = :user2 AND c.isGroup = false", ChatConversation.class)
            .setParameter("user1", currentUser)
            .setParameter("user2", friend)
            .getResultList();

        ChatConversation chat = results.isEmpty() ? null : results.get(0);

        session.close();

        if (chat == null) {
            MessPane.getChildren().clear();
            return;
        }

        this.selectedChat = chat;
        try {
            chatClient.joinChat(chat.getChatId());
        } catch (IOException e) {
            e.printStackTrace();
        }

        loadMessages(chat, currentUser);
    }

    public void loadGroupMessages(ChatConversation chat, User currentUser) {
        this.selectedChat = chat;

        try {
            chatClient.joinChat(chat.getChatId());
        } catch (IOException e) {
            e.printStackTrace();
        }

        FriendName.setText(chat.getGroupName() != null ? chat.getGroupName() : "Group Chat");
        FriendAvatar.setImage(null); // Có thể set group avatar

        Session session = HibernateUtil.getSessionFactory().openSession();
        List<ChatMessage> messages = session
            .createQuery("FROM ChatMessage WHERE chat.chatId = :chatId ORDER BY sentAt", ChatMessage.class)
            .setParameter("chatId", chat.getChatId())
            .list();
        session.close();

        MessPane.getChildren().clear(); // ✅ Xóa đoạn chat cũ

        for (ChatMessage msg : messages) {
            try {
                FXMLLoader loader;
                AnchorPane bubble;

                if ("image".equals(msg.getType())) {
                    loader = new FXMLLoader(getClass().getResource(
                        msg.getSender().getId() == currentUser.getId()
                            ? "/fxml/imageBubble_right.fxml"
                            : "/fxml/imageBubble_left.fxml"));
                    bubble = loader.load();

                    if (msg.getSender().getId() == currentUser.getId()) {
                        loader.<ImageBubbleRightController>getController().setData(msg);
                    } else {
                        loader.<ImageBubbleLeftController>getController().setData(msg, msg.getSender());
                    }

                } else {
                    loader = new FXMLLoader(getClass().getResource(
                        msg.getSender().getId() == currentUser.getId()
                            ? "/fxml/textBubble_right.fxml"
                            : "/fxml/textBubble_left.fxml"));
                    bubble = loader.load();

                    if (msg.getSender().getId() == currentUser.getId()) {
                        loader.<TextBubbleRightController>getController().setData(msg, currentUser);
                    } else {
                        loader.<TextBubbleLeftController>getController().setData(msg, msg.getSender());
                    }
                }

                MessPane.getChildren().add(bubble); // ✅ add vào MessPane
                Platform.runLater(() -> scrollPane.setVvalue(1.0));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleSendByEnter() {
        String content = MessContent.getText().trim();
        if (content.isEmpty() || selectedChat == null) return;

        try {
            // ✅ Gửi tin nhắn có kèm avatar
            chatClient.sendMessage(
                loggedInUser.getUsername(),
                content,
                selectedChat.getChatId(),
                loggedInUser.getAvatar()  // Thêm avatar ở đây
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Lưu vào DB
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();

        ChatMessage msg = new ChatMessage();
        msg.setChat(selectedChat);
        msg.setSender(loggedInUser);
        msg.setContent(content);
        msg.setType("text");
        msg.setSentAt(LocalDateTime.now());

        session.save(msg);
        session.getTransaction().commit();
        session.close();

        // ❌ KHÔNG hiển thị ở đây nữa, vì sẽ được onMessageReceived() hiển thị lại
        MessContent.clear(); // Chỉ cần xóa nội dung
    }

    @FXML
    private void handleSendFile() {
        if (selectedChat == null) {
            showAlert("Warning", "Please select a chat first!");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Send");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                // Kiểm tra kích thước file (ví dụ max 10MB)
                long maxSize = 10 * 1024 * 1024;
                if (file.length() > maxSize) {
                    showAlert("Error", "File size cannot exceed 10MB!");
                    return;
                }

                String type = isImage(file) ? "image" : "file";
                String tempDir = type.equals("image") ? "temp_images" : "temp_files";
                Files.createDirectories(Paths.get(tempDir));

                // Tạo tên file độc nhất
                String fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "_" + file.getName();
                String tempPath = tempDir + "/" + fileName;

                // Sao chép file vào thư mục tạm
                Files.copy(file.toPath(), Paths.get(tempPath), StandardCopyOption.REPLACE_EXISTING);

                // Gửi qua socket
                chatClient.sendFile(
                    loggedInUser.getUsername(),
                    new File(tempPath),
                    type,
                    selectedChat.getChatId(),
                    loggedInUser.getAvatar()
                );

                // Tạo tin nhắn tạm
                ChatMessage tempMsg = new ChatMessage();
                tempMsg.setContent(fileName);
                tempMsg.setFilePath(tempPath);
                tempMsg.setType(type);
                tempMsg.setSentAt(LocalDateTime.now());
                tempMsg.setSender(loggedInUser);
                tempMsg.setChat(selectedChat);

                // Hiển thị bubble
                FXMLLoader loader;
                AnchorPane bubble;
                
                if (type.equals("image")) {
                    loader = new FXMLLoader(getClass().getResource("/fxml/imageBubble_right.fxml"));
                    bubble = loader.load();
                    ImageBubbleRightController controller = loader.getController();
                    controller.setData(tempMsg);
                } else {
                    loader = new FXMLLoader(getClass().getResource("/fxml/fileBubble_right.fxml"));
                    bubble = loader.load();
                    FileBubbleRightController controller = loader.getController();
                    controller.setData(tempMsg);
                }
                
                MessPane.getChildren().add(bubble);
                scrollToBottom();

                // Lưu vào database
                Session session = HibernateUtil.getSessionFactory().openSession();
                session.beginTransaction();
                session.save(tempMsg);
                session.getTransaction().commit();
                session.close();

            } catch (IOException e) {
                showAlert("Error", "Failed to send file: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private boolean isImage(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".gif");
    }
    
    private boolean isSending = false; // Thêm biến kiểm tra trạng thái gửi

    @FXML
    private void handleSendImage() {
        if (isSending || selectedChat == null) return;
        
        isSending = true;
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Image to Send");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );

            File file = fileChooser.showOpenDialog(null);
            if (file != null) {
                // Giới hạn kích thước file
                if (file.length() > 20 * 1024 * 1024) {
                    showAlert("Error", "Image size cannot exceed 20MB!");
                    return;
                }

                // Tạo file tạm với tên độc nhất
                String tempDir = "temp_images";
                Files.createDirectories(Paths.get(tempDir));
                String fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")) + "_" + file.getName();
                String tempPath = tempDir + "/" + fileName;

                // Resize ảnh nếu cần
                File resizedFile = resizeImageIfNeeded(file, tempPath, 950, 950);

                // Gửi file
                chatClient.sendFile(
                    loggedInUser.getUsername(),
                    resizedFile,
                    "image",
                    selectedChat.getChatId(),
                    loggedInUser.getAvatar()
                );

                // Tạo tin nhắn tạm
                ChatMessage tempMsg = new ChatMessage();
                tempMsg.setContent(fileName);
                tempMsg.setFilePath(resizedFile.getAbsolutePath());
                tempMsg.setType("image");
                tempMsg.setSentAt(LocalDateTime.now());
                tempMsg.setSender(loggedInUser);
                tempMsg.setChat(selectedChat);

                // Hiển thị
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/imageBubble_right.fxml"));
                AnchorPane bubble = loader.load();
                ImageBubbleRightController controller = loader.getController();
                controller.setData(tempMsg);

                MessPane.getChildren().add(bubble);
                scrollToBottom();
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to send image: " + e.getMessage());
            e.printStackTrace();
        } finally {
            isSending = false;
        }
    }

    private File resizeImageIfNeeded(File inputFile, String outputPath, int maxWidth, int maxHeight) throws IOException {
        BufferedImage originalImage = ImageIO.read(inputFile);
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        if (width <= maxWidth && height <= maxHeight) {
            // Không cần resize
            Files.copy(inputFile.toPath(), Paths.get(outputPath), StandardCopyOption.REPLACE_EXISTING);
            return new File(outputPath);
        }

        // Resize ảnh theo tỉ lệ
        double scale = Math.min((double) maxWidth / width, (double) maxHeight / height);
        int newWidth = (int) (width * scale);
        int newHeight = (int) (height * scale);

        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        File output = new File(outputPath);
        ImageIO.write(resizedImage, "jpg", output);
        return output;
    }



    private void scrollToBottom() {
        Platform.runLater(() -> {
            scrollPane.setVvalue(1.0);
            // Cách khác đảm bảo scroll chính xác hơn
            scrollPane.applyCss();
            scrollPane.layout();
            scrollPane.setVvalue(scrollPane.getVmax());
        });
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}