package model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_members")
@IdClass(ChatMemberId.class) // khóa chính composite
public class ChatMember {

    @Id
    @ManyToOne
    @JoinColumn(name = "chat_id")
    private ChatConversation chat;

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    // Getters and Setters
    public ChatConversation getChat() {
        return chat;
    }

    public void setChat(ChatConversation chat) {
        this.chat = chat;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
}
