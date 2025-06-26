package controller;

import javafx.scene.control.TextArea; // ✅ đúng
import javafx.scene.control.Tooltip;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import org.hibernate.Session;
import org.hibernate.Transaction;

import DAO.StudyTimeDAO;
import Util.HibernateUtil;
import model.Note;
import model.StudyTime;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import model.Task;                
import org.hibernate.Session;     

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;
import model.User;

public class DashboardController {
	@FXML private ImageView GiaoDienChinhIcon;
	@FXML private ImageView LogoutIcon;
	@FXML private Label welcomeLabel;
	@FXML private ImageView avatarIcon;
	private User loggedInUser;
	private Parent root;
	@FXML private GridPane calendarGrid;
	@FXML private Label monthLabel;
	@FXML private Label yearLabel; 
	@FXML private YearMonth currentYearMonth;
	@FXML private ImageView rightArrow;
	@FXML private ImageView leftArrow;
	@FXML private AnchorPane noteContainer;
	@FXML private TextArea noteText;
	@FXML private Label DateLabel;
	@FXML private Label taskLabel;
	@FXML private ImageView taskIcon;
	@FXML private BarChart<String, Number> studyChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private ComboBox<String> timeChoice;
	@FXML
	private VBox taskContainer;
	@FXML private AnchorPane taskPane;
	@FXML private AnchorPane timePane;
	@FXML private Label monthLabel1;
	@FXML private Label dayLabel;
	@FXML
	private ImageView communityIcon;
	private Note latestNote;
@FXML private Button saveButton;
    private LocalDate currentDate = LocalDate.now();
    private int daysToShow = 7;
    
    private StudyTimeDAO studyTimeDAO = StudyTimeDAO.getInstance();

	public void setRoot(Parent root) {
	    this.root = root;
	}

	public Parent getRoot() {
	    return root;
	}
	public void initialize() {
        if (currentYearMonth == null) {
            currentYearMonth = YearMonth.now();
        }
        updateCalendar();
        setupTimeChoiceComboBox();
        setupChart();

    }
	public void handleGDChinhClick (MouseEvent event){
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/GiaoDienChinh.fxml"));
			Parent root = loader.load();
		    GDChinhController gdchinhController = loader.getController();
		    gdchinhController.setLoggedInUser(this.loggedInUser);
			Stage stage = (Stage) GiaoDienChinhIcon.getScene().getWindow();
			stage.setScene(new Scene(root));
			stage.show();
		} catch (Exception e) {
			e.printStackTrace();
            System.out.println("Cannot switch to Giao Dien Chinh");
		}
	}
	public void handleLogoutClick (MouseEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SignUp.fxml"));
			Parent root = loader.load();
			Stage stage = (Stage) LogoutIcon.getScene().getWindow();
			stage.setScene(new Scene(root));
			stage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
		initialize();
	}
	public void handleCommunityClick(MouseEvent event) {
	    try {
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Community.fxml"));
	        Parent root = loader.load();
	        
	        CommunityController communityController = loader.getController();
	        communityController.setLoggedInUser(this.loggedInUser);
	        
	        Stage stage = (Stage) communityIcon.getScene().getWindow();
	        stage.setScene(new Scene(root));
	        stage.show();
	    } catch (Exception e) {
	        e.printStackTrace();
	        System.out.println("Cannot switch to Community screen");
	    }
	}
	public void setLoggedInUser(User user) {
	    this.loggedInUser = user;
	    updateWelcomeMessage();
        updateAvatarImage(user.getAvatar());
        loadChartData();
	    setLatestNoteToDashboard();
	    loadTasksForDashboard(loggedInUser);
	      generateHourGrid();
	        loadTasksForDay(currentDate);
	        updateSelectedDateDisplay(currentDate);

	}

	private void updateWelcomeMessage() {
        if (loggedInUser != null) {
            welcomeLabel.setText("Hi! " + loggedInUser.getUsername() + ", welcome!");
        }
    }

	public void updateAvatarImage(String path) {
	    if (path != null) {
	        File avatarFile = new File(path);
	        if (avatarFile.exists()) {
	            avatarIcon.setImage(new Image(avatarFile.toURI().toString()));
	        }
	    }
	}
	public void handleAvatarClick() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/hopTrangCaNhan.fxml"));
			Parent root = loader.load();
			AvatarController avatarController = loader.getController();
			avatarController.setUsername(this.loggedInUser);
			avatarController.setDashboardController(this);

			Stage avatarStage = new Stage();
			avatarStage.setScene(new Scene(root));
			avatarStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
		initialize();
	}


	public void updateCalendar() {
	    // Xoá các label ngày cũ (giữ lại dòng tiêu đề nếu có)
	    ObservableList<Node> children = calendarGrid.getChildren();
	    children.removeIf(node -> GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) > 0);

	    LocalDate firstDayOfMonth = currentYearMonth.atDay(1);
	    int daysInMonth = currentYearMonth.lengthOfMonth();
	    int startDayOfWeek = firstDayOfMonth.getDayOfWeek().getValue();

	    // Cập nhật tháng/năm trên giao diện
	    monthLabel.setText(String.valueOf(currentYearMonth.getMonthValue()));
	    yearLabel.setText(String.valueOf(currentYearMonth.getYear()));

	    int col = (startDayOfWeek % 7);
	    int row = 1;
	    for (int day = 1; day <= daysInMonth; day++) {
	        final LocalDate selectedDate = currentYearMonth.atDay(day); // final để dùng trong lambda

	        Label dayLabel = new Label(String.valueOf(day));
	        dayLabel.setCursor(Cursor.HAND);
	        dayLabel.getStyleClass().add("calendar-day"); // 🔥 Thêm class riêng

	        // Nếu là ngày hiện tại → tô màu nền
	        if (selectedDate.equals(currentDate)) {
	            dayLabel.setStyle("-fx-background-color: #a5d6a7; -fx-padding: 5px; -fx-font-size: 14px; -fx-background-radius: 5px;");
	        } else {
	            dayLabel.setStyle("-fx-padding: 5px; -fx-font-size: 14px; -fx-background-radius: 5px;");
	        }

	        dayLabel.setOnMouseClicked(e -> {
	            // ✅ Chỉ reset style của các label là ngày
	            for (Node node : calendarGrid.getChildren()) {
	                if (node instanceof Label && node.getStyleClass().contains("calendar-day")) {
	                    ((Label) node).setStyle("-fx-padding: 5px; -fx-font-size: 14px; -fx-background-radius: 5px;");
	                }
	            }

	            currentDate = selectedDate;
	            updateSelectedDateDisplay(currentDate);
	            loadTasksForDay(currentDate);

	            // Highlight label được chọn
	            dayLabel.setStyle("-fx-background-color: #a5d6a7; -fx-padding: 5px; -fx-font-size: 14px; -fx-background-radius: 5px;");
	        });

	        calendarGrid.add(dayLabel, col, row);

	        col++;
	        if (col == 7) {
	            col = 0;
	            row++;
	        }
	    }
	}

	private void updateSelectedDateDisplay(LocalDate selectedDate) {
	    dayLabel.setText(String.format("%02d", selectedDate.getDayOfMonth()));
	    //monthLabel1.setText(String.format("%02d", selectedDate.getMonthValue()));
	    monthLabel1.setText(
	            selectedDate.getMonth().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH)
	        );
	}

    @FXML
    public void handleLeftClick() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        updateCalendar();
    }

    @FXML
    public void handleRightClick() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        updateCalendar();
    }

    private void setLatestNoteToDashboard() {
        Session session = HibernateUtil.getSessionFactory().openSession();

        latestNote = session.createQuery(
                "SELECT n FROM Note n LEFT JOIN FETCH n.task WHERE n.user_id = :userId ORDER BY n.created_at DESC",
                Note.class)
            .setParameter("userId", loggedInUser.getId())
            .setMaxResults(1)
            .uniqueResult();

        session.close();

        if (latestNote == null) {
            noteText.setText("Chưa có ghi chú nào.");
            taskLabel.setVisible(false);
            taskIcon.setVisible(false);
            DateLabel.setText("");
            return;
        }

        if (latestNote.getCreatedAt() != null) {
            DateLabel.setText(latestNote.getCreatedAt().toLocalDate().toString());
        } else {
            DateLabel.setText("Unknown");
        }

        noteText.setText(latestNote.getContent());
        noteText.setEditable(true); 

        if (latestNote.getTask() != null) {
            taskLabel.setText(latestNote.getTask().getTitle());
            taskLabel.setVisible(true);
            taskIcon.setVisible(true);
        } else {
            taskLabel.setVisible(false);
            taskIcon.setVisible(false);
        }
    }

    @FXML
    private void saveNote() {
        String newContent = noteText.getText().trim();
        if (newContent.isEmpty()) {
            System.out.println("Nội dung ghi chú trống.");
            return;
        }

        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;

        try {
            tx = session.beginTransaction();

            if (latestNote == null) {
                latestNote = new Note();
                latestNote.setUserId(loggedInUser.getId());
                latestNote.setContent(newContent);
                latestNote.setCreatedAt(LocalDateTime.now());
                session.persist(latestNote);
                System.out.println("Đã tạo ghi chú mới.");
            } else {
                latestNote.setContent(newContent);
                latestNote.setCreatedAt(LocalDateTime.now());
                session.merge(latestNote);
                System.out.println("Đã cập nhật ghi chú.");
            }

            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }

        noteText.setEditable(false);
    }

	private void loadTasksForDashboard(User user) {
	    Session session = HibernateUtil.getSessionFactory().openSession();

	    List<Task> tasks = session.createQuery(
	        "FROM Task WHERE user_id = :userId ORDER BY deadline ASC", Task.class)
	        .setParameter("userId", user.getId())
	        .getResultList();

	    session.close();

	    taskContainer.getChildren().clear(); // Xóa các task cũ nếu có

	    for (Task task : tasks) {
	        try {
	            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/taskDashboard.fxml"));
	            AnchorPane taskPane = loader.load();

	            TaskDashboardController controller = loader.getController();
	            controller.setTask(task);

	            taskContainer.getChildren().add(taskPane); // hiển thị lên giao diện
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
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
                  //  controller.setOnSelect(clickedTask -> openTaskWindowAndHighlight(clickedTask));

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
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/deadlineDashboard.fxml"));
                AnchorPane redLinePane = loader.load();

                //RedlineController controller = loader.getController();
                DeadlineDashboardController controller = loader.getController();

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
    private void setupChart() {
        studyChart.setLegendVisible(false);
        studyChart.setAnimated(false);
        yAxis.setLabel("Hours");
        yAxis.setTickUnit(1);
        yAxis.setAutoRanging(true);
        
        yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis) {
            @Override
            public String toString(Number object) {
                return String.format("%.1fh", object.doubleValue());
            }
        });
        
        studyChart.setCategoryGap(5);
        studyChart.setBarGap(3); 
    }
    private void setupTimeChoiceComboBox() {
        timeChoice.getItems().addAll(
            "Last 7 days", 
            "Last 14 days", 
            "Last 30 days", 
            "Last 3 months",
            "Custom range"
        );
        timeChoice.setValue("Last 7 days");
        
        timeChoice.setOnAction(e -> handleTimeChoiceSelection());
    }
    private void handleTimeChoiceSelection() {
        switch(timeChoice.getValue()) {
            case "Last 7 days": 
                daysToShow = 7; 
                break;
            case "Last 14 days": 
                daysToShow = 14; 
                break;
            case "Last 30 days": 
                daysToShow = 30; 
                break;
            case "Last 3 months": 
                daysToShow = 90; 
                break;
            case "Custom range": 
                showCustomDateDialog(); 
                return;
        }
        loadChartData();
    }
    public void loadChartData() {
        if (loggedInUser == null) return;

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(daysToShow - 1);

        List<StudyTime> studyList = studyTimeDAO.getStudyTimeForUserBetweenDates(loggedInUser, startDate, endDate);

        Map<LocalDate, Double> dateToHours = new TreeMap<>();
        for (int i = 0; i < daysToShow; i++) {
            LocalDate date = startDate.plusDays(i);
            dateToHours.put(date, 0.0);
        }
        for (StudyTime time : studyList) {
            dateToHours.put(time.getStudyDate(), time.getHours());
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Hours");

        DateTimeFormatter formatter = getDateFormatterForRange();
        // Thêm dữ liệu vào series
        for (Map.Entry<LocalDate, Double> entry : dateToHours.entrySet()) {
            String label = entry.getKey().format(formatter);
            XYChart.Data<String, Number> data = new XYChart.Data<>(label, entry.getValue());
            series.getData().add(data);
            
            setupBarChartTooltip(data, entry);
        }

        studyChart.getData().clear();
        studyChart.getData().add(series);
        
        adjustXAxisForDataRange();
        
        applyBarChartStyle();
    }
    private void setupBarChartTooltip(XYChart.Data<String, Number> data, Map.Entry<LocalDate, Double> entry) {
        data.nodeProperty().addListener((obs, oldNode, newNode) -> {
            if (newNode != null) {
                Tooltip tooltip = new Tooltip(
                    String.format("Ngày: %s\nGiờ học: %.1f giờ", 
                        entry.getKey().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        entry.getValue())
                );
                Tooltip.install(newNode, tooltip);
                
                // Hiệu ứng khi hover
                newNode.setStyle("-fx-cursor: hand;");
                newNode.setOnMouseEntered(e -> newNode.setStyle("-fx-bar-fill: #3498db;"));
                newNode.setOnMouseExited(e -> newNode.setStyle("-fx-bar-fill: #2a9df4;"));
            }
        });
    }
    private void applyBarChartStyle() {
        for (XYChart.Series<String, Number> series : studyChart.getData()) {
            for (XYChart.Data<String, Number> data : series.getData()) {
                Node node = data.getNode();
                if (node != null) {
                    node.setStyle(
                        "-fx-bar-fill: #2a9df4;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 1);"
                    );
                }
            }
        }
    }
    private void adjustXAxisForDataRange() {
        if (daysToShow > 30) {
            xAxis.setTickLabelRotation(90);
            xAxis.setTickMarkVisible(false);
            xAxis.setTickLabelGap(10);
        } else {
            xAxis.setTickLabelRotation(0);
            xAxis.setTickMarkVisible(true);
        }
        
        // Đặt khoảng cách giữa các nhãn trục X
        xAxis.setTickLabelGap(5);
        xAxis.setTickLength(5);
    }
    private DateTimeFormatter getDateFormatterForRange() {
        if (daysToShow <= 7) {
            return DateTimeFormatter.ofPattern("EEE\ndd/MM");
        } else if (daysToShow <= 14) {
            return DateTimeFormatter.ofPattern("dd/MM");
        } else if (daysToShow <= 30) {
            return DateTimeFormatter.ofPattern("dd/MM");
        } else {
            return DateTimeFormatter.ofPattern("MM/yyyy");
        }
    }
    private void showCustomDateDialog() {
        Dialog<Pair<LocalDate, LocalDate>> dialog = new Dialog<>();
        dialog.setTitle("Select Date Range");
        
        // Set up buttons
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Create date pickers
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        DatePicker startDatePicker = new DatePicker(LocalDate.now().minusDays(6));
        DatePicker endDatePicker = new DatePicker(LocalDate.now());
        
        grid.add(new Label("From:"), 0, 0);
        grid.add(startDatePicker, 1, 0);
        grid.add(new Label("To:"), 0, 1);
        grid.add(endDatePicker, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        // Convert result to pair of dates
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return new Pair<>(startDatePicker.getValue(), endDatePicker.getValue());
            }
            return null;
        });
        
        Optional<Pair<LocalDate, LocalDate>> result = dialog.showAndWait();
        
        result.ifPresent(dateRange -> {
            daysToShow = (int) ChronoUnit.DAYS.between(dateRange.getKey(), dateRange.getValue()) + 1;
            loadChartData();
        });
    }
    

}
