package controller;

import java.io.File;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;

import service.StudySessionService;
import DAO.EventDAO;
import DAO.StudyTimeDAO;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Pair;
import model.Event;
import model.StudyTime;
import model.User;

public class DashboardController {

    @FXML private ImageView GiaoDienChinhIcon, LogoutIcon, avatarIcon, rightArrow, leftArrow;
    @FXML private Label welcomeLabel, monthLabel, yearLabel;
    @FXML private GridPane calendarGrid;
    @FXML private BarChart<String, Number> studyChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private ComboBox<String> timeChoice, calendarViewMode;
    @FXML private VBox eventList;

    private YearMonth currentYearMonth;
    private User loggedInUser;
    private Parent root;
    private int daysToShow = 7;

    private final StudyTimeDAO studyTimeDAO = StudyTimeDAO.getInstance();

    @FXML
    public void initialize() {
        setupUIComponents();
        setupChart();
        setupTimeChoiceComboBox();
        setupCalendarViewMode();
        currentYearMonth = YearMonth.now();
        updateCalendar();
        startReminderScheduler();
    }

    private void setupCalendarViewMode() {
        calendarViewMode.getItems().addAll("Ngày", "Tuần", "Tháng");
        calendarViewMode.setValue("Tháng");
        calendarViewMode.setOnAction(e -> updateCalendar());
    }

    private void setupUIComponents() {
        GiaoDienChinhIcon.setCursor(Cursor.HAND);
        LogoutIcon.setCursor(Cursor.HAND);
        avatarIcon.setCursor(Cursor.HAND);
        rightArrow.setCursor(Cursor.HAND);
        leftArrow.setCursor(Cursor.HAND);
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
        timeChoice.getItems().addAll("Last 7 days", "Last 14 days", "Last 30 days", "Last 3 months", "Custom range");
        timeChoice.setValue("Last 7 days");
        timeChoice.setOnAction(e -> handleTimeChoiceSelection());
    }

    private void handleTimeChoiceSelection() {
        switch(timeChoice.getValue()) {
            case "Last 7 days": daysToShow = 7; break;
            case "Last 14 days": daysToShow = 14; break;
            case "Last 30 days": daysToShow = 30; break;
            case "Last 3 months": daysToShow = 90; break;
            case "Custom range": showCustomDateDialog(); return;
        }
        loadChartData();
    }

    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
        updateWelcomeMessage();
        updateAvatarImage();
        loadChartData();
    }

    private void updateWelcomeMessage() {
        if (loggedInUser != null) {
            welcomeLabel.setText("Hi! " + loggedInUser.getUsername() + ", welcome!");
        }
    }

    private void updateAvatarImage() {
        if (loggedInUser != null && loggedInUser.getAvatar() != null) {
            File avatarFile = new File(loggedInUser.getAvatar());
            if (avatarFile.exists()) {
                avatarIcon.setImage(new Image(avatarFile.toURI().toString()));
            }
        }
    }

    @FXML
    public void handleGDChinhClick(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/GiaoDienChinh.fxml"));
            Parent root = loader.load();
            GDChinhController controller = loader.getController();
            controller.setLoggedInUser(this.loggedInUser);
            Stage stage = (Stage) GiaoDienChinhIcon.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            showErrorAlert("Navigation Error", "Cannot switch to main interface: " + e.getMessage());
        }
    }

    public void handleLogoutClick(MouseEvent event) {
        try {
            StudySessionService service = StudySessionService.getInstance();
            if (service != null) service.endStudySession();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SignUp.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) LogoutIcon.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            showErrorAlert("Logout Error", "Failed to logout: " + e.getMessage());
        }
    }

    public void handleAvatarClick(MouseEvent event) {
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
            showErrorAlert("Profile Error", "Failed to open profile: " + e.getMessage());
        }
    }

    public void updateCalendar() {
        if (calendarGrid == null || currentYearMonth == null) return;
        ObservableList<Node> children = calendarGrid.getChildren();
        children.removeIf(node -> GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) > 0);

        LocalDate firstDay = currentYearMonth.atDay(1);
        int daysInMonth = currentYearMonth.lengthOfMonth();
        int startCol = firstDay.getDayOfWeek().getValue() % 7;

        if (monthLabel != null) monthLabel.setText(String.valueOf(currentYearMonth.getMonthValue()));
        if (yearLabel != null) yearLabel.setText(String.valueOf(currentYearMonth.getYear()));

        int col = startCol, row = 1;
        for (int day = 1; day <= daysInMonth; day++) {
            Label dayLabel = new Label(String.valueOf(day));
            dayLabel.setStyle("-fx-alignment: center;");
            calendarGrid.add(dayLabel, col, row);
            int finalDay = day;
            dayLabel.setOnMouseClicked(e -> handleDateClick(currentYearMonth.atDay(finalDay)));
            loadEventsForDay(day, dayLabel);
            col++;
            if (col == 7) { col = 0; row++; }
        }
    }

    private void loadEventsForDay(int day, Label dayLabel) {
        if (loggedInUser == null) return;
        LocalDate date = currentYearMonth.atDay(day);
        List<Event> events = EventDAO.getInstance().getEventsByDate(loggedInUser, date);
        if (!events.isEmpty()) {
            Label indicator = new Label("•");
            indicator.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            calendarGrid.add(indicator, GridPane.getColumnIndex(dayLabel), GridPane.getRowIndex(dayLabel));
        }
    }

    private void handleDateClick(LocalDate selectedDate) {
        eventList.getChildren().clear();
        List<Event> events = EventDAO.getInstance().getEventsByDate(loggedInUser, selectedDate);
        for (Event ev : events) addEventToList(ev);
        openEventDialog(selectedDate);
    }

    private void addEventToList(Event ev) {
        HBox hbox = new HBox(10);
        hbox.setStyle("-fx-background-color: #ffffff;");

        Label timeLabel = new Label(ev.getTime().toString());
        timeLabel.setPrefWidth(56);
        Label eventLabel = new Label(ev.getDescription());
        eventLabel.setPrefWidth(139);

        ImageView changeIcon = new ImageView(new Image("/images/icons8-pencil-48.png"));
        changeIcon.setFitHeight(26);
        changeIcon.setFitWidth(27);
        changeIcon.setCursor(Cursor.HAND);
        changeIcon.setOnMouseClicked(e -> openEditDialog(ev));

        ImageView deleteIcon = new ImageView(new Image("/images/icons8-trash-can-48.png"));
        deleteIcon.setFitHeight(26);
        deleteIcon.setFitWidth(37);
        deleteIcon.setCursor(Cursor.HAND);
        deleteIcon.setOnMouseClicked(e -> {
            EventDAO.getInstance().deleteEvent(ev.getId());
            eventList.getChildren().remove(hbox);
        });

        hbox.getChildren().addAll(timeLabel, eventLabel, changeIcon, deleteIcon);
        eventList.getChildren().add(hbox);
    }

    private void openEditDialog(Event ev) {
        Dialog<Event> dialog = new Dialog<>();
        dialog.setTitle("Chỉnh sửa sự kiện");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField descriptionField = new TextField(ev.getDescription());
        ComboBox<Integer> hourBox = new ComboBox<>();
        ComboBox<Integer> minuteBox = new ComboBox<>();
        ComboBox<String> reminderBox = new ComboBox<>();

        for (int i = 0; i < 24; i++) hourBox.getItems().add(i);
        for (int i = 0; i < 60; i += 5) minuteBox.getItems().add(i);
        reminderBox.getItems().addAll("15 phút trước", "2 ngày trước", "Không nhắc");

        hourBox.setValue(ev.getTime().getHour());
        minuteBox.setValue(ev.getTime().getMinute());

        grid.add(new Label("Nội dung:"), 0, 0);
        grid.add(descriptionField, 1, 0);
        grid.add(new Label("Giờ:"), 0, 1);
        grid.add(hourBox, 1, 1);
        grid.add(new Label("Phút:"), 0, 2);
        grid.add(minuteBox, 1, 2);
        grid.add(new Label("Nhắc nhở:"), 0, 3);
        grid.add(reminderBox, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                ev.setDescription(descriptionField.getText());
                ev.setTime(LocalTime.of(hourBox.getValue(), minuteBox.getValue()));
                return ev;
            }
            return null;
        });

        Optional<Event> result = dialog.showAndWait();
        result.ifPresent(updated -> {
            EventDAO.getInstance().updateEvent(updated);
            handleDateClick(updated.getDate());
        });
    }

    private void openEventDialog(LocalDate selectedDate) {
        Dialog<Event> dialog = new Dialog<>();
        dialog.setTitle("Thêm sự kiện");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField descriptionField = new TextField();
        ComboBox<Integer> hourBox = new ComboBox<>();
        ComboBox<Integer> minuteBox = new ComboBox<>();
        ComboBox<String> reminderBox = new ComboBox<>();

        for (int i = 0; i < 24; i++) hourBox.getItems().add(i);
        for (int i = 0; i < 60; i += 5) minuteBox.getItems().add(i);
        reminderBox.getItems().addAll("15 phút trước", "2 ngày trước", "Không nhắc");

        grid.add(new Label("Nội dung:"), 0, 0);
        grid.add(descriptionField, 1, 0);
        grid.add(new Label("Giờ:"), 0, 1);
        grid.add(hourBox, 1, 1);
        grid.add(new Label("Phút:"), 0, 2);
        grid.add(minuteBox, 1, 2);
        grid.add(new Label("Nhắc nhở:"), 0, 3);
        grid.add(reminderBox, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                Event ev = new Event();
                ev.setUserId(loggedInUser.getId());
                ev.setDate(selectedDate);
                ev.setTime(LocalTime.of(hourBox.getValue(), minuteBox.getValue()));
                ev.setDescription(descriptionField.getText());

                String reminderStr = reminderBox.getValue();
                if ("15 phút trước".equals(reminderStr)) ev.setReminderOffset(Duration.ofMinutes(15));
                else if ("2 ngày trước".equals(reminderStr)) ev.setReminderOffset(Duration.ofDays(2));
                else ev.setReminderOffset(Duration.ZERO);

                return ev;
            }
            return null;
        });

        Optional<Event> result = dialog.showAndWait();
        result.ifPresent(event -> {
            EventDAO.getInstance().saveEvent(event);
            addEventToList(event);
        });
    }

    private void startReminderScheduler() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            List<Event> reminders = EventDAO.getInstance().getUpcomingReminders(LocalDateTime.now());
            Platform.runLater(() -> {
                for (Event ev : reminders) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Nhắc nhở");
                    alert.setHeaderText("Sự kiện sắp diễn ra");
                    alert.setContentText(ev.getDescription() + " | " + ev.getTime());
                    alert.show();
                }
            });
        }, 0, 1, TimeUnit.MINUTES);
    }

    private void showCustomDateDialog() {
        Dialog<Pair<LocalDate, LocalDate>> dialog = new Dialog<>();
        dialog.setTitle("Select Date Range");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

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

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Getters and setters
    public void setRoot(Parent root) {
        this.root = root;
    }

    public Parent getRoot() {
        return root;
    }
}