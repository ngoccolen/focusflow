package controller;

import java.io.File;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import com.mysql.cj.conf.ConnectionUrlParser.Pair;

import DAO.StudyTimeDAO;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
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
import model.StudyTime;
import model.User;

public class DashboardController {

    @FXML private ImageView GiaoDienChinhIcon;
    @FXML private ImageView LogoutIcon;
    @FXML private Label welcomeLabel;
    @FXML private ImageView avatarIcon;
    @FXML private GridPane calendarGrid;
    @FXML private Label monthLabel;
    @FXML private Label yearLabel; 
    @FXML private ImageView rightArrow;
    @FXML private ImageView leftArrow;
    @FXML private LineChart<String, Number> studyChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private ComboBox<String> timeChoice;
    private int daysToShow = 7;
    private YearMonth currentYearMonth;
    private User loggedInUser;
    private Parent root;
    private long startTime;

    public void setRoot(Parent root) {
        this.root = root;
    }

    public Parent getRoot() {
        return root;
    }

    @FXML
    public void initialize() {
        GiaoDienChinhIcon.setCursor(Cursor.HAND);
        LogoutIcon.setCursor(Cursor.HAND);
        avatarIcon.setCursor(Cursor.HAND);
        rightArrow.setCursor(Cursor.HAND);
        leftArrow.setCursor(Cursor.HAND);

        if (currentYearMonth == null) {
            currentYearMonth = YearMonth.now();
        }

        startTime = System.currentTimeMillis();

        updateCalendar();
        timeChoice.getItems().addAll(
                "Last 7 days", 
                "Last 14 days", 
                "Last 30 days", 
                "Last 3 months",
                "Custom range"
            );
            timeChoice.setValue("Last 7 days");
            
            timeChoice.setOnAction(e -> {
                switch(timeChoice.getValue()) {
                    case "Last 7 days": daysToShow = 7; break;
                    case "Last 14 days": daysToShow = 14; break;
                    case "Last 30 days": daysToShow = 30; break;
                    case "Last 3 months": daysToShow = 90; break;
                    case "Custom range": showCustomDateDialog(); break;
                }
                loadChartData();
            });
            studyChart.setLegendVisible(false);
            studyChart.setCreateSymbols(true);
            studyChart.setAnimated(false);
            yAxis.setLabel("Hours");
            yAxis.setTickUnit(1); 
            
    }

    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
        setWelcome();

        if (user.getAvatar() != null) {
            File avatarFile = new File(user.getAvatar());
            if (avatarFile.exists()) {
                avatarIcon.setImage(new Image(avatarFile.toURI().toString()));
            }
        }
        loadChartData();
    }

    private void setWelcome() {
        welcomeLabel.setText("Hi! " + loggedInUser.getUsername() + ", welcome!");
    }

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
            e.printStackTrace();
            System.out.println("Cannot switch to Giao Dien Chinh");
        }
    }

    public void handleLogoutClick(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SignUp.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) LogoutIcon.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        double hours = (endTime - startTime) / (1000.0 * 60 * 60);

        StudyTimeDAO dao = new StudyTimeDAO();
        dao.saveOrUpdateStudyTime(loggedInUser, LocalDate.now(), hours);
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
        ObservableList<Node> children = calendarGrid.getChildren();
        children.removeIf(node -> GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) > 0);

        LocalDate firstDayOfMonth = currentYearMonth.atDay(1);
        int daysInMonth = currentYearMonth.lengthOfMonth();
        int startDayOfWeek = firstDayOfMonth.getDayOfWeek().getValue();

        monthLabel.setText(String.valueOf(currentYearMonth.getMonthValue()));
        yearLabel.setText(String.valueOf(currentYearMonth.getYear()));

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

    public void handleLeftClick() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        updateCalendar();
    }

    public void handleRightClick() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        updateCalendar();
    }

    public void loadChartData() {
        if (loggedInUser == null) return;

        StudyTimeDAO dao = new StudyTimeDAO();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(daysToShow - 1);

        List<StudyTime> studyList = dao.getStudyTimeForUserBetweenDates(loggedInUser, startDate, endDate);

        // Tính tổng và trung bình thời gian học
        double totalHours = studyList.stream().mapToDouble(StudyTime::getHours).sum();
        double avgHours = totalHours / daysToShow;
        
        // Tạo map chứa tất cả các ngày trong khoảng
        Map<LocalDate, Double> dateToHours = new TreeMap<>();
        for (int i = 0; i < daysToShow; i++) {
        	LocalDate date = startDate.plusDays(i);
            dateToHours.put(date, 0.0);
        }
        for (StudyTime time : studyList) {
            dateToHours.put(time.getStudyDate(), time.getHours());
        }

        // Định dạng hiển thị khác nhau tùy theo số ngày
        DateTimeFormatter formatter;
        if (daysToShow <= 14) {
            formatter = DateTimeFormatter.ofPattern("E dd/MM");
        } else if (daysToShow <= 30) {
            formatter = DateTimeFormatter.ofPattern("dd/MM");
        } else {
            formatter = DateTimeFormatter.ofPattern("MM/yyyy");
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Study Hours");

        for (Map.Entry<LocalDate, Double> entry : dateToHours.entrySet()) {
        	String label = entry.getKey().format(formatter);
            XYChart.Data<String, Number> data = new XYChart.Data<>(label, entry.getValue());
            series.getData().add(data);
            
            // Thêm tooltip hiển thị chi tiết khi hover
            Node node = data.getNode();
            if (node != null) {
                Tooltip tooltip = new Tooltip(String.format("%s\n%.1f hours", 
                    entry.getKey().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")),
                    entry.getValue()));
                Tooltip.install(node, tooltip);
            }
        }
        studyChart.getData().clear();
        studyChart.getData().add(series);
        
        // Điều chỉnh trục X nếu có quá nhiều điểm dữ liệu
        if (daysToShow > 30) {
            xAxis.setTickLabelRotation(90);
            xAxis.setTickMarkVisible(false);
        } else {
            xAxis.setTickLabelRotation(0);
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
