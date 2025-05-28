package controller;

import model.Task; 
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;
import javafx.scene.layout.AnchorPane;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Button;

import java.io.IOException;

import org.hibernate.Session;

import model.Note;
import model.User;
import Util.HibernateUtil;
import javafx.scene.control.ComboBox;
import javafx.scene.image.ImageView;

public class NoteController {
	@FXML private Button addButton, removeButton, searchButton;
	@FXML private ImageView resetImage;
    @FXML private VBox NoteContainer;
    private User loggedInUser; 
    private List<NoteItemController> noteItemControllers = new ArrayList<>(); 

    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
        loadTasksForComboBox();
        loadNotes(); 
    }

    @FXML
    private ComboBox<String> pickTask;
    private List<Task> taskList; 
    private NoteItemController selectedNote;
    public void setSelectedNote(NoteItemController controller) {
        this.selectedNote = controller;
    }

    @FXML
    public void handleAddClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/noteItem.fxml"));
            AnchorPane noteItem = loader.load();
            NoteItemController controller = loader.getController();
            // Tạo Note mới
            Note newNote = new Note();
            newNote.setUserId(loggedInUser.getId());
            newNote.setCreatedAt(java.time.LocalDateTime.now());
            newNote.setContent("");
            String selectedTaskTitle = pickTask.getSelectionModel().getSelectedItem();
            if (!"Không liên kết với task".equals(selectedTaskTitle)) {
                Task selectedTask = taskList.stream()
                    .filter(t -> t.getTitle().equals(selectedTaskTitle))
                    .findFirst()
                    .orElse(null);
                newNote.setTask(selectedTask);
            } else {
                newNote.setTask(null);
            }

            controller.setNote(newNote);
            controller.setParentController(this);
            controller.setRootPane(noteItem);
            noteItemControllers.add(controller);

            NoteContainer.getChildren().add(0, noteItem); // thêm lên đầu

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadNotes() {
        NoteContainer.getChildren().clear();
        noteItemControllers.clear();
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Note> notes = session.createQuery(
            "SELECT n FROM Note n LEFT JOIN FETCH n.task WHERE n.user_id = :userId ORDER BY n.created_at DESC", Note.class)
            .setParameter("userId", loggedInUser.getId())
            .getResultList();
        session.close();
        for (Note note : notes) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/noteItem.fxml"));
                AnchorPane notePane = loader.load();
                NoteItemController controller = loader.getController();
                controller.setNote(note);
                controller.setParentController(this);
                controller.setRootPane(notePane);

                noteItemControllers.add(controller);
                NoteContainer.getChildren().add(notePane);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadTasksForComboBox() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Task> tasks = session.createQuery(
                "FROM Task WHERE user_id = :userId", Task.class)
                .setParameter("userId", loggedInUser.getId())
                .getResultList();
        session.close();
        taskList = tasks;
        pickTask.getItems().clear();
        pickTask.getItems().add("Không liên kết với task"); // đại diện cho NULL
        for (Task task : tasks) {
            pickTask.getItems().add(task.getTitle());
        }
        pickTask.getSelectionModel().selectFirst(); // chọn mặc định là không liên kết
    }

    // Khi được gọi từ từng note item
    public void saveNoteFromItem(NoteItemController itemController) {
        Note note = itemController.getNote();
        note.setContent(itemController.getNoteText());

        saveNoteToDatabase(note);
    }

    // Khi nhấn resetImage (nút reset tổng)
    @FXML
    public void handleResetClick() {
        for (NoteItemController controller : noteItemControllers) {
            Note note = controller.getNote();
            note.setContent(controller.getNoteText());
            saveNoteToDatabase(note);
        }
        loadNotes();
    }

    private void saveNoteToDatabase(Note note) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            session.beginTransaction();
            session.merge(note);
            session.getTransaction().commit();
            System.out.println("Đã lưu ghi chú ID " + note.getNoteId());
        } catch (Exception e) {
            e.printStackTrace();
            session.getTransaction().rollback();
        } finally {
            session.close();
        }
    }
    @FXML
    public void handleRemoveClick() {
        if (selectedNote == null) {
            System.out.println("Chưa chọn ghi chú nào để xoá");
            return;
        }

        Note note = selectedNote.getNote();

        // 1. Xoá khỏi database
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            session.beginTransaction();
            session.remove(session.contains(note) ? note : session.merge(note));
            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            session.getTransaction().rollback();
        } finally {
            session.close();
        }

        // 2. Xoá khỏi giao diện
        NoteContainer.getChildren().remove(selectedNote.getRootPane());

        // 3. Xoá khỏi danh sách
        noteItemControllers.remove(selectedNote);
        selectedNote = null;
    }
    public void handleSearchClick() {
        String selectedTaskTitle = pickTask.getSelectionModel().getSelectedItem();

        // Nếu chọn "Không liên kết với task"
        if (selectedTaskTitle.equals("Không liên kết với task")) {
            // Tìm các note không gắn task
            filterNotesByTaskId(null);
        } else {
            // Tìm taskId tương ứng với title
            Task selectedTask = taskList.stream()
                .filter(task -> task.getTitle().equals(selectedTaskTitle))
                .findFirst()
                .orElse(null);

            if (selectedTask != null) {
                filterNotesByTaskId(selectedTask.getTask_id());
            } else {
                System.out.println("Không tìm thấy task.");
            }
        }
    }

    private void filterNotesByTaskId(Integer taskId) {
        NoteContainer.getChildren().clear();
        noteItemControllers.clear();

        Session session = HibernateUtil.getSessionFactory().openSession();

        String hql;
        List<Note> notes;

        if (taskId == null) {
            hql = "SELECT n FROM Note n LEFT JOIN FETCH n.task WHERE n.user_id = :userId AND n.task IS NULL ORDER BY n.created_at DESC";
            notes = session.createQuery(hql, Note.class)
                .setParameter("userId", loggedInUser.getId())
                .getResultList();
        } else {
            hql = "SELECT n FROM Note n LEFT JOIN FETCH n.task WHERE n.user_id = :userId AND n.task.task_id = :taskId ORDER BY n.created_at DESC";
            notes = session.createQuery(hql, Note.class)
                .setParameter("userId", loggedInUser.getId())
                .setParameter("taskId", taskId)
                .getResultList();
        }

        session.close();

        for (Note note : notes) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/noteItem.fxml"));
                AnchorPane notePane = loader.load();

                NoteItemController controller = loader.getController();
                controller.setNote(note);
                controller.setParentController(this);
                controller.setRootPane(notePane);

                noteItemControllers.add(controller);
                NoteContainer.getChildren().add(notePane);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<NoteItemController> getAllNoteItemControllers() {
        return noteItemControllers;
    }


}
