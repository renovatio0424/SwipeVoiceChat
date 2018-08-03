package com.square.renov.swipevoicechat.Util;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class RealmHelper {
    private static Realm instance;

    public static String CHAT_ROOM = "chatroom.realm";
    public static String CHAT = "chat.realm";

    public RealmHelper(){

    }

    public static RealmConfiguration getRealmConfig(String configName){
        RealmConfiguration resultConfig = new RealmConfiguration.Builder()
                .name(configName)
                .schemaVersion(0)
                .build();
        return resultConfig;
    }

    public static Realm getRealm(String configName){
        return instance.getInstance(getRealmConfig(configName));
    }
}
