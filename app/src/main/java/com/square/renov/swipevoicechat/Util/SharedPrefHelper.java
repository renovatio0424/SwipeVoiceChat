package com.square.renov.swipevoicechat.Util;

import android.content.Context;
import android.content.SharedPreferences;
import android.speech.tts.Voice;

import com.google.gson.Gson;
import com.square.renov.swipevoicechat.Model.User;
import com.square.renov.swipevoicechat.Model.VoiceChat;

import java.util.ArrayList;

public class SharedPrefHelper {
    private static SharedPrefHelper instance;

    public static final String PREFERENCE_NAME = "PUBLIC_ENEMY_PREF";
    public static final String ACCESS_TOKEN = "ACCESS_TOKEN";
    public static final String USER_STATUS = "USER_STATUS";
    public static final String USER_ID = "USER_ID";
    public static final String POINT = "POINT";
    //	출석일자 체크 2018-02-05 김정원
    public static final String LOGGED_DAY_COUNT = "LOGGED_DAY_COUNT";
    public static final String SNS_TYPE = "SNS_TYPE";
    public static final String USER_INFO = "USER_INFO";

    public static final String MY_CHAT = "MY_CHAT";
    public static final String OTHER_CHAT = "OTHER_CHAT";

    public static final String TUTORAIL = "TUTORIAL";
    public static final String BADGE_COUNT = "BADGE_COUNT";
    public static final String CHAT_ROOM_DATA_UPDATE_TIME = "CHAT_ROOM_DATA_UPDATE_TIME";
    public static final String CHAT_DATA_UPDATE_TIME = "CHAT_DATA_UPDATE_TIME";

    //유저의 회원 가입 단계
    public static final String SIGN_UP_STEP = "SIGN_UP_STEP";
    public static final int TUTORIAL = 0;
    public static final int LOGIN = 1;
    public static final int SIGNUP = 2;
    public static final int PROFILE = 3;
    public static final int MAIN = 4;


    public SharedPreferences prefs;

    protected SharedPrefHelper() {
    }

    public static SharedPrefHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefHelper();
            instance.prefs = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        }

        return instance;
    }

    public boolean getSharedPreferences(String key, boolean defaultVal) {
        return prefs.getBoolean(key, defaultVal);
    }

    public String getSharedPreferences(String key, String defaultVal) {
        return prefs.getString(key, defaultVal);
    }

    public long getSharedPreferences(String key, long defaultVal) {
        return prefs.getLong(key, defaultVal);
    }

    public int getSharedPreferences(String key, int defaultVal) {
        return prefs.getInt(key, defaultVal);
    }

    public float getSharedPreferences(String key, float defaultVal) {
        return prefs.getFloat(key, defaultVal);
    }

    public User getUserInfo(){
        String userInfo = instance.getSharedPreferences(USER_INFO, null);
        Gson gson = new Gson();
        User me = gson.fromJson(userInfo, User.class);
        return me;
    }

    public void setMyChat(VoiceChat voiceChat){
        Gson gson = new Gson();
        String json = gson.toJson(voiceChat);
        setSharedPreferences(MY_CHAT, json);
    }

    public void setOtherChat(VoiceChat voicechat){
        Gson gson = new Gson();
        String json = gson.toJson(voicechat);
        setSharedPreferences(OTHER_CHAT, json);
    }

    public ArrayList<VoiceChat> getChatList(){
        Gson gson = new Gson();
        ArrayList<VoiceChat> result = new ArrayList<>();
        String myjson = getSharedPreferences(MY_CHAT, null);
        VoiceChat myChat = gson.fromJson(myjson, VoiceChat.class);

        String otherjson = getSharedPreferences(OTHER_CHAT, null);
        VoiceChat otherChat = gson.fromJson(otherjson, VoiceChat.class);

        result.add(otherChat);
        result.add(myChat);
        return result;
    }

    public void setSharedPreferences(String key, boolean val) {
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean(key, val);
        edit.commit();
    }

    public void setSharedPreferences(String key, int val) {
        SharedPreferences.Editor edit = prefs.edit();
        edit.putInt(key, val);
        edit.commit();
    }

    public void setSharedPreferences(String key, String val) {
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString(key, val);
        edit.commit();
    }

    public void setSharedPreferences(String key, long val) {
        SharedPreferences.Editor edit = prefs.edit();
        edit.putLong(key, val);
        edit.commit();
    }

    public void setSharedPreferences(String key, float val) {
        SharedPreferences.Editor edit = prefs.edit();
        edit.putFloat(key, val);
        edit.commit();
    }

    public void removeSharedPreferences(String key) {
        SharedPreferences.Editor edit = prefs.edit();
        edit.remove(key);
        edit.commit();
    }

    public void removeAllSharedPreferences() {
        removeSharedPreferences(ACCESS_TOKEN);
        removeSharedPreferences(USER_STATUS);
        removeSharedPreferences(USER_INFO);
    }

    public boolean hadExperiencedSignUpStep(int user_sign_up_step){
        int pastStep = prefs.getInt(SIGN_UP_STEP, -1);
        if(pastStep > user_sign_up_step)
            return true;
        else if(pastStep == -1){
            this.setSharedPreferences(SIGN_UP_STEP, 0);
            return false;
        }
        else if(pastStep == 4){
            return true;
        }
        else
            return false;
    }
}
