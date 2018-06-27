package com.square.renov.swipevoicechat.Event;

public class RefreshEvent {

    public Action action;
    public String type;
    public int position;

    public RefreshEvent(Action action) {
        this.action = action;
    }

    public RefreshEvent(Action action, String type) {
        this.action = action;
        this.type = type;
    }

    public RefreshEvent(Action action, int mPosition){
        this.action = action;
        this.position = mPosition;
    }

    public enum Action {
        SEND_NEW_STORY
    }
}
