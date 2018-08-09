package com.square.renov.swipevoicechat.Model;

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
    private Integer ageMin;
    @SerializedName("ageMax")
    @Expose
    private Integer ageMax;

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

    public Integer getAgeMin() {
        return ageMin;
    }

    public void setAgeMin(Integer ageMin) {
        this.ageMin = ageMin;
    }

    public Integer getAgeMax() {
        return ageMax;
    }

    public void setAgeMax(Integer ageMax) {
        this.ageMax = ageMax;
    }

}