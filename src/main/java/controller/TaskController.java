
package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.Cursor;
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

    @FXML
    private Button addButton, removeButton;
    @FXML
    private DatePicker datePicker;
    @FXML
    private ImageView resetImage;
    @FXML
    private VBox taskContainer;


    private Task selectedTask;
    private User loggedInUser;
    @FXML
    private void handleAddClick(javafx.scene.input.MouseEvent event) {
        addTask();
    }
    private final List<TaskItemController> taskItemControllers = new java.util.ArrayList<>();


    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
        loadTasks(null); // Bây giờ gọi mới hợp lý
     // Lấy toàn bộ task từ DB để kiểm tra deadline
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Task> allTasks = session.createQuery("FROM Task", Task.class).getResultList();
        session.close();

        startDeadlineChecker(allTasks); // ✅ GỌI ở đây!
    }

    @FXML
    public void initialize() {
        addButton.setCursor(Cursor.HAND);
        removeButton.setCursor(Cursor.HAND);
        datePicker.setOnAction(event -> loadTasks(datePicker.getValue()));
//        resetImage.setOnMouseClicked(event -> {
//            datePicker.setValue(null);
//            loadTasks(null);
//        });
        resetImage.setCursor(Cursor.HAND);

    }


    private void addTask() {
        if (loggedInUser == null) return; // Đảm bảo user đã đăng nhập

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
    @FXML
    private void handleRemoveClick(javafx.scene.input.MouseEvent event) {
        if (selectedTask == null) {
            System.out.println("No task selected to remove.");
            return;
        }

        removeTask(selectedTask);
        selectedTask = null;
    }
    @FXML
    private void handleResetClick() {
        datePicker.setValue(null);     // ✅ Xoá lọc theo ngày
        loadTasks(null);               // ✅ Load toàn bộ task
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

        //tasks.sort(Comparator.comparing(Task::getStart_time, Comparator.nullsLast(Comparator.reverseOrder())));
        tasks.sort(
        	    Comparator
        	        // Ưu tiên task chưa hoàn thành (false < true)
        	        .comparing(Task::isCompleted)
        	        // Sau đó sắp xếp theo start_time tăng dần, null xuống dưới
        	        .thenComparing(Task::getStart_time, Comparator.nullsLast(Comparator.naturalOrder()))
        	);

    taskItemControllers.clear(); // reset trước mỗi lần load

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
            taskItemControllers.add(itemController); // lưu lại để reset sau
            taskContainer.getChildren().add(taskNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }}


    private void editTask(Task task) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/taskEdit.fxml")); // đường dẫn FXML
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
//    public void setHighlightedTask(Task taskToHighlight) {
//        Platform.runLater(() -> {
//            loadTasks(null); // đảm bảo hiển thị lại
//            for (Node node : taskContainer.getChildren()) {
//                FXMLLoader loader = (FXMLLoader) node.getUserData();
//                TaskItemController item = loader.getController();
//                if (item.getTask().getTask_id() == taskToHighlight.getTask_id()) {
//                    item.highlight();
//                }
//
//            }
//        });
//    }
//    public void setHighlightedTask(Task taskToHighlight) {
//        Platform.runLater(() -> {
//            if (taskToHighlight.getDate() != null) {
//                datePicker.setValue(taskToHighlight.getDate());
//                loadTasks(taskToHighlight.getDate());
//            } else {
//                loadTasks(null);
//            }
//
//            Platform.runLater(() -> {
//                for (Node node : taskContainer.getChildren()) {
//                    FXMLLoader loader = (FXMLLoader) node.getUserData();
//                    if (loader != null) {
//                        TaskItemController item = loader.getController();
//                        if (item.getTask().getTask_id() == taskToHighlight.getTask_id()) {
//                            item.highlight();
//                            break;
//                        }
//                    }
//                }
//            });
//        });
//    }
    public void setHighlightedTask(Task taskToHighlight) {
        Platform.runLater(() -> {
            datePicker.setValue(null);      // ❗ reset bộ lọc ngày
            loadTasks(null);                // ✅ load toàn bộ task

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
            if (task.getDeadline() != null && !task.isCompleted() && !task.isReminded()) {
                long minutesUntilDeadline = Duration.between(now, task.getDeadline()).toMinutes();

                if (minutesUntilDeadline >= 0 && minutesUntilDeadline <= 10) {
                    showDeadlinePopup(task);         // ⏰ Hiện popup
                    task.setReminded(true);          // ✅ Đánh dấu đã nhắc

                    // Cập nhật vào CSDL
                    try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                        Transaction tx = session.beginTransaction();
                        session.merge(task);         // lưu cờ đã nhắc
                        tx.commit();
                    }
                }
            }
        }
    }

    private void showDeadlinePopup(Task task) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("⏰ Deadline Reminder");
            alert.setHeaderText("Task deadline coming soon!");
            alert.setContentText("Task \"" + task.getTitle() + "\" is due in less than 10 minutes!");
            alert.show();
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
