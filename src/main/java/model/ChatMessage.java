package model;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id") // ✅ map đúng tên cột trong DB
    private int messageId;

    @ManyToOne
    @JoinColumn(name = "chat_id")
    private ChatConversation chat;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @Column(name = "content")
    private String content;

    @Column(name = "type")
    private String type; // "text", "image", "file", "audio", "emoji"

    @Column(name = "file_path") // ✅ map đúng cột file_path trong DB
    private String filePath;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    // Getters & Setters
    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public ChatConversation getChat() {
        return chat;
    }

    public void setChat(ChatConversation chat) {
        this.chat = chat;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}
