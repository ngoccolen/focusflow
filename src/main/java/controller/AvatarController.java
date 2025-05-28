package controller;

import java.io.File;

import org.hibernate.Session;

import Util.HibernateUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.User;

public class AvatarController {
    @FXML private ImageView avatarIcon, editIcon;
    @FXML private Label nameLabel, emailLabel;
    private DashboardController dashboardController; //Tham chiếu đến dashboard, dùng để cập nhật ảnh avatar trong dashboard
    private User user; //Khai báo người dùng hiện tại
    
    //nhấn nút chỉnh ảnh đại diện
    public void handleEditClick() {
    	//Mở hộp thoại chọn file
        FileChooser fileChooser = new FileChooser();
        //chọn định dạng .png, .jpg, .jpeg
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Ảnh", "*.png", "*.jpg", "*.jpeg")
        );
        
        //Ép kiểu từ Window về Stage (vì getWindow() trả về Window, nhưng showOpenDialog yêu cầu Stage làm tham số).
        Stage stage = (Stage) editIcon.getScene().getWindow();
        //Mở hộp thoại để người dùng chọn file
        File file = fileChooser.showOpenDialog(stage);
        
        //khi người dùng chọn 1 file thì lấy đường dẫn của file đó chuyển thành chuỗi để truyền cho constructor của lớp image
        if (file != null) {
        	avatarIcon.setImage(new Image(file.toURI().toString()));
            
            // Lưu đường dẫn ảnh vào thuộc tính avatar của user
            user.setAvatar(file.getAbsolutePath());
            
            //Session: đại diện cho 1 phiên làm việc với csdl, dùng để kết nối và tương tác với csdl, thực hiện các thao tác như lưu, cập nhật, xóa, truy vấn
            //openSession: mở 1 phiên làm việc với csdl
            //getSessionFactory: trả về SessionFactory-đối tượng chịu trách nhiệm tạo ra session
            Session session = HibernateUtil.getSessionFactory().openSession();
            
            //bắt đầu 1 giao dịch (Hibernate yêu cầu mọi thao tác ghi dữ liệu (save, update, delete) phải được thực hiện trong một giao dịch.)
            session.beginTransaction();
            
            //Cập nhật dữ liệu của đối tượng user trong cơ sở dữ liệu.
            session.update(user); 
            
            /*ghi các thay đổi vừa thực hiện xuống csdl. Khi commit() được gọi, 
             * Hibernate sẽ thực thi câu SQL tương ứng (ở đây là câu UPDATE) để cập nhật dữ liệu trong bảng.
             */
            session.getTransaction().commit();
            session.close();
        }
        //cập nhật avatar ngoài giao diện dashboard
        if (dashboardController != null) {
            dashboardController.updateAvatarImage(user.getAvatar());
        }
    }
    
    //Gán đối tượng người dùng hiện tại
    public void setUsername(User user) {
        this.user = user;

        // Cập nhật giao diện avatar
        if (user != null) {
            nameLabel.setText(user.getUsername());
            emailLabel.setText(user.getEmail());
            
            if (user.getAvatar() != null) {
                File avatarFile = new File(user.getAvatar());
                if (avatarFile.exists()) {
                  avatarIcon.setImage(new Image(avatarFile.toURI().toString()));
                }
            }
        }
    }
    
    //thiết lập liên kết giữa AvatarController và DashboardController, cho phép đồng bộ ảnh đại diện trên nhiều phần giao diện.
    public void setDashboardController(DashboardController controller) {
        this.dashboardController = controller;
    }
}
