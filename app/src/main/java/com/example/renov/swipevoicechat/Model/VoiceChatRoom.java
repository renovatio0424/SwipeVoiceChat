package com.example.renov.swipevoicechat.Model;

public class VoiceChatRoom {
    private int id;
    private User opponentUser;
    private long lastChatDate;

    public int getId() {
        return id;
    }

    public User getOpponentUser() {
        return opponentUser;
    }

    public long getLastChatDate() {
        return lastChatDate;
    }
}
