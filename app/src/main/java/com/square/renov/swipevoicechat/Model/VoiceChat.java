package com.square.renov.swipevoicechat.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class VoiceChat extends RealmObject{

    @SerializedName("id")
    @Expose
    @PrimaryKey
    private int chatId;
    @SerializedName("user")
    @Expose
    private User voiceUser;
    @SerializedName("voiceUrl")
    @Expose
    private String voiceUrl;
    @SerializedName("createdAt")
    @Expose
    private long sendTime;

    private int chatRoomId;


    public User getVoiceUser() {
        return voiceUser;
    }

    public long getSendTime() {
        return sendTime;
    }

    public String getVoiceUrl() {
        return voiceUrl;
    }

    public void setVoiceUser(User voiceUser) {
        this.voiceUser = voiceUser;
    }

    public void setVoiceUrl(String voiceUrl) {
        this.voiceUrl = voiceUrl;
    }

    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
    }

    public int getChatRoomId() {
        return chatRoomId;
    }

    public void setChatRoomId(int chatRoomId) {
        this.chatRoomId = chatRoomId;
    }
}
