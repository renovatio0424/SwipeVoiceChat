package com.example.renov.swipevoicechat.Model;

public class Filter {
    private boolean activeUser;
    private int ageMax;
    private int ageMin;
    private String gender;

    public boolean isActiveUser() {
        return activeUser;
    }

    public int getAgeMax() {
        return ageMax;
    }

    public int getAgeMin() {
        return ageMin;
    }

    public String getGender() {
        return gender;
    }
}
