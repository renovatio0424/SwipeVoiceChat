package com.square.renov.swipevoicechat.Event;

import com.square.renov.swipevoicechat.Model.VoiceChat;
import com.square.renov.swipevoicechat.Model.VoiceChatRoom;

public class RefreshEvent {

    public Action action;
    public String type;
//    public int position;
    public int chatRoomId;
    public VoiceChatRoom voiceChatRoom;
    public VoiceChat voiceChat;
    public static String TYPE_REPLY = "reply";

    public RefreshEvent(Action action) {
        this.action = action;
    }

    public RefreshEvent(Action action, String type) {
        this.action = action;
        this.type = type;
    }

    public RefreshEvent(Action action, int chatRoomId){
        this.action = action;
        this.chatRoomId = chatRoomId;
    }

//    public RefreshEvent(Action action, int mPosition){
//        this.action = action;
//        this.position = mPosition;
//    }

    public RefreshEvent(Action action, VoiceChatRoom room){
        this.action = action;
        this.voiceChatRoom = room;
    }

    public RefreshEvent(Action action, VoiceChat chat){
        this.action = action;
        this.voiceChat = chat;
    }

    public enum Action {
        STATUS_CHANGE,
        PUSH
    }
}
