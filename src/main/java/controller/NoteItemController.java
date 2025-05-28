package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import model.Note;
import model.Task;
import java.time.format.DateTimeFormatter;

public class NoteItemController {

    @FXML private Label DateLabel, taskLabel;
    @FXML private ImageView taskIcon;
    @FXML private TextArea noteText;
    @FXML private AnchorPane rootPane;

    private Note note;
    private Task task; // có thể null
    private NoteController parentController;
    private boolean selected = false;

    public AnchorPane getRootPane() {
        return rootPane;
    }

    @FXML
    private void initialize() {
        noteText.setEditable(false);
        noteText.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                noteText.setEditable(true);
                noteText.requestFocus();
            }
        });
        rootPane.setOnMouseClicked(event -> {
            if (parentController != null) {
                parentController.setSelectedNote(this);
                System.out.println("Note được chọn: " + note.getNoteId());
                highlightSelected();
            }
        });
    }

    public void setParentController(NoteController parentController) {
        this.parentController = parentController;
    }


    public void setNote(Note note) {
        this.note = note;
        if (note.getCreatedAt() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateLabel.setText(note.getCreatedAt().format(formatter));
        } else {
            DateLabel.setText("Unknown");
        }
        noteText.setText(note.getContent());
        noteText.setEditable(false);
        Task noteTask = note.getTask();
        if (noteTask != null) {
            taskLabel.setText(noteTask.getTitle());
            taskIcon.setVisible(true);
            taskLabel.setVisible(true);
        } else {
            taskLabel.setText("");
            taskIcon.setVisible(false);
            taskLabel.setVisible(false);
        }
    }

    public String getNoteText() {
        return noteText.getText();
    }

    public Note getNote() {
        return note;
    }
    public void setTask(Task task) {
        this.task = task;
    }
    public void setRootPane(AnchorPane rootPane) {
        this.rootPane = rootPane;
        // Gán sự kiện click ngay khi đã có rootPane
        this.rootPane.setOnMouseClicked(event -> {
            if (parentController != null) {
                parentController.setSelectedNote(this);
                System.out.println("Note được chọn: " + note.getNoteId());
                highlightSelected();
            }
        });
    }
    private void highlightSelected() {
        for (NoteItemController other : parentController.getAllNoteItemControllers()) {
            other.rootPane.setStyle("-fx-background-color: #98ccf5; -fx-background-radius: 5px;");
        }

        this.rootPane.setStyle("-fx-background-color: #98ccf5; -fx-border-color: #ff6600; -fx-border-width: 2px; -fx-background-radius: 5px;");

    }



}
