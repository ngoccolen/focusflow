package controller;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import model.Task;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;

public class CalDashboardController {

	@FXML
	private AnchorPane calendarItemPane;
	@FXML
	private CheckBox taskCheckBox;
	private Task task;

	public void setTask(Task task) {
		this.task = task;
		taskCheckBox.setText(task.getTitle());
		taskCheckBox.setSelected(task.isCompleted());

		updateStyleBasedOnCompletion(task.isCompleted());

		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
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

		Tooltip.install(calendarItemPane, new Tooltip(tooltipText.toString()));
	}

	private void updateStyleBasedOnCompletion(boolean isDone) {
		if (isDone) {
			calendarItemPane.setStyle(
					"-fx-background-color: #dcedc8; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-effect: dropshadow(gaussian, #a5d6a7, 4, 0, 0, 2);");
		} else {
			calendarItemPane.setStyle(
					"-fx-background-color: #fff9c4; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-effect: dropshadow(gaussian, #fbc02d, 4, 0, 0, 2);");
		}
	}
}
