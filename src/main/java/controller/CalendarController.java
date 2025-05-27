package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import model.Task;
import model.User;
import org.hibernate.Session;
import Util.HibernateUtil;
import java.time.format.TextStyle;
import java.util.Locale;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Comparator;


public class CalendarController {

    @FXML private ImageView dayBefore, dayAfter;
    @FXML private Button todayButton;
    @FXML private Label thuLabel, dayLabel, monthLabel;
    @FXML private VBox timeSlotsVBox;
    private TaskController taskController;
    private LocalDate currentDate = LocalDate.now();
    private User loggedInUser;
    private Stage taskStage;

    @FXML private AnchorPane timePane;       // Bên trái: hiển thị giờ
    @FXML private AnchorPane taskPane;       // Bên phải: hiển thị task
    private List<List<Task>> groupOverlappingTasks(List<Task> tasks) {
        List<List<Task>> groups = new ArrayList<>();

        for (Task task : tasks) {
            boolean added = false;
            for (List<Task> group : groups) {
                for (Task g : group) {
                    if (isOverlapping(task, g)) {
                        group.add(task);
                        added = true;
                        break;
                    }
                }
                if (added) break;
            }
            if (!added) {
                List<Task> newGroup = new ArrayList<>();
                newGroup.add(task);
                groups.add(newGroup);
            }
        }
        return groups;
    }

    public void initialize() {
        dayBefore.setCursor(Cursor.HAND);
        dayAfter.setCursor(Cursor.HAND);
        todayButton.setCursor(Cursor.HAND);
        updateDateLabels();
        generateHourGrid();
    }
    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
        loadTasksForDay(currentDate);
    }

    @FXML
    public void handleDayBeforeClick() {
        currentDate = currentDate.minusDays(1);
        updateDateLabels();
        loadTasksForDay(currentDate);
    }

    @FXML
    public void handleDayAfterClick() {
        currentDate = currentDate.plusDays(1);
        updateDateLabels();
        loadTasksForDay(currentDate);
    }

    @FXML
    public void handleTodayClick() {
        currentDate = LocalDate.now();
        updateDateLabels();
        loadTasksForDay(currentDate);
    }

    private void updateDateLabels() {
        thuLabel.setText(currentDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH)); // Ví dụ: Monday
        dayLabel.setText(String.valueOf(currentDate.getDayOfMonth()));
        monthLabel.setText(currentDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH)); // Ví dụ: May
    }

    private void openTaskWindowAndHighlight(Task taskToHighlight) {
        if (taskController == null || taskStage == null || !taskStage.isShowing()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/task.fxml"));
                Parent root = loader.load();

                taskController = loader.getController();
                taskController.setLoggedInUser(loggedInUser);
                taskController.setHighlightedTask(taskToHighlight); // truyền task cần highlight

                taskStage = new Stage();
                taskStage.setTitle("Task Manager");
                taskStage.setScene(new Scene(root));
                taskStage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // task.fxml đang mở → chỉ cần truyền task để highlight
            taskController.setHighlightedTask(taskToHighlight);
            taskStage.toFront(); // đưa cửa sổ ra trước
        }
    }

    private void loadTasksForDay(LocalDate date) {
        if (loggedInUser == null) return;

        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Task> tasks = session.createQuery(
            "FROM Task WHERE user_id = :userId AND date = :targetDate", Task.class)
            .setParameter("userId", loggedInUser.getId())
            .setParameter("targetDate", date)
            .getResultList();
        session.close();

        taskPane.getChildren().removeIf(node -> !(node instanceof javafx.scene.shape.Line)); // xóa task cũ, giữ lại line


     // Sắp xếp theo start_time
        tasks.sort(Comparator.comparing(Task::getStart_time));

        // Nhóm các task giao nhau
        List<List<Task>> overlapGroups = groupOverlappingTasks(tasks);

        for (List<Task> group : overlapGroups) {
            int size = group.size();
            for (int i = 0; i < size; i++) {
                Task task = group.get(i);
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/calItem.fxml"));
                    AnchorPane taskBlock = loader.load();

                    CalItemController controller = loader.getController();
                    controller.setTask(task);
                    controller.setOnSelect(clickedTask -> openTaskWindowAndHighlight(clickedTask));

                    // Vị trí theo thời gian
                    int startMin = task.getStart_time().getHour() * 60 + task.getStart_time().getMinute();
                    int endMin = task.getEnd_time() != null
                            ? task.getEnd_time().getHour() * 60 + task.getEnd_time().getMinute()
                            : startMin + 30;

                    taskBlock.setLayoutY(startMin);
                    taskBlock.setPrefHeight(endMin - startMin);

                    // Dàn ngang theo group
                    double widthPerTask = 240.0 / size;
                    taskBlock.setLayoutX(i * widthPerTask + 5);
                    taskBlock.setPrefWidth(widthPerTask - 10);

                    taskPane.getChildren().add(taskBlock);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
//     // Thêm đường đỏ thể hiện deadline
//        for (Task task : tasks) {
//            if (task.getDeadline_time() != null) {
//                //int deadlineMin = task.getDeadline_time().getHour() * 60 + task.getDeadline_time().getMinute();
////                Line deadlineLine = new Line(0, 0, 250, 0); // kẻ ngang, rộng 250px
////                deadlineLine.setLayoutY(deadlineMin); // đặt theo phút
////                deadlineLine.setStroke(javafx.scene.paint.Color.RED);
////                deadlineLine.setStrokeWidth(1.5);
////             //   deadlineLine.getStrokeDashArray().addAll(4.0, 4.0); // nếu muốn gạch đứt đoạn
////             // Tooltip cho deadlineLine
////                Tooltip deadlineTooltip = new Tooltip();
////                String tooltipText = "Title: " + task.getTitle() + "\nStatus: " + task.getStatus();
////                deadlineTooltip.setText(tooltipText);
////                Tooltip.install(deadlineLine, deadlineTooltip);
////                taskPane.getChildren().add(deadlineLine);
//                try {
//                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/redline.fxml"));
//                    AnchorPane redLinePane = loader.load();
//
//                    RedlineController controller = loader.getController();
//                    controller.setTask(task);
//
//                    int deadlineMin = task.getDeadline_time().getHour() * 60 + task.getDeadline_time().getMinute();
//
//                    redLinePane.setLayoutY(deadlineMin); // Vị trí theo phút
//                    taskPane.getChildren().add(redLinePane);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//            }
//        }
     // 👉 Gọi vẽ line deadline sau khi vẽ xong calItem
        loadDeadlineForDay(date);

    }
    private void loadDeadlineForDay(LocalDate date) {
        if (loggedInUser == null) return;

        Session session = HibernateUtil.getSessionFactory().openSession();

        // Lấy tất cả task có deadline không null
        List<Task> tasksWithDeadline = session.createQuery(
            "FROM Task WHERE user_id = :userId AND deadline is not null", Task.class)
            .setParameter("userId", loggedInUser.getId())
            .getResultList();

        session.close();

        // Lọc những task có deadline trùng ngày đang xem
        List<Task> matchingDeadlineTasks = tasksWithDeadline.stream()
            .filter(task -> task.getDeadline().toLocalDate().equals(date))
            .toList();

        for (Task task : matchingDeadlineTasks) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/redline.fxml"));
                AnchorPane redLinePane = loader.load();

                RedlineController controller = loader.getController();
                controller.setTask(task); // để set tooltip

                int y = task.getDeadline().toLocalTime().getHour() * 60
                        + task.getDeadline().toLocalTime().getMinute();

                redLinePane.setLayoutY(y);
                taskPane.getChildren().add(redLinePane);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void generateHourGrid() {
        timePane.getChildren().clear();
        taskPane.getChildren().clear();
        taskPane.setPrefHeight(1440); // đủ cho 24h
        for (int hour = 0; hour <= 23; hour++) {
            Label hourLabel = new Label(String.format("%02d:00", hour));
            hourLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");
            hourLabel.setLayoutY(hour * 60); // mỗi giờ = 60px
            hourLabel.setLayoutX(5);
            timePane.getChildren().add(hourLabel);
            

            // đường kẻ mỏng
            javafx.scene.shape.Line line = new javafx.scene.shape.Line();
            line.setStartX(0);
            line.setStartY(hour * 60);
            line.setEndX(250); // chiều dài taskPane
            line.setEndY(hour * 60);
            line.setStyle("-fx-stroke: #e0e0e0;");
            taskPane.getChildren().add(line);
        }
    }
    private boolean isOverlapping(Task a, Task b) {
        return !(a.getEnd_time().isBefore(b.getStart_time()) || a.getStart_time().isAfter(b.getEnd_time()));
    }



}
