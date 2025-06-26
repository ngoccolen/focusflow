package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import model.User;
import model.Friendship;
import org.hibernate.Session;

import Util.HibernateUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;
import javafx.scene.control.Label;

public class FindFriendController {
    @FXML private ImageView DashboardIcon;
    @FXML private ImageView LogoutIcon;
    @FXML private ImageView avatarIcon;
    @FXML
    private Label MessengerLabel;
    @FXML
    private Label MyFriendsLabel;
    @FXML
    private AnchorPane FriendRequestPane;
    @FXML
    private AnchorPane FindFriendPane;
    @FXML
    private GridPane FindFriendContainer;

    private User loggedInUser;
    
    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
        if (user.getAvatar() != null) {
            File avatarFile = new File(user.getAvatar());
            if (avatarFile.exists()) {
                avatarIcon.setImage(new Image(avatarFile.toURI().toString()));
            }
        }
        loadAvailableUsers();
    }
    
    public void initialize() {
        DashboardIcon.setCursor(Cursor.HAND);
        LogoutIcon.setCursor(Cursor.HAND);
        MessengerLabel.setCursor(Cursor.HAND);  
        MyFriendsLabel.setCursor(Cursor.HAND);
        FriendRequestPane.setCursor(Cursor.HAND);
        FindFriendPane.setCursor(Cursor.HAND);
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
    
    public void handleFriendRequestClick(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/FriendRequest.fxml"));
            Parent root = loader.load();
            FriendRequestController controller = loader.getController();
            controller.setLoggedInUser(loggedInUser);
            
            Stage stage = (Stage) FriendRequestPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Cannot switch to FriendRequest.fxml");
        }
    }
    
    public void handleFindFriendClick(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/FindFriend.fxml"));
            Parent root = loader.load();
            FindFriendController controller = loader.getController();
            controller.setLoggedInUser(loggedInUser);

            Stage stage = (Stage) FindFriendPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Cannot switch to FindFriend.fxml");
        }
    }

    private void loadAvailableUsers() {
        Session session = HibernateUtil.getSessionFactory().openSession();

        String hql =
                "FROM User u " +
                "WHERE u.id != :currentId " +
                "AND u.id NOT IN (" +
                "   SELECT f.friend.id FROM Friendship f " +
                "   WHERE f.user.id = :currentId AND f.status IN ('pending', 'accepted') " +
                "   UNION " +
                "   SELECT f.user.id FROM Friendship f " +
                "   WHERE f.friend.id = :currentId AND f.status IN ('pending', 'accepted')" +
                ")";

        List<User> potentialFriends = session.createQuery(hql, User.class)
                .setParameter("currentId", loggedInUser.getId())
                .list();

        session.close();

        FindFriendContainer.getChildren().clear();

        int column = 0;
        int row = 0;

        for (User user : potentialFriends) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/FindFriendItem.fxml"));
                AnchorPane item = loader.load();

                controller.FindFriendItemController itemController = loader.getController();
                itemController.setData(user, loggedInUser);

                FindFriendContainer.add(item, column, row);
                column++;
                if (column == 2) {
                    column = 0;
                    row++;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}