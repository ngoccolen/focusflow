package controller;

import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import model.Task;
import org.hibernate.Session;
import org.hibernate.Transaction;
import Util.HibernateUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;



public class TaskItemController {

    @FXML
    private AnchorPane taskItemPane;

    @FXML
    private CheckBox completeBox;

    @FXML
    private ImageView editImage;
    @FXML private Label date;
    @FXML private Label starttimeLabel; // (giữ nguyên nếu em đặt fx:id là vậy)
    @FXML private ImageView alertIcon;
    private Task task;
    private Consumer<Task> onEdit;
    private Runnable onSelect;
    private Consumer<Task> onRemove;
   

    public void setTask(Task task) {
        this.task = task;
        completeBox.setText(task.getTitle() != null ? task.getTitle() : "Untitled Task");

        completeBox.setSelected(task.isCompleted());
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
//        date.setText("📅 " + task.getDate().format(dateFormatter));
//        starttimeLabel.setText("🕒 " + task.getStart_time().format(timeFormatter));

        // Hiển thị ngày
        if (task.getDate() != null) {
            date.setText(task.getDate().format(dateFormatter));
        } else {
            date.setText("");
        }

        // Hiển thị giờ bắt đầu
        if (task.getStart_time() != null) {
            starttimeLabel.setText(task.getStart_time().format(timeFormatter));
        } else {
            starttimeLabel.setText(" ");
        }
     // Hiển thị icon nếu deadline hôm nay
        if (task.getDeadline() != null && task.getDeadline().toLocalDate().equals(LocalDate.now())) {
            alertIcon.setVisible(true);
            Tooltip.install(alertIcon, new Tooltip("Today deadline!"));
        } else {
            alertIcon.setVisible(false);
        }

    }


    public void setOnEdit(Consumer<Task> onEdit) {
        this.onEdit = onEdit;
    }
    
    public void setOnSelect(Runnable onSelect) {
        this.onSelect = onSelect;
    }

    public void setOnRemove(Consumer<Task> onRemove) {
        this.onRemove = onRemove;
    }

    @FXML
    private void initialize() {
        taskItemPane.setOnMouseEntered(e -> taskItemPane.setCursor(Cursor.HAND));
        editImage.setCursor(Cursor.HAND);
        completeBox.setCursor(Cursor.HAND);

//        // ✅ Khi người dùng click checkbox → cập nhật task
//        completeBox.setOnAction(e -> {
//            if (task != null) {
//                task.setCompleted(completeBox.isSelected());
//                // Có thể gọi callback hoặc cập nhật DB ở đây nếu muốn
//            }
//        });
        completeBox.setOnAction(e -> {
            if (task != null) {
                boolean isChecked = completeBox.isSelected();
                task.setCompleted(isChecked);

                // Tùy chọn: cập nhật DB ngay lập tức
                updateTaskCompletion(task);
            }
        });

    }


    @FXML
    private void handleEditClick(MouseEvent event) {
        if (onEdit != null && task != null) {
            onEdit.accept(task); // Gọi controller cha xử lý mở form
            event.consume();     // Ngăn click lan sang AnchorPane
        }
    }


    @FXML
    private void handleSelect(MouseEvent event) {
        if (onSelect != null) {
            onSelect.run();
        }
    }

//    @FXML
//    private void handleRemove(MouseEvent event) {
//        if (onRemove != null && task != null) {
//            onRemove.accept(task);
//            event.consume(); // chặn sự kiện không lan rộng
//        }
//    }
    @FXML
    private void handleRemove(MouseEvent event) {
        // ❌ KHÔNG xoá ở đây nữa!
        event.consume(); // chỉ chặn nếu có dùng icon xoá riêng
    }
    public void setSelected(boolean selected) {
        if (selected) {
            taskItemPane.setStyle("-fx-border-color: #2196F3; -fx-background-color: #e3f2fd; -fx-background-radius: 5px;");
        } else {
            taskItemPane.setStyle("-fx-border-color: #d1d1d1; -fx-background-color: white; -fx-background-radius: 5px;");
        }
    }
    private void updateTaskCompletion(Task task) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.merge(task); // lưu trạng thái completed vào DB
            tx.commit();
        }
    }
    public Task getTask() {
        return task;
    }

//    public void highlight() {
//        taskItemPane.setStyle("-fx-border-color: #f27d5d; -fx-background-color: #fff3ed;"); // hiệu ứng tùy chỉnh
//    }
    public void highlight() {
        taskItemPane.setStyle("-fx-border-color: #f27d5d; -fx-background-color: #fff3e0; -fx-border-radius: 5px;");
    }


}
