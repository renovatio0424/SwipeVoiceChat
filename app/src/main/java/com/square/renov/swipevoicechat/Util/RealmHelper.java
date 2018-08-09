package com.square.renov.swipevoicechat.Util;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class RealmHelper {
    private static Realm instanceChatRoom;
    private static Realm instanceChat;
    private static Realm instanceFilterCard;

    public static String CHAT_ROOM = "chatroom.realm";
    public static String CHAT = "chat.realm";
    public static String FILTER_CARD = "filtercard.realm";

    public RealmHelper() {

    }

    public static RealmConfiguration getRealmConfig(String configName) {
        RealmConfiguration resultConfig = new RealmConfiguration.Builder()
                .name(configName)
                .schemaVersion(0)
                .build();
        return resultConfig;
    }

    public static Realm getRealm(String configName) {
        if (CHAT_ROOM.equals(configName))
            return instanceChatRoom.getInstance(getRealmConfig(configName));
        else if (CHAT.equals(configName))
            return instanceChat.getInstance(getRealmConfig(configName));
        else if (FILTER_CARD.equals(configName))
            return instanceFilterCard.getInstance(getRealmConfig(configName));
        else return null;
    }
}
