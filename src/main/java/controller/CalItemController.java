package controller;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import java.util.function.Consumer;
import org.hibernate.Session;
import org.hibernate.Transaction;
import Util.HibernateUtil;
import model.Task;
import javafx.scene.control.Tooltip;
import java.time.format.DateTimeFormatter;
public class CalItemController {
	@FXML private CheckBox taskCheckBox;
	@FXML private AnchorPane calendarItemPane;

	private Task task;
	private Consumer<Task> onSelect;
	private Runnable onUpdated;
    @FXML
    public void initialize() {
        // Hover effect
        calendarItemPane.setOnMouseEntered(e -> calendarItemPane.setOpacity(0.85));
        calendarItemPane.setOnMouseExited(e -> calendarItemPane.setOpacity(1.0));
    }
	public void setTask(Task task) {
	    this.task = task;
	    taskCheckBox.setText(task.getTitle());
	    taskCheckBox.setSelected(task.isCompleted());
	       // Giao diện theo trạng thái
        updateStyleBasedOnCompletion(task.isCompleted());
        // Tooltip thông tin chi tiết
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        //DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        StringBuilder tooltipText = new StringBuilder();
        tooltipText.append("📌 Title: ").append(task.getTitle()).append("\n");
        tooltipText.append("📅 Date: ").append(task.getDate().format(dateFormatter)).append("\n");
        tooltipText.append("⏰ Start: ").append(task.getStart_time().format(timeFormatter)).append("\n");

        if (task.getEnd_time() != null) {
            tooltipText.append("🛑 End: ").append(task.getEnd_time().format(timeFormatter)).append("\n");
        }

        if (task.getDeadline() != null) {
            tooltipText.append("⚠️ Deadline: ").append(task.getDeadline().format(dateTimeFormatter));
        }


        Tooltip tooltip = new Tooltip(tooltipText.toString());
        Tooltip.install(calendarItemPane, tooltip);
	}

	public void setOnSelect(Consumer<Task> onSelect) {
	    this.onSelect = onSelect;
	}

	public void setOnUpdated(Runnable onUpdated) {
	    this.onUpdated = onUpdated;
	}

	@FXML
	public void handleCheck() {
	    if (task != null) {
	        task.setCompleted(taskCheckBox.isSelected());

	        // cập nhật vào DB
	        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
	            Transaction tx = session.beginTransaction();
	            session.merge(task);
	            tx.commit();
	        }
	        // Cập nhật lại style ngay sau khi lưu
	        updateStyleBasedOnCompletion(task.isCompleted());
	     // Cập nhật giao diện
	        if (onUpdated != null) onUpdated.run(); // gọi lại giao diện task.fxml nếu cần
	    }
	}
	private void updateStyleBasedOnCompletion(boolean isDone) {
	    if (isDone) {
	        calendarItemPane.setStyle("-fx-background-color: #dcedc8; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-effect: dropshadow(gaussian, #a5d6a7, 4, 0, 0, 2);");
	    } else {
	        calendarItemPane.setStyle("-fx-background-color: #fff9c4; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-effect: dropshadow(gaussian, #fbc02d, 4, 0, 0, 2);");
	    }
	}

	@FXML
	public void handleSelect(MouseEvent e) {
	    if (onSelect != null && task != null) {
	        onSelect.accept(task); // truyền task để hiển thị trong task.fxml
	    }
	}

}
