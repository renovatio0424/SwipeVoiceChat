package com.square.renov.swipevoicechat.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VoiceChatRoom {

    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("user")
    @Expose
    private User opponentUser;
    @SerializedName("lastChatDate")
    @Expose
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
