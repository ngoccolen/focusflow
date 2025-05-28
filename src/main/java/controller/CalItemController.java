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
	private Task task; //lưu thông tin task hiện tại
	private Consumer<Task> onSelect; //Consumer là một đối tượng có thể "tiêu thụ" một giá trị mà bạn truyền vào
	private Runnable onUpdated; //đại diện cho một đoạn mã (task) mà không nhận tham số đầu vào và không trả về kết quả
	
    public void initialize() {
    	//hiệu ứng hover khi đưa chuột vào
        calendarItemPane.setOnMouseEntered(e -> calendarItemPane.setOpacity(0.85));
        calendarItemPane.setOnMouseExited(e -> calendarItemPane.setOpacity(1.0));
    }
    
    //cập nhật giao diện của item task trên lịch dựa trên đối tượng Task truyền vào
	public void setTask(Task task) {
	    this.task = task;
	    taskCheckBox.setText(task.getTitle());
	    taskCheckBox.setSelected(task.isCompleted());
	    // Giao diện theo trạng thái
        updateStyleBasedOnCompletion(task.isCompleted());
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
        //Tooltip là một phần tử giao diện nhỏ xuất hiện khi di chuột hoặc giữ chuột trên một thành phần giao diện
        Tooltip tooltip = new Tooltip(tooltipText.toString());
        //Cài tooltip lên toàn bộ vùng calendarItemPane, khi rê chuột vào sẽ hiện ra popup thông tin chi tiết.
        Tooltip.install(calendarItemPane, tooltip);
	}
	
	@FXML
	//Cập nhật trạng thái khi checkbox được tick hoặc bỏ tick
	public void handleCheck() {
	    if (task != null) {
	        task.setCompleted(taskCheckBox.isSelected());

	        // cập nhật vào DB
	        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
	            Transaction tx = session.beginTransaction();
	            //Dùng để cập nhật entity task trong DB.
	            session.merge(task);
	            tx.commit();
	        }
	        // Cập nhật lại style ngay sau khi lưu
	        updateStyleBasedOnCompletion(task.isCompleted());
	        // Cập nhật giao diện
	        if (onUpdated != null) onUpdated.run(); // gọi lại giao diện task.fxml nếu cần
	    }
	}
	
	//cập nhật giao diện khi tick checkbox
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
	public void setOnSelect(Consumer<Task> onSelect) {
	    this.onSelect = onSelect;
	}
	public void setOnUpdated(Runnable onUpdated) {
	    this.onUpdated = onUpdated;
	}


}
