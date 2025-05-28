
package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Line;
import model.Task;
import java.time.format.DateTimeFormatter;
public class DeadlineDashboardController {
    @FXML
    private AnchorPane redLinePane;
    @FXML
    private Line redLine;


    public void setTask(Task task) {
        String status = task.isCompleted() ? "✅ Completed" : "❌ Incomplete";

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        StringBuilder tooltipText = new StringBuilder();
        tooltipText.append("📌 Title: ").append(task.getTitle()).append("\n");

        if (task.getDate() != null)
            tooltipText.append("📅 Date: ").append(task.getDate().format(dateFormatter)).append("\n");

        if (task.getStart_time() != null)
            tooltipText.append("⏰ Start: ").append(task.getStart_time().format(timeFormatter)).append("\n");

        if (task.getEnd_time() != null)
            tooltipText.append("🛑 End: ").append(task.getEnd_time().format(timeFormatter)).append("\n");

        if (task.getDeadline() != null)
            tooltipText.append("⚠️ Deadline: ").append(task.getDeadline().format(dateTimeFormatter)).append("\n");

        tooltipText.append("📦 Status: ").append(status).append("\n");

        if (task.getCreated_at() != null)
            tooltipText.append("🕓 Created: ").append(task.getCreated_at().format(dateTimeFormatter));

        Tooltip tooltip = new Tooltip(tooltipText.toString());
        Tooltip.install(redLine, tooltip);
    }
    public AnchorPane getRoot() {
        return redLinePane;
    }
}
