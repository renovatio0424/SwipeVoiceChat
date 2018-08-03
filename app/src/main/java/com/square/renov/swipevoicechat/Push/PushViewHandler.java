package com.square.renov.swipevoicechat.Push;

import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationDisplayedResult;
import com.onesignal.OSNotificationReceivedResult;
import com.square.renov.swipevoicechat.Model.VoiceChatRoom;
import com.square.renov.swipevoicechat.R;
import com.square.renov.swipevoicechat.Util.RealmHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

import io.realm.Realm;

public class PushViewHandler extends NotificationExtenderService {
    private static final String TAG = PushViewHandler.class.getSimpleName();
    String profileImagePath = null;
    String name = null;
    String message = null;
    int chatRoomId = 0;

    @Override
    protected boolean onNotificationProcessing(OSNotificationReceivedResult notification) {

        JSONObject data = notification.payload.additionalData;
//
//        if(notification.payload != null){
//            Log.d(TAG, "receive Push: " + notification.payload.toString());
//            Log.d(TAG, "receive Push: " + notification.payload.body);
//            Log.d(TAG, "receive Push: " + notification.payload.additionalData);
//        }
////        String profileImageUrl = null;
////        try {
////            profileImageUrl = data.getString("profileImage");
////        } catch (JSONException e) {
////            e.printStackTrace();
////        }
////
////        if(profileImageUrl != null) {
////            try {
////                name = data.getString("name");
////                profileImagePath = data.getString("profileImage");
////                chatRoomId = data.getInt("chatId");
////            } catch (JSONException e) {
////                e.printStackTrace();
////            }
////        }
//
        try {
            chatRoomId = data.getInt("chatId");
            Log.d(TAG, "chat room id : " + chatRoomId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(chatRoomId != 0){
            Realm realm = RealmHelper.getRealm(RealmHelper.CHAT_ROOM);
            realm.executeTransactionAsync(realm1 -> {
                VoiceChatRoom oldRoom = realm1.where(VoiceChatRoom.class).equalTo("id", chatRoomId).findFirst();
                oldRoom.setNewRoom(true);
            });
        }

        OverrideSettings overrideSettings = new OverrideSettings();
        overrideSettings.extender = builder -> {
//            if(profileImagePath != null){
//                try {
//                    Bitmap bitmapProfile = Glide.with(getApplicationContext())
//                            .asBitmap()
//                            .load(profileImagePath)
//                            .submit(192,192)
//                            .get();
//
//                    builder.setLargeIcon(bitmapProfile);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                } catch (ExecutionException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            if(name != null)
//                builder.setContentTitle(name);

            return builder
                    .setSmallIcon(R.drawable.ic_moon)
                    .setContentText(notification.payload.body);
        };
        OSNotificationDisplayedResult displayedResult = displayNotification(overrideSettings);

        return true;
    }
}
