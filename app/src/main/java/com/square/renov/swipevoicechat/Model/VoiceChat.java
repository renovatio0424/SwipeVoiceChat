package com.square.renov.swipevoicechat.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VoiceChat {
    @SerializedName("user")
    @Expose
    private User voiceUser;
    @SerializedName("voiceUrl")
    @Expose
    private String voiceUrl;
    @SerializedName("sendTime")
    @Expose
    private long sendTime;

    public User getVoiceUser() {
        return voiceUser;
    }

    public long getSendTime() {
        return sendTime;
    }

    public String getVoiceUrl() {
        return voiceUrl;
    }
}
