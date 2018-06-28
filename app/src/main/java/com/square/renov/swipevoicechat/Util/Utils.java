package com.square.renov.swipevoicechat.Util;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.square.renov.swipevoicechat.Model.Profile;
import com.square.renov.swipevoicechat.Model.VoiceCard;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.square.renov.swipevoicechat.Model.VoiceChat;
import com.square.renov.swipevoicechat.Model.VoiceChatRoom;

import org.json.JSONArray;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Utils {
    private static final String TAG = "Utils";

    public static List<Profile> loadProfiles(Context context) {
        try {
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            JSONArray array = new JSONArray(loadJSONFromAsset(context, "profiles.json"));
            List<Profile> profileList = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                Profile profile = gson.fromJson(array.getString(i), Profile.class);
                profileList.add(profile);
            }
            return profileList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<VoiceCard> loadCards(Context context){
        try {
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            JSONArray array = new JSONArray(loadJSONFromAsset(context, "voicecards.json"));
            List<VoiceCard> voiceCards = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                VoiceCard voiceCard = gson.fromJson(array.getString(i), VoiceCard.class);
                voiceCards.add(voiceCard);
            }
            return voiceCards;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<VoiceChatRoom> loadRooms(Context context){
        try {
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            JSONArray array = new JSONArray(loadJSONFromAsset(context, "voicechatrooms.json"));
            List<VoiceChatRoom> voiceChatRooms = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                VoiceChatRoom voiceChatRoom = gson.fromJson(array.getString(i), VoiceChatRoom.class);
                voiceChatRooms.add(voiceChatRoom);
            }
            return voiceChatRooms;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<VoiceChat> loadChats(Context context){
        try {
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            JSONArray array = new JSONArray(loadJSONFromAsset(context, "voicechats.json"));
            List<VoiceChat> voiceChats = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                VoiceChat voiceChat = gson.fromJson(array.getString(i), VoiceChat.class);
                voiceChats.add(voiceChat);
            }
            return voiceChats;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String loadJSONFromAsset(Context context, String jsonFileName) {
        String json = null;
        InputStream is = null;
        try {
            AssetManager manager = context.getAssets();
            Log.d(TAG, "path " + jsonFileName);
            is = manager.open(jsonFileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

}
