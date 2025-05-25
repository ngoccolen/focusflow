package model;

import jakarta.persistence.*;
import javafx.css.converter.DurationConverter;

import java.time.*;

@Entity
@Table(name = "event") 
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime time;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(name = "reminder_offset", nullable = false)
    @Convert(converter = DurationConverter.class)
    private Duration reminderOffset;

    public Event() {} // Bắt buộc cần cho JPA và JavaFX

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(long i) { this.userId = i; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getTime() { return time; }
    public void setTime(LocalTime time) { this.time = time; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Duration getReminderOffset() { return reminderOffset; }
    public void setReminderOffset(Duration reminderOffset) { this.reminderOffset = reminderOffset; }
}
