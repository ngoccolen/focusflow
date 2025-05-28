
package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Task;
import model.User;

import org.hibernate.Session;
import org.hibernate.Transaction;
import Util.HibernateUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.control.Alert;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TaskController {

    @FXML private Button addButton, removeButton;
    @FXML private DatePicker datePicker;
    @FXML private ImageView resetImage;
    @FXML private VBox taskContainer;
    private Task selectedTask;
    private User loggedInUser;
    
    public void handleAddClick(MouseEvent event) {
        addTask();
    }
    private final List<TaskItemController> taskItemControllers = new java.util.ArrayList<>();
    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
        loadTasks(null); 
        // Lấy toàn bộ task từ DB để kiểm tra deadline
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Task> allTasks = session.createQuery("FROM Task", Task.class).getResultList();
        session.close();
        startDeadlineChecker(allTasks); 
    }

    @FXML
    public void initialize() {
        datePicker.setOnAction(event -> loadTasks(datePicker.getValue()));
    }

    private void addTask() {
        if (loggedInUser == null) return; 
        Task newTask = new Task();
        newTask.setTitle("New Task");
        newTask.setCreated_at(java.time.LocalDateTime.now());
        // Gán user_id dựa trên loggedInUser
        newTask.setUser_id(loggedInUser.getId());
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        session.save(newTask);
        tx.commit();
        session.close();
        loadTasks(datePicker.getValue());
    }

    public void removeTask(Task task) {
        if (task == null) return;
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        session.remove(task);
        tx.commit();
        session.close();

        loadTasks(datePicker.getValue());
    }
    
    public void handleRemoveClick(MouseEvent event) {
        if (selectedTask == null) {
            System.out.println("No task selected to remove.");
            return;
        }
        removeTask(selectedTask);
        selectedTask = null;
    }
    
    public void handleResetClick() {
        datePicker.setValue(null);     
        loadTasks(null);              
    }

    public void loadTasks(LocalDate filterDate) {
        taskContainer.getChildren().clear();
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Task> tasks = session.createQuery("FROM Task", Task.class).getResultList();
        session.close();

        if (filterDate != null) {
            tasks = tasks.stream()
                    .filter(t -> filterDate.equals(t.getDate()))
                    .collect(Collectors.toList());
        }

        tasks.sort(
        	    Comparator
        	        .comparing(Task::isCompleted)
        	        // Sau đó sắp xếp theo start_time tăng dần, null xuống dưới
        	        .thenComparing(Task::getStart_time, Comparator.nullsLast(Comparator.naturalOrder()))
        	);

       taskItemControllers.clear(); 

    for (Task task : tasks) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/taskItem.fxml"));
            Node taskNode = loader.load();

            TaskItemController itemController = loader.getController();
            itemController.setTask(task);
            itemController.setOnEdit(this::editTask);
            itemController.setOnSelect(() -> {
                selectedTask = task;
                // Reset tất cả taskItem về trạng thái bình thường
                for (TaskItemController c : taskItemControllers) {
                    c.setSelected(false);
                }
                // Highlight task được chọn
                itemController.setSelected(true);
            });
            taskNode.setUserData(loader);
            taskItemControllers.add(itemController);
            taskContainer.getChildren().add(taskNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }}

    private void editTask(Task task) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/taskEdit.fxml")); 
            Parent root = loader.load();
            EditTaskController controller = loader.getController();
            controller.setTask(task);
            controller.setOnSave(() -> {
                loadTasks(datePicker.getValue()); // Refresh sau khi lưu
            });

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Edit Task");
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void setHighlightedTask(Task taskToHighlight) {
        Platform.runLater(() -> {
            datePicker.setValue(null);      
            loadTasks(null);               

            Platform.runLater(() -> {
                for (TaskItemController c : taskItemControllers) {
                    Task t = c.getTask();
                    // Reset style tất cả task về mặc định
                    c.setSelected(false);
                    // Nếu là task cần highlight → set màu nổi bật
                    if (t.getTask_id() == taskToHighlight.getTask_id()) {
                        c.setSelected(true); // highlight bằng màu, border
                    }
                }
            });
        });
    }

    private void checkUpcomingDeadlines(List<Task> tasks) {
        LocalDateTime now = LocalDateTime.now();

        for (Task task : tasks) {
            if (task.isCompleted() || task.isReminded()) continue;

            // Nếu người dùng chọn remind_at → dùng cái đó
            if (task.getRemindAt() != null) {
                long mins = Duration.between(now, task.getRemindAt()).toMinutes();
                if (mins >= 0 && mins <= 1) {
                    showDeadlinePopup(task);
                }
            } else if (task.getDeadline() != null) {
                long mins = Duration.between(now, task.getDeadline()).toMinutes();
                if (mins >= 0 && mins <= 10) {
                    showDeadlinePopup(task);
                }
            }
        }
    }

    private void showDeadlinePopup(Task task) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("⏰ Deadline Reminder");
            alert.setHeaderText("Task \"" + task.getTitle() + "\" is due soon!");
            alert.setContentText("Do you want to be reminded again later?");

            ButtonType remindLater = new ButtonType("Remind me later");
            ButtonType dismiss = new ButtonType("Dismiss", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(remindLater, dismiss);

            alert.showAndWait().ifPresent(response -> {
                if (response == remindLater) {
                    task.setRemindAt(LocalDateTime.now().plusMinutes(10));
                    task.setReminded(false); 
                } else {
                    task.setReminded(true); 
                }

                try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                    Transaction tx = session.beginTransaction();
                    session.merge(task);
                    tx.commit();
                }
            });
        });
    }
    private ScheduledExecutorService scheduler;

    public void startDeadlineChecker(List<Task> taskList) {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            checkUpcomingDeadlines(taskList);
        }, 0, 1, TimeUnit.MINUTES); // Kiểm tra mỗi 1 phút
    }
    public void stopDeadlineChecker() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }

}
