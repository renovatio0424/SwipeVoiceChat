package com.square.renov.swipevoicechat.Push;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import com.onesignal.OSNotification;
import com.onesignal.OneSignal;
import com.square.renov.swipevoicechat.Event.RefreshEvent;
import com.square.renov.swipevoicechat.Model.Event;
import com.square.renov.swipevoicechat.Model.VoiceChatRoom;
import com.square.renov.swipevoicechat.Network.NetRetrofit;
import com.square.renov.swipevoicechat.Util.RealmHelper;
import com.square.renov.swipevoicechat.Util.SharedPrefHelper;
import com.square.renov.swipevoicechat.Util.Utils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * 푸시가 도착했을 때 이벤트 처리
 * <p>
 * 답장 알림 체크 시점 (프로필 이미지가 있을 경우)
 */
public class PushReceiveHandler implements OneSignal.NotificationReceivedHandler {
    private static final String TAG = PushReceiveHandler.class.getSimpleName();
    Context context;
    int chatRoomId = -1;

    public PushReceiveHandler(Context context) {
        this.context = context;
    }

    @Override
    public void notificationReceived(OSNotification notification) {
        JSONObject data = notification.payload.additionalData;
        Log.d(TAG, "receive Push");
        Log.d(TAG, "push data : " + data.toString());
        Log.d(TAG, "push json object : " + notification.toJSONObject().toString());

        //푸시 답장왔을 경우 데이터 업데이트
        SharedPrefHelper.getInstance(context).setSharedPreferences(SharedPrefHelper.CHAT_ROOM_DATA_UPDATE_TIME, 0L);
        SharedPrefHelper.getInstance(context).setSharedPreferences(SharedPrefHelper.CHAT_DATA_UPDATE_TIME, 0L);

        try {
            if (data.has("chatId")){
                Log.d(TAG, "chat id 1 : " + chatRoomId);
                chatRoomId = Integer.valueOf((String) data.get("chatId"));
                SharedPrefHelper.getInstance(context).setSharedPreferences(SharedPrefHelper.NEW_CHAT_ROOM_ID, chatRoomId);

                if(chatRoomId == -1){
                    return;
                }
//                VoiceChatRoom newRoom = new VoiceChatRoom();
//                newRoom.setId(chatRoomId);
//                newRoom.setNewRoom(true);
//                Realm realm = RealmHelper.getRealm(RealmHelper.CHAT_ROOM);
//                realm.executeTransaction(realm1 -> realm1.copyToRealmOrUpdate(newRoom));
//                realm.executeTransactionAsync(realm1 -> {
//                    VoiceChatRoom oldRoom = realm1.where(VoiceChatRoom.class).equalTo("id", chatRoomId).findFirst();
//                    oldRoom.setNewRoom(true);
//                });
                EventBus.getDefault().post(new RefreshEvent(RefreshEvent.Action.STATUS_CHANGE, chatRoomId));
                Log.d(TAG, "chat id 2 : " + data.get("chatId"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }
}
