package model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "note")
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int note_id;

    private int user_id;

    @Lob
    private String content;

    private LocalDateTime created_at;

    // Ghi chú có thể gắn với task hoặc không
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = true)
    private Task task;

    public Note() {
        this.created_at = LocalDateTime.now();
    }

    public Note(int userId, String content, Task task) {
        this.user_id = userId;
        this.content = content;
        this.created_at = LocalDateTime.now();
        this.task = task;
    }

    // Getters and setters
    public int getNoteId() {
        return note_id;
    }

    public void setNoteId(int noteId) {
        this.note_id = noteId;
    }

    public int getUserId() {
        return user_id;
    }

    public void setUserId(int userId) {
        this.user_id = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return created_at;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.created_at = createdAt;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }
}
