package model;

import java.io.Serializable;
import java.util.Objects;

public class ChatMemberId implements Serializable {
    private int chat;
    private int user;

    public ChatMemberId() {
    }

    public ChatMemberId(int chat, int user) {
        this.chat = chat;
        this.user = user;
    }

    // bắt buộc phải override equals và hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatMemberId)) return false;
        ChatMemberId that = (ChatMemberId) o;
        return chat == that.chat && user == that.user;
    }

    @Override
    public int hashCode() {
        return Objects.hash(chat, user);
    }
}
