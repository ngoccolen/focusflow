package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
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
    @FXML private AnchorPane timePane, taskPane; 
    private TaskController taskController;
    private LocalDate currentDate = LocalDate.now();
    private User loggedInUser;
    private Stage taskStage;
    
    // hàm khởi tạo được gọi tự động sau khi giao diện FXML được tải xong.
    public void initialize() {
        updateDateLabels();
        generateHourGrid();
    }
    
    //hiển thị thông tin ngày tháng hiện tại lên giao diện
    private void updateDateLabels() {
    	//currentDate là một biến kiểu LocalDate chứa ngày hiện tại.
    	//getDayOfWeek() → lấy thứ trong tuần 
    	//Trả về tên hiển thị đầy đủ và bằng tiếng anh
        thuLabel.setText(currentDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        dayLabel.setText(String.valueOf(currentDate.getDayOfMonth()));
        monthLabel.setText(currentDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH)); 
    }
    
    //tạo đường kẻ dòng thời gian tương ứng với 24h
    private void generateHourGrid() {
        timePane.getChildren().clear();
        taskPane.getChildren().clear();
        //Đặt chiều cao của taskPane là 1440px → tương đương 24 giờ × 60 phút = 1440 phút, mỗi phút là 1 pixel.
        taskPane.setPrefHeight(1440);
        for (int hour = 0; hour <= 23; hour++) {
            Label hourLabel = new Label(String.format("%02d:00", hour));
            hourLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");
            hourLabel.setLayoutY(hour * 60); // mỗi giờ = 60px
            hourLabel.setLayoutX(5);
            timePane.getChildren().add(hourLabel);

            // tạo đường kẻ mỏng tương ứng với 1h
            javafx.scene.shape.Line line = new javafx.scene.shape.Line();
            line.setStartX(0);
            line.setStartY(hour * 60);
            line.setEndX(250); // chiều dài taskPane
            line.setEndY(hour * 60);
            line.setStyle("-fx-stroke: #e0e0e0;");
            taskPane.getChildren().add(line);
        }
    }
    
    //nhóm các nhiệm vụ (Task) bị chồng lấn thời gian với nhau vào cùng một nhóm (List<Task>)
    //Nhận vào danh sách các Task, trả về danh sách các nhóm Task mà các Task trong cùng một nhóm có thời gian chồng lấn nhau.
    private List<List<Task>> groupOverlappingTasks(List<Task> tasks) {
    	
    	//Tạo danh sách groups chứa các nhóm Task. Mỗi phần tử trong groups là một List<Task> đại diện cho 1 nhóm các Task chồng lấn nhau.
        List<List<Task>> groups = new ArrayList<>();
        
        //Duyệt từng task
        //Với mỗi Task task, chương trình tìm một nhóm sẵn có trong groups để chèn vào nếu nó chồng lấn thời gian với bất kỳ Task nào trong nhóm đó.
        //Nếu không tìm được nhóm nào phù hợp thì tạo một nhóm mới cho nó.
        for (int t = 0; t < tasks.size(); t++) {
            Task task = tasks.get(t);
            boolean added = false;

            // Duyệt qua từng nhóm có trong groups
            //Với mỗi Task đang xét, duyệt qua tất cả các nhóm đã tạo trong groups để xem task có thể chèn vào nhóm nào không.
            for (int i = 0; i < groups.size(); i++) {
                List<Task> group = groups.get(i);

                // Duyệt qua từng Task trong nhóm đó
                for (int j = 0; j < group.size(); j++) {
                    Task g = group.get(j);

                    // Kiểm tra xem task hiện tại có chồng lấn với task g trong nhóm không
                    if (isOverlapping(task, g)) {
                        group.add(task);    // Nếu chồng lấn, thêm task vào group
                        added = true;
                        break;             
                    }
                }

                // Nếu đã thêm task vào một group thì không cần duyệt tiếp các nhóm khác
                if (added) {
                    break;
                }
            }

            // Nếu không thêm được vào nhóm nào, tạo nhóm mới cho task
            if (!added) {
                List<Task> newGroup = new ArrayList<>();
                newGroup.add(task);
                groups.add(newGroup);
            }
        }

        return groups;
    }
    
    //kiểm tra 2 task có bị trùng tg ko
    private boolean isOverlapping(Task a, Task b) {
        return !(a.getEnd_time().isBefore(b.getStart_time()) || a.getStart_time().isAfter(b.getEnd_time()));
    }
    
    //truyền vào người dùng vừa đăng nhập
    public void setLoggedInUser(User user) {
    	//Gán đối tượng user cho biến loggedInUser của controller.
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
    
    //Tải các Task của người dùng đang đăng nhập trong ngày được chọn.
    private void loadTasksForDay(LocalDate date) {
        if (loggedInUser == null) return;
        //Mở 1 phiên làm việc mới với hibernate để giao tiếp với database
        Session session = HibernateUtil.getSessionFactory().openSession();
        //Truy vấn danh sách các Task của người dùng đăng nhập trong ngày date
        List<Task> tasks = session.createQuery(
        		//Lấy tất cả bản ghi thuộc class Task
        		//Task.class là kiểu kết quả mong muốn của truy vấn, Hibernate sẽ trả về một danh sách các đối tượng kiểu Task
            "FROM Task WHERE user_id = :userId AND date = :targetDate", Task.class)
        		/*truyền giá trị cho tham số :userId trong câu truy vấn.
                 loggedInUser.getId() gọi phương thức getId() trên đối tượng loggedInUser, 
                 trả về ID của user hiện tại đã đăng nhập.
                 Giá trị này sẽ được gán cho tham số :userId.*/
            .setParameter("userId", loggedInUser.getId())
            .setParameter("targetDate", date)
            .getResultList();
        session.close();
        //getChildren: Phương thức này trả về một danh sách các Node con trực tiếp bên trong taskPane.
        //removeIf: cho phép xóa tất cả phần tử thỏa mãn một điều kiện
        //node instanceof javafx.scene.shape.Line kiểm tra xem node có phải là một đối tượng thuộc lớp Line hay lớp con của nó không.
        //instanceof là một toán tử trong Java dùng để kiểm tra một đối tượng có phải là thể hiện (instance) của một lớp hoặc một kiểu dữ liệu cụ thể hay không.
        taskPane.getChildren().removeIf(node -> !(node instanceof javafx.scene.shape.Line)); // xóa task cũ, giữ lại line

        // sắp xếp danh sách các Task theo thời gian bắt đầu (start_time)
        //Comparator là một interface trong Java dùng để so sánh hai đối tượng với nhau
        tasks.sort(Comparator.comparing(task -> task.getStart_time()));

        // Nhóm các task giao nhau
        List<List<Task>> overlapGroups = groupOverlappingTasks(tasks);
        //Duyệt từng nhóm task giao nhau
        for (List<Task> group : overlapGroups) {
            int size = group.size();
            for (int i = 0; i < size; i++) {
                Task task = group.get(i);
                try {
                	//Tải giao diện FXML cho mỗi task 
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/calItem.fxml"));
                    AnchorPane taskBlock = loader.load();
                    /*Gửi Task vừa load vào controller để nó hiển thị dữ liệu:
                      setTask() → gán dữ liệu
                      setOnSelect() → xử lý khi user click vào block đó → mở cửa sổ chi tiết*/
                    CalItemController controller = loader.getController();
                    controller.setTask(task);
                    controller.setOnSelect(clickedTask -> openTaskWindowAndHighlight(clickedTask));

                    // Vị trí theo thời gian
                    int startMin = task.getStart_time().getHour() * 60 + task.getStart_time().getMinute();
                    //nếu thời gian end task ko có thì mặc định thời lượng task là 30p
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
        loadDeadlineForDay(date);
    }
    
    //Hiển thị các thanh đỏ lên giao diện taskPane để đánh dấu các task có deadline
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
                controller.setTask(task); 
                int y = task.getDeadline().toLocalTime().getHour() * 60
                        + task.getDeadline().toLocalTime().getMinute();
                redLinePane.setLayoutY(y);
                taskPane.getChildren().add(redLinePane);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    //mở cửa sổ quản lý task và tự động tô sáng task đang được chọn
    private void openTaskWindowAndHighlight(Task taskToHighlight) {
        if (taskController == null || taskStage == null || !taskStage.isShowing()) {
            try {
            	//Nếu chưa có task thì cần tạo task mới
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Task.fxml"));
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
}
