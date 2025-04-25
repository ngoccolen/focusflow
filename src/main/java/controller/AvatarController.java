package controller;

import java.io.File;

import org.hibernate.Session;

import Util.HibernateUtil;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.User;

public class AvatarController {
    @FXML private ImageView avatarIcon;
    @FXML private ImageView editIcon;
    private User user;

    public void handleEditClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh đại diện");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Ảnh", "*.png", "*.jpg", "*.jpeg")
        );

        Stage stage = (Stage) editIcon.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
        	avatarIcon.setImage(new Image(file.toURI().toString()));
            
            // Lưu đường dẫn ảnh vào database
            user.setAvatar(file.getAbsolutePath());
            
            Session session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            session.update(user); // hoặc session.saveOrUpdate(user);
            session.getTransaction().commit();
            session.close();
        }
    }
}
