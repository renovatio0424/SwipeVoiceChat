package com.square.renov.swipevoicechat.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class VoiceChatRoom extends RealmObject{

    @SerializedName("id")
    @Expose
    @PrimaryKey
    private int id;
    @SerializedName("lastChatDate")
    @Expose
    private long lastChatDate;
    @SerializedName("createdAt")
    @Expose
    private long createdAt;
    @SerializedName("leaved")
    @Expose
    private Boolean leaved;
    @SerializedName("user")
    @Expose
    private User opponentUser;


    private boolean isNewRoom = true;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getLastChatDate() {
        return lastChatDate;
    }

    public void setLastChatDate(long lastChatDate) {
        this.lastChatDate = lastChatDate;
    }

    public Boolean getLeaved() {
        return leaved;
    }

    public void setLeaved(Boolean leaved) {
        this.leaved = leaved;
    }

    public User getOpponentUser() {
        return opponentUser;
    }

    public void setOpponentUser(User opponentUser) {
        this.opponentUser = opponentUser;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public boolean isNewRoom() {
        return isNewRoom;
    }

    public void setNewRoom(boolean newRoom) {
        isNewRoom = newRoom;
    }
}


//package com.square.renov.swipevoicechat.Model;
//
//import com.google.gson.annotations.Expose;
//import com.google.gson.annotations.SerializedName;
//
//public class VoiceChatRoom {
//
//    @SerializedName("id")
//    @Expose
//    private int id;
//    @SerializedName("opponentUser")
//    @Expose
//    private User opponentUser;
//    @SerializedName("lastChatDate")
//    @Expose
//    private long lastChatDate;
//    @SerializedName("leaved")
//    @Expose
//    private boolean leaved;
//
//
//    public int getId() {
//        return id;
//    }
//
//    public User getOpponentUser() {
//        return opponentUser;
//    }
//
//    public long getLastChatDate() {
//        return lastChatDate;
//    }
//
//    public boolean isLeaved() {
//        return leaved;
//    }
//
//    public void setOpponentUser(User opponentUser) {
//        this.opponentUser = opponentUser;
//    }
//}
