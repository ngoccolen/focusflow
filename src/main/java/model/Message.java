package model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Message implements Serializable {
    private String senderName;
    private String content;
    private String type;
    private LocalDateTime sentAt;
    private long fileSize;
    private String filePath;
    private byte[] fileData;
    private int chatId;
    private String senderAvatar;

    // ✅ Constructor mặc định cần thiết cho deserialization
    public Message() {
    }

    public Message(String senderName, String content, String type, LocalDateTime sentAt) {
        this.senderName = senderName;
        this.content = content;
        this.type = type;
        this.sentAt = sentAt;
    }

    public Message(String senderName, String content, String type, LocalDateTime sentAt, String filePath) {
        this.senderName = senderName;
        this.content = content;
        this.type = type;
        this.sentAt = sentAt;
        this.filePath = filePath;
    }

    // Getters and Setters
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public byte[] getFileData() { return fileData; }
    public void setFileData(byte[] fileData) { this.fileData = fileData; }

    public int getChatId() {
        return chatId;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
    }
    public String getSenderAvatar() {
        return senderAvatar;
    }

    public void setSenderAvatar(String senderAvatar) {
        this.senderAvatar = senderAvatar;
    }
}
