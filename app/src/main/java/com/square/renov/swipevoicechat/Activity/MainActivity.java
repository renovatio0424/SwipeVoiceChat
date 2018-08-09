package com.square.renov.swipevoicechat.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.square.renov.swipevoicechat.Event.RefreshEvent;
import com.square.renov.swipevoicechat.Fragment.CardFragment;
import com.square.renov.swipevoicechat.Fragment.ChatRoomFragment;
import com.square.renov.swipevoicechat.Fragment.SettingFragment;
import com.square.renov.swipevoicechat.Handler.BackPressCloseHandler;
import com.square.renov.swipevoicechat.Model.User;
import com.square.renov.swipevoicechat.Model.VoiceChatRoom;
import com.square.renov.swipevoicechat.Network.NetRetrofit;
import com.square.renov.swipevoicechat.R;
import com.square.renov.swipevoicechat.Util.AdbrixUtil;
import com.square.renov.swipevoicechat.Util.RealmHelper;
import com.square.renov.swipevoicechat.Util.SharedPrefHelper;
import com.square.renov.swipevoicechat.Util.Utils;
import com.square.renov.swipevoicechat.widget.NonSwipeViewPager;
import com.igaworks.IgawCommon;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.frame_layout)
    NonSwipeViewPager viewPager;
    @BindView(R.id.title_user)
    ImageView titleUser;
    @BindView(R.id.title_main)
    ImageView titleMain;
    @BindView(R.id.iv_chat_room)
    ImageView titleChatRoom;

    public static ImageView newBadge;

    User myinfo;
    private Unbinder unbinder;

    public static boolean isActive;

    // Const
    private static final int PERMISSION_REQUEST_CODE = 8100;

    BackPressCloseHandler backPressCloseHandler = new BackPressCloseHandler(this);

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void moveTitleMenu(int i) {
        switch (i) {
            case 0:
                viewPager.setCurrentItem(0);
                titleUser.setAlpha(1.0f);
                titleMain.setAlpha(0.4f);
                titleChatRoom.setAlpha(0.4f);
                break;
            case 1:
                viewPager.setCurrentItem(1);
                titleUser.setAlpha(0.4f);
                titleMain.setAlpha(1.0f);
                titleChatRoom.setAlpha(0.4f);
                break;
            case 2:
                viewPager.setCurrentItem(2);
                titleUser.setAlpha(0.4f);
                titleMain.setAlpha(0.4f);
                titleChatRoom.setAlpha(1.0f);
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @OnClick(R.id.title_user)
    public void moveToUserPage() {
        moveTitleMenu(0);

    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @OnClick(R.id.title_main)
    public void moveToMainPage() {
        moveTitleMenu(1);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @OnClick(R.id.title_chat_room)
    public void moveToChatRoom() {
        moveTitleMenu(2);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EventBus.getDefault().register(this);

        unbinder = ButterKnife.bind(this);

        AdbrixUtil.setFirstTimeExperience(this, SharedPrefHelper.MAIN);

        getMyInfo();

        FragmentPagerAdapter pagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                Fragment currentFragment = null;
                switch (position) {
                    case 0:
                        currentFragment = SettingFragment.newInstance(myinfo);
                        break;
                    case 1:
                        currentFragment = CardFragment.newInstance(myinfo);
                        break;
                    case 2:
                        currentFragment = ChatRoomFragment.newInstance();
                        break;
                }
                return currentFragment;
            }

            @Override
            public int getCount() {
                return 3;
            }
        };

        viewPager.setAdapter(pagerAdapter);
        viewPager.setPagingEnabled(false);
        viewPager.setCurrentItem(1);
        moveTitleMenu(1);

        if (getIntent().hasExtra("push")) {
            moveTitleMenu(getIntent().getIntExtra("push", 1));
            Log.d(TAG, "move to chat room");
        }

//        setChatNewBadgeChangeListener();
        newBadge = (ImageView) findViewById(R.id.iv_new_badge);
        checkPermissions();
    }

    public static void chatNewBadge(boolean isNew){
        if(isNew)
            newBadge.setVisibility(View.VISIBLE);
        else
            newBadge.setVisibility(View.GONE);
    }

//    private void setChatNewBadgeChangeListener() {
//
//        RealmResults<VoiceChatRoom> results = realm.where(VoiceChatRoom.class).equalTo("isNewRoom",true).findAll();
//
//        if(results.size() > 0)
//            newBadge.setVisibility(View.VISIBLE);
//        else
//            newBadge.setVisibility(View.GONE);
//
//        results.addChangeListener(voiceChatRooms -> {
//            if(newBadge == null)
//                return;
//
//            if(voiceChatRooms.size() > 0)
//                newBadge.setVisibility(View.VISIBLE);
//            else
//                newBadge.setVisibility(View.GONE);
//        });
//    }

    @Override
    public void onStart() {
        super.onStart();
        isActive = true;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Subscribe
    public void onRefreshEvent(RefreshEvent refreshEvent) {
        Log.e("event bus", "onRefreshEvent(): " + MainActivity.class.getSimpleName());
        if (refreshEvent.action == RefreshEvent.Action.PUSH && isActive) {
            moveTitleMenu(2);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IgawCommon.startSession(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        IgawCommon.endSession();
    }

    @Override
    public void onStop() {
        isActive = false;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onBackPressed() {
        backPressCloseHandler.onBackPressed();
    }

    public void getMyInfo() {
        Call<User> request = NetRetrofit.getInstance(this).getService().checkCurrentUserInfo();
        request.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    myinfo = response.body();
                    Gson gson = new Gson();
                    String stringUserInfo = gson.toJson(myinfo);
                    SharedPrefHelper.getInstance(getApplicationContext()).setSharedPreferences(SharedPrefHelper.USER_INFO, stringUserInfo);
                    Log.d(TAG, "gender: " + myinfo.getGender() +
                            "\nlat: " + myinfo.getLat() +
                            "\nlng: " + myinfo.getLng() +
                            "\nprofileImageUrl: " + myinfo.getProfileImageUrl() +
                            "\nbirth: " + myinfo.getBirth() +
                            "\nname: " + myinfo.getName());
                } else {
                    try {
                        Utils.toastError(getApplicationContext(), response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> permissionList = new ArrayList<String>();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED)
                permissionList.add(Manifest.permission.INTERNET);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
                permissionList.add(Manifest.permission.READ_PHONE_STATE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED)
                permissionList.add(Manifest.permission.ACCESS_WIFI_STATE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED)
                permissionList.add(Manifest.permission.ACCESS_NETWORK_STATE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
                permissionList.add(Manifest.permission.RECORD_AUDIO);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
                permissionList.add(Manifest.permission.RECORD_AUDIO);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
                permissionList.add(Manifest.permission.READ_CONTACTS);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                permissionList.add(Manifest.permission.CAMERA);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_BOOT_COMPLETED) != PackageManager.PERMISSION_GRANTED)
                permissionList.add(Manifest.permission.RECEIVE_BOOT_COMPLETED);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);

            if (permissionList.size() > 0) {
                String[] permissions = new String[permissionList.size()];
                permissions = permissionList.toArray(permissions);
                ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    @SuppressLint("NewApi")
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int i = 0; i < permissions.length; ++i) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                }
                if (allGranted) {
                    Toast.makeText(this, "all granted", Toast.LENGTH_SHORT).show();
                    //TODO init nas
                } else {
                    Toast.makeText(this, "not all granted", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
