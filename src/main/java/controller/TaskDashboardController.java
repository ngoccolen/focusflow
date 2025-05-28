package controller;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import model.Task;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TaskDashboardController {

    @FXML private CheckBox completeBox;
    @FXML private Label date;
    @FXML private Label starttimeLabel;
    @FXML private ImageView alertIcon;

    public void setTask(Task task) {
        completeBox.setText(task.getTitle() != null ? task.getTitle() : "Untitled Task");
        completeBox.setSelected(task.isCompleted());

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        if (task.getDate() != null) {
            date.setText(task.getDate().format(dateFormatter));
        } else {
            date.setText("");
        }

        if (task.getStart_time() != null) {
            starttimeLabel.setText(task.getStart_time().format(timeFormatter));
        } else {
            starttimeLabel.setText(" ");
        }

        if (task.getDeadline() != null && task.getDeadline().toLocalDate().equals(LocalDate.now())) {
            alertIcon.setVisible(true);
            Tooltip.install(alertIcon, new Tooltip("Today deadline!"));
        } else {
            alertIcon.setVisible(false);
        }
    }
}

