package com.square.renov.swipevoicechat.Activity;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.square.renov.swipevoicechat.Event.RefreshEvent;
import com.square.renov.swipevoicechat.Fragment.CardFragment;
import com.square.renov.swipevoicechat.Fragment.ChatRoomFragment;
import com.square.renov.swipevoicechat.Fragment.SettingFragment;
import com.square.renov.swipevoicechat.Handler.BackPressCloseHandler;
import com.square.renov.swipevoicechat.Model.User;
import com.square.renov.swipevoicechat.Network.NetRetrofit;
import com.square.renov.swipevoicechat.R;
import com.square.renov.swipevoicechat.Util.AdbrixUtil;
import com.square.renov.swipevoicechat.Util.SharedPrefHelper;
import com.square.renov.swipevoicechat.Util.Utils;
import com.square.renov.swipevoicechat.widget.NonSwipeViewPager;
import com.igaworks.IgawCommon;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
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
    @BindView(R.id.title_chat_room)
    ImageView titleChatRoom;

    User myinfo;
    private Unbinder unbinder;

    public static boolean isActive;

    BackPressCloseHandler backPressCloseHandler = new BackPressCloseHandler(this);

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void moveTitleMenu(int i) {
        switch (i){
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
    public void moveToUserPage(){
        moveTitleMenu(0);

    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @OnClick(R.id.title_main)
    public void moveToMainPage(){
        moveTitleMenu(1);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @OnClick(R.id.title_chat_room)
    public void moveToChatRoom(){
        moveTitleMenu(2);
    }



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EventBus.getDefault().register(this);

        if(getIntent().hasExtra("push")){
            moveTitleMenu(getIntent().getIntExtra("push",1));
            Log.d(TAG, "move to chat room");
        }

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
    }


    @Override
    public void onStart() {
        super.onStart();
        isActive = true;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Subscribe
    public void onRefreshEvent(RefreshEvent refreshEvent) {
        Log.e("event bus", "onRefreshEvent(): " + MainActivity.class.getSimpleName());
        if(refreshEvent.action == RefreshEvent.Action.PUSH && isActive){
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
}
