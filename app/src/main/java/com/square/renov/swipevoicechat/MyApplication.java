package com.square.renov.swipevoicechat;

import android.app.Application;
import android.content.Context;

import com.nextapps.naswall.NASWall;
import com.square.renov.swipevoicechat.Network.network.RequestManager;
import com.onesignal.OneSignal;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

//        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.DEBUG, OneSignal.LOG_LEVEL.DEBUG);

        RequestManager.initialize(this);

        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();


    }


}
