package controller;

import java.io.File;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import DAO.StudyTimeDAO;
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
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Pair;
import model.StudyTime;
import model.User;
import service.StudySessionService;

public class DashboardController {

    // FXML Components
    @FXML private ImageView GiaoDienChinhIcon;
    @FXML private ImageView LogoutIcon;
    @FXML private Label welcomeLabel;
    @FXML private ImageView avatarIcon;
    @FXML private GridPane calendarGrid;
    @FXML private Label monthLabel;
    @FXML private Label yearLabel;
    @FXML private ImageView rightArrow;
    @FXML private ImageView leftArrow;
    @FXML private BarChart<String, Number> studyChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private ComboBox<String> timeChoice;

    // Application state
    private YearMonth currentYearMonth;
    private User loggedInUser;
    private Parent root;
    private int daysToShow = 7;
    
    // DAO instance
    private final StudyTimeDAO studyTimeDAO = StudyTimeDAO.getInstance();

    @FXML
    public void initialize() {
        setupUIComponents();
        setupChart();
        setupTimeChoiceComboBox();
        
        currentYearMonth = YearMonth.now();
        updateCalendar();
    }

    private void setupUIComponents() {
        // Set cursor for interactive elements
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
            GDChinhController gdchinhController = loader.getController();
            gdchinhController.setLoggedInUser(this.loggedInUser);
            
            Stage stage = (Stage) GiaoDienChinhIcon.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            showErrorAlert("Navigation Error", "Cannot switch to main interface: " + e.getMessage());
        }
    }

    @FXML
    public void handleLogoutClick(MouseEvent event) {
        try {
            // Kết thúc phiên học nếu đang hoạt động
            StudySessionService.getInstance().endStudySession();
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SignUp.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) LogoutIcon.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            showErrorAlert("Logout Error", "Failed to logout: " + e.getMessage());
        }
    }

    @FXML
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
            showErrorAlert("Profile Error", "Failed to open profile: " + e.getMessage());
        }
    }

    public void updateAvatarImage(String avatarPath) {
        if (avatarPath != null) {
            File avatarFile = new File(avatarPath);
            if (avatarFile.exists()) {
                avatarIcon.setImage(new Image(avatarFile.toURI().toString()));
            }
        }
    }

    public void updateCalendar() {
        // Clear existing day labels
        ObservableList<Node> children = calendarGrid.getChildren();
        children.removeIf(node -> GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) > 0);

        LocalDate firstDayOfMonth = currentYearMonth.atDay(1);
        int daysInMonth = currentYearMonth.lengthOfMonth();
        int startDayOfWeek = firstDayOfMonth.getDayOfWeek().getValue();

        // Update month and year labels
        monthLabel.setText(String.valueOf(currentYearMonth.getMonthValue()));
        yearLabel.setText(String.valueOf(currentYearMonth.getYear()));

        // Add day labels to calendar grid
        int col = (startDayOfWeek % 7);
        int row = 1;

        for (int day = 1; day <= daysInMonth; day++) {
            Label dayLabel = new Label(String.valueOf(day));
            calendarGrid.add(dayLabel, col, row);
            col++;
            if (col == 7) {
                col = 0;
                row++;
            }
        }
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