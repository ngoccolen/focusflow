package service;

import DAO.StudyTimeDAO;
import model.User;
import java.time.LocalDate;

public class StudySessionService {
    private static StudySessionService instance;
    private long sessionStartTime;
    private User currentUser;
    private boolean isActive;

    private StudySessionService() {}

    public static StudySessionService getInstance() {
        if (instance == null) {
            instance = new StudySessionService();
        }
        return instance;
    }
    public boolean isActive() {
        return isActive;
    }

    public void startStudySession(User user) {
        if (!isActive) {
            this.currentUser = user;
            this.sessionStartTime = System.currentTimeMillis();
            this.isActive = true;
            System.out.println("Bắt đầu phiên học - User: " + user.getUsername());
        }
    }

    public void endStudySession() {
        if (isActive && currentUser != null) {
            double hoursStudied = (System.currentTimeMillis() - sessionStartTime) / (1000.0 * 60 * 60.0);
            StudyTimeDAO.getInstance().saveOrUpdateStudyTime(currentUser, LocalDate.now(), hoursStudied);
            System.out.println("Đã lưu " + hoursStudied + " giờ học");
            isActive = false;
        }
    }
}