package com.square.renov.swipevoicechat.Push;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;
import com.square.renov.swipevoicechat.Activity.MainActivity;
import com.square.renov.swipevoicechat.Event.RefreshEvent;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * 유저가 푸시 알림을 클릭했을 경우 이벤트 처리
 * */

public class PushOpenHandler implements OneSignal.NotificationOpenedHandler{
    private static final String TAG = PushOpenHandler.class.getSimpleName();
    Context context;

    public PushOpenHandler(Context context){
        this.context = context;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void notificationOpened(OSNotificationOpenResult result) {
        if(result.notification.payload.additionalData != null){
            //TODO 푸시 타입에 따라 할일 들 ...
            JSONObject data = result.notification.payload.additionalData;

//                TODO : N 표시 띄우기
            if(isAppRunning(context)){
//                TODO : 데이터 갱신 요청
                Log.d(TAG, "app is not running");
                EventBus.getDefault().post(new RefreshEvent(RefreshEvent.Action.PUSH));
                Intent intent = new Intent(context, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("push",2);
                context.startActivity(intent);
            } else if(!isAppRunning(context)){
//                TODO : 앱 실행
                Log.d(TAG, "app is running");
                Intent intent = new Intent(context, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
            }

        }
    }

    private boolean isAppRunning(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
        for (int i = 0; i < procInfos.size(); i++) {
            if (procInfos.get(i).processName.equals(context.getPackageName())) {
                return true;
            }
        }

        return false;
    }

}
