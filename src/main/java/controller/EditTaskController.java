package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Task;
import org.hibernate.Session;
import org.hibernate.Transaction;
import Util.HibernateUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class EditTaskController {

    @FXML private TextField titleField, startTimeField, endTimeField, deadlineTimeField;
    @FXML private CheckBox completeCheckBox; 
    @FXML private DatePicker datePicker, deadlineDatePicker;
    @FXML private Button saveButton;
    private Task task;
    private Runnable onSave;

    public void setTask(Task task) {
        this.task = task;
        if (task.getTitle() != null) titleField.setText(task.getTitle());
        if (task.getDate() != null) datePicker.setValue(task.getDate());
        if (task.getStart_time() != null) startTimeField.setText(task.getStart_time().toString());
        if (task.getEnd_time() != null) endTimeField.setText(task.getEnd_time().toString());
        if (task.getDeadline() != null) {
            deadlineDatePicker.setValue(task.getDeadline().toLocalDate());
            deadlineTimeField.setText(task.getDeadline().toLocalTime().toString());
        }
        // Nếu task có trường completed
        if (completeCheckBox != null ) {
            completeCheckBox.setSelected(task.isCompleted());
        }
    }

    public void setOnSave(Runnable onSave) {
        this.onSave = onSave;
    }

    @FXML
    public void handleSave() {
        task.setTitle(titleField.getText());
        task.setDate(datePicker.getValue());
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        try {
            if (!startTimeField.getText().isBlank()) {
                task.setStart_time(LocalTime.parse(startTimeField.getText().trim(), timeFormatter));
            }
        } catch (Exception e) {
            showAlert("Giờ bắt đầu không hợp lệ", "Vui lòng nhập đúng định dạng HH:mm");
            return;
        }
        try {
            if (!endTimeField.getText().isBlank()) {
                task.setEnd_time(LocalTime.parse(endTimeField.getText().trim(), timeFormatter));
            }
        } catch (Exception e) {
            showAlert("Giờ kết thúc không hợp lệ", "Vui lòng nhập đúng định dạng HH:mm");
            return;
        }
        try {
        	if (deadlineDatePicker.getValue() != null) {
        	    LocalDate deadlineDate = deadlineDatePicker.getValue();
        	    // Nếu người dùng có nhập giờ → dùng giờ đó
        	    if (deadlineTimeField != null && !deadlineTimeField.getText().isBlank()) {
        	        try {
        	            LocalTime deadlineTime = LocalTime.parse(deadlineTimeField.getText().trim(), timeFormatter);
        	            task.setDeadline(LocalDateTime.of(deadlineDate, deadlineTime));
        	        } catch (Exception e) {
        	            showAlert("Giờ deadline không hợp lệ", "Vui lòng nhập đúng định dạng HH:mm");
        	            return;
        	        }
        	    } else {
        	        // Không nhập giờ → mặc định là 23:59
        	        task.setDeadline(LocalDateTime.of(deadlineDate, LocalTime.of(23, 59)));
        	    }
        	} else {
        	    task.setDeadline(null);
        	}

        } catch (Exception e) {
            showAlert("Ngày deadline không hợp lệ", "Vui lòng chọn ngày");
            return;
        }
        // Nếu có checkbox hoàn thành
        if (completeCheckBox != null) {
            task.setCompleted(completeCheckBox.isSelected());
        }

        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        session.merge(task);
        tx.commit();
        session.close();
        if (onSave != null) onSave.run();
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
