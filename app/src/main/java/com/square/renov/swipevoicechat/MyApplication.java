package com.square.renov.swipevoicechat;

import android.app.Application;
import android.content.Context;

import com.igaworks.IgawCommon;
import com.nextapps.naswall.NASWall;
import com.square.renov.swipevoicechat.Network.network.RequestManager;
import com.onesignal.OneSignal;
import com.square.renov.swipevoicechat.Push.PushOpenHandler;
import com.square.renov.swipevoicechat.Push.PushReceiveHandler;
import com.tsengvn.typekit.Typekit;
import com.tsengvn.typekit.TypekitContextWrapper;

import java.lang.reflect.Type;

import io.realm.Realm;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

//        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.DEBUG, OneSignal.LOG_LEVEL.DEBUG);

        RequestManager.initialize(this);

        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .setNotificationOpenedHandler(new PushOpenHandler(getApplicationContext()))
                .setNotificationReceivedHandler(new PushReceiveHandler(getApplicationContext()))
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        IgawCommon.autoSessionTracking(MyApplication.this);

        Typekit.getInstance()
                .addNormal(Typekit.createFromAsset(this,"yoongodic_330.ttf"))
                .addBold(Typekit.createFromAsset(this,"yoongodic_350.ttf"))
                .addCustom1(Typekit.createFromAsset(this,"yoongodic_310.ttf"));

        Realm.init(this);
    }

}
