package com.square.renov.swipevoicechat.Push;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import com.onesignal.OSNotification;
import com.onesignal.OneSignal;
import com.square.renov.swipevoicechat.Event.RefreshEvent;
import com.square.renov.swipevoicechat.Model.Event;
import com.square.renov.swipevoicechat.Network.NetRetrofit;
import com.square.renov.swipevoicechat.Util.SharedPrefHelper;
import com.square.renov.swipevoicechat.Util.Utils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

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

    public PushReceiveHandler(Context context){
        this.context = context;
    }

    @Override
    public void notificationReceived(OSNotification notification) {
        JSONObject data = notification.payload.additionalData;

        Log.d(TAG, "receive Push");

        //푸시 답장왔을 경우 데이터 업데이트
        SharedPrefHelper.getInstance(context).setSharedPreferences(SharedPrefHelper.CHAT_ROOM_DATA_UPDATE_TIME, 0L);
        SharedPrefHelper.getInstance(context).setSharedPreferences(SharedPrefHelper.CHAT_DATA_UPDATE_TIME, 0L);
        EventBus.getDefault().post(new RefreshEvent(RefreshEvent.Action.STATUS_CHANGE, RefreshEvent.TYPE_REPLY));

        if (data != null && data.has("profileImage")) {
            int badgeCount = SharedPrefHelper.getInstance(context).getSharedPreferences(SharedPrefHelper.BADGE_COUNT, 0);
            badgeCount++;
            setBadge(context, badgeCount);
            SharedPrefHelper.getInstance(context).setSharedPreferences(SharedPrefHelper.BADGE_COUNT, badgeCount);
        }
    }

    public void setBadge(Context context, int count) {
        String packageName = context.getPackageName();
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        String launcherClassName = launchIntent.getComponent().getClassName();

        if (launcherClassName == null) {
            return;
        }

        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
        intent.putExtra("badge_count", count);
        intent.putExtra("badge_count_package_name", packageName);
        intent.putExtra("badge_count_class_name", launcherClassName);
        context.sendBroadcast(intent);
    }


}
