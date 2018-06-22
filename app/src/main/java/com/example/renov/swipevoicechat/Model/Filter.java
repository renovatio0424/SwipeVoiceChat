package com.example.renov.swipevoicechat.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Filter {

    @SerializedName("activeUser")
    @Expose
    private Boolean activeUser;
    @SerializedName("gender")
    @Expose
    private String gender;
    @SerializedName("ageMin")
    @Expose
    private int ageMin;
    @SerializedName("ageMax")
    @Expose
    private int ageMax;

    public Boolean getActiveUser() {
        return activeUser;
    }

    public void setActiveUser(Boolean activeUser) {
        this.activeUser = activeUser;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getAgeMin() {
        return ageMin;
    }

    public void setAgeMin(int ageMin) {
        this.ageMin = ageMin;
    }

    public int getAgeMax() {
        return ageMax;
    }

    public void setAgeMax(int ageMax) {
        this.ageMax = ageMax;
    }

}