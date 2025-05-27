package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import model.Note;
import model.Task;

import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;

public class NoteItemController {

    @FXML private Label DateLabel;
    @FXML private Label taskLabel;
    @FXML private ImageView taskIcon;
    @FXML private TextArea noteText;
    @FXML private AnchorPane rootPane;

    private Note note;
    private Task task; // có thể null
    private NoteController parentController;
 //   private AnchorPane rootPane; // để tham chiếu node gốc
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
        // ✅ GÁN SỰ KIỆN CLICK TRỰC TIẾP CHO rootPane
        rootPane.setOnMouseClicked(event -> {
            if (parentController != null) {
                parentController.setSelectedNote(this);
                System.out.println("Note được chọn: " + note.getNoteId());
                highlightSelected();
            }
        });
//        rootPane.setOnMouseClicked(event -> {
//            parentController.setSelectedNote(this); // báo cho NoteController biết đang chọn cái này
//        });
    }

    public void setParentController(NoteController parentController) {
        this.parentController = parentController;
    }


    public void setNote(Note note) {
        this.note = note;

        // Format ngày tạo
        if (note.getCreatedAt() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateLabel.setText(note.getCreatedAt().format(formatter));
        } else {
            DateLabel.setText("Unknown");
        }

        // Nội dung ghi chú
        noteText.setText(note.getContent());
        noteText.setEditable(false);

        // ✅ Sửa đoạn này: lấy task từ chính note
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

    
//    @FXML
//    private void initialize() {
//        noteText.setEditable(false);
//        noteText.setOnMouseClicked(event -> {
//            if (event.getClickCount() == 2) {
//                noteText.setEditable(true);
//                noteText.requestFocus();
//            }
//        });
//
//    }


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
        // 1. Reset style tất cả các note về nền mặc định
        for (NoteItemController other : parentController.getAllNoteItemControllers()) {
            other.rootPane.setStyle("-fx-background-color: #98ccf5; -fx-background-radius: 5px;");
        }

        // 2. Đặt style nổi bật cho note được chọn (nền xanh đậm)
        this.rootPane.setStyle("-fx-background-color: #98ccf5; -fx-border-color: #ff6600; -fx-border-width: 2px; -fx-background-radius: 5px;");

    }



}
