package DAO;

import jakarta.persistence.*;
import model.Event;
import model.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class EventDAO {
    private static EventDAO instance;
    private final EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-pu");

    public static EventDAO getInstance() {
        if (instance == null) instance = new EventDAO();
        return instance;
    }

    public void saveEvent(Event event) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        em.persist(event);
        tx.commit();
        em.close();
    }

    public List<Event> getEventsByDate(User user, LocalDate date) {
        if (user == null) return List.of(); // hoặc Collections.emptyList()
        EntityManager em = emf.createEntityManager();
        List<Event> result = em.createQuery("SELECT e FROM Event e WHERE e.userId = :uid AND e.date = :d", Event.class)
                .setParameter("uid", user.getId())
                .setParameter("d", date)
                .getResultList();
        em.close();
        return result;
    }

    public List<Event> getUpcomingReminders(LocalDateTime now) {
        EntityManager em = emf.createEntityManager();
        List<Event> result = em.createQuery("SELECT e FROM Event e", Event.class)
                .getResultList();
        em.close();

        return result.stream().filter(event -> {
            LocalDateTime eventTime = LocalDateTime.of(event.getDate(), event.getTime());
            LocalDateTime notifyTime = eventTime.minus(event.getReminderOffset());
            return now.isAfter(notifyTime.minusMinutes(1)) && now.isBefore(notifyTime.plusMinutes(1));
        }).toList();
    }

    public void deleteEvent(Long id) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        Event ev = em.find(Event.class, id);
        if (ev != null) em.remove(ev);
        tx.commit();
        em.close();
    }

    public void updateEvent(Event updated) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        em.merge(updated);
        tx.commit();
        em.close();
    }
}
