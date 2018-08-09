package com.square.renov.swipevoicechat.Push;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.request.RequestOptions;
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
    MultiTransformation multiTransformation = new MultiTransformation(new CircleCrop(),
            new FitCenter());

    @Override
    protected boolean onNotificationProcessing(OSNotificationReceivedResult notification) {

        JSONObject data = notification.payload.additionalData;

        try {
            chatRoomId = data.getInt("chatId");
            Log.d(TAG, "chat room id : " + chatRoomId);
            if (chatRoomId != 0) {
                String name = (String) data.get("name");
                String profileImage = (String) data.get("profileImage");

                Log.d(TAG, "name : " + name);
                Log.d(TAG, "profileImage : " + profileImage);

                OverrideSettings overrideSettings = new OverrideSettings();
                overrideSettings.extender = new NotificationCompat.Extender() {
                    @Override
                    public NotificationCompat.Builder extend(NotificationCompat.Builder builder) {

                        Bitmap bitmapProfile = null;
                        try {
                            bitmapProfile = Glide
                                    .with(getApplicationContext())
                                    .applyDefaultRequestOptions(RequestOptions.bitmapTransform(multiTransformation))
                                    .asBitmap()
                                    .load(profileImage)
                                    .submit()
                                    .get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }

                        return builder.setContentTitle(name)
                                .setSmallIcon(R.drawable.ic_moon)
                                .setLargeIcon(bitmapProfile);
                    }
                };
                OSNotificationDisplayedResult displayedResult = displayNotification(overrideSettings);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        OverrideSettings overrideSettings = new OverrideSettings();
//        overrideSettings.extender = builder -> {
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
//
//            return builder
//                    .setSmallIcon(R.drawable.ic_moon)
//                    .setContentText(notification.payload.body);
//        };
        return true;
    }
}
