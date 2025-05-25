package DAO;

import model.StudyTime;
import model.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import Util.HibernateUtil;

import java.time.LocalDate;
import java.util.List;

public class StudyTimeDAO {
    // Thay đổi để đảm bảo singleton được sử dụng đúng
    private static final StudyTimeDAO instance = new StudyTimeDAO();
    
    private StudyTimeDAO() {}
    
    public static StudyTimeDAO getInstance() {
        return instance;
    }

    public void saveOrUpdateStudyTime(User user, LocalDate date, double hours) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            StudyTime existing = getStudyTimeByDate(session, user, date);
            
            if (existing != null) {
                existing.setHours(existing.getHours() + hours);
                session.update(existing);
            } else {
                StudyTime newTime = new StudyTime(user, date, hours);
                session.save(newTime);
            }

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Failed to save or update study time", e);
        }
    }

    private StudyTime getStudyTimeByDate(Session session, User user, LocalDate date) {
        try {
            return session.createQuery("FROM StudyTime WHERE user = :user AND studyDate = :date", StudyTime.class)
                         .setParameter("user", user)
                         .setParameter("date", date)
                         .uniqueResult();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get study time by date", e);
        }
    }

    public List<StudyTime> getStudyTimeForUser(User user) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM StudyTime WHERE user = :user ORDER BY studyDate", StudyTime.class)
                         .setParameter("user", user)
                         .list();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get study time for user", e);
        }
    }

    public List<StudyTime> getStudyTimeForUserBetweenDates(User user, LocalDate startDate, LocalDate endDate) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                "FROM StudyTime WHERE user = :user AND studyDate BETWEEN :start AND :end ORDER BY studyDate", 
                StudyTime.class)
                .setParameter("user", user)
                .setParameter("start", startDate)
                .setParameter("end", endDate)
                .list();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get study time between dates", e);
        }
    }
    
    public double getTotalStudyHours(User user) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Double total = session.createQuery("SELECT SUM(hours) FROM StudyTime WHERE user = :user", Double.class)
                                .setParameter("user", user)
                                .uniqueResult();
            return total != null ? total : 0.0;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get total study hours", e);
        }
    }
}