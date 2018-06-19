package com.example.renov.swipevoicechat.Model;

public class User {
    private String birth;
    private String gender;
    private String lat;
    private String lng;
    private String profileImageUrl;

    public String getBirth() {
        return birth;
    }

    public String getGender() {
        return gender;
    }

    public String getLat() {
        return lat;
    }

    public String getLng() {
        return lng;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setBirth(String birth) {
        this.birth = birth;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
