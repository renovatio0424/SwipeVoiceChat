package com.square.renov.swipevoicechat.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PointLog {

    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("createdAt")
    @Expose
    private long createdAt;
    @SerializedName("cashKey")
    @Expose
    private String cashKey;
    @SerializedName("event")
    @Expose
    private Event event;
    @SerializedName("amount")
    @Expose
    private int amount;
    @SerializedName("remain")
    @Expose
    private int remain;


    public int getId() {
        return id;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getCashKey() {
        return cashKey;
    }

    public Event getEvent() {
        return event;
    }

    public int getAmount() {
        return amount;
    }

    public int getRemain() {
        return remain;
    }
}
