package com.example.renov.swipevoicechat.Activity;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.example.renov.swipevoicechat.Event.RefreshEvent;
import com.example.renov.swipevoicechat.Fragment.CardFragment;
import com.example.renov.swipevoicechat.Fragment.SettingFragment;
import com.example.renov.swipevoicechat.Fragment.ChatRoomFragment;
import com.example.renov.swipevoicechat.Fragment.SettingFragment2;
import com.example.renov.swipevoicechat.Handler.BackPressCloseHandler;
import com.example.renov.swipevoicechat.Model.User;
import com.example.renov.swipevoicechat.Network.NetRetrofit;
import com.example.renov.swipevoicechat.R;
import com.example.renov.swipevoicechat.widget.NonSwipeViewPager;
import com.igaworks.IgawCommon;
import com.tapjoy.TJActionRequest;
import com.tapjoy.TJConnectListener;
import com.tapjoy.TJError;
import com.tapjoy.TJPlacement;
import com.tapjoy.TJPlacementListener;
import com.tapjoy.Tapjoy;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.Hashtable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements TJConnectListener, TJPlacementListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    @BindView(R.id.navigation)
    public BottomNavigationView navigation;
    @BindView(R.id.frame_layout)
    NonSwipeViewPager viewPager;

    User myinfo;
    private Unbinder unbinder;

    BackPressCloseHandler backPressCloseHandler = new BackPressCloseHandler(this);

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {

                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        viewPager.setCurrentItem(0);
                        break;
                    case R.id.navigation_dashboard:
                        viewPager.setCurrentItem(1);
                        break;
                    case R.id.navigation_notifications:
                        viewPager.setCurrentItem(2);
                        break;
                }

                return true;
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        unbinder = ButterKnife.bind(this);

        getMyInfo();

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        FragmentPagerAdapter pagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                Fragment currentFragment = null;
                switch (position){
                    case 0:
                        currentFragment = SettingFragment2.newInstance(myinfo);
                        break;
                    case 1:
                        currentFragment = CardFragment.newInstance();
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
//        viewPager.setOnTouchListener((v, event) -> true);
        navigation.setSelectedItemId(R.id.navigation_dashboard);

//        viewPager.setCurrentItem(1);
//        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        transaction.replace(R.id.frame_layout, SettingFragment.newInstance());
//        transaction.commit();
    }


    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        Tapjoy.onActivityStart(this);
    }

    @Subscribe
    public void onRefreshEvent(RefreshEvent refreshEvent){
        Log.e("event bus","onRefreshEvent(): " + MainActivity.class.getSimpleName());

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
        Tapjoy.onActivityStop(this);
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @Override
    public void onBackPressed() {
        backPressCloseHandler.onBackPressed();
    }

    @Override
    public void onConnectSuccess() {
        Toast.makeText(this, "tapjoy Connect success", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectFailure() {
        Toast.makeText(this, "tapjoy Connect failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestSuccess(TJPlacement tjPlacement) {

    }

    @Override
    public void onRequestFailure(TJPlacement tjPlacement, TJError tjError) {

    }

    @Override
    public void onContentReady(TJPlacement tjPlacement) {

    }

    @Override
    public void onContentShow(TJPlacement tjPlacement) {

    }

    @Override
    public void onContentDismiss(TJPlacement tjPlacement) {

    }

    @Override
    public void onPurchaseRequest(TJPlacement tjPlacement, TJActionRequest tjActionRequest, String s) {

    }

    @Override
    public void onRewardRequest(TJPlacement tjPlacement, TJActionRequest tjActionRequest, String s, int i) {

    }

    public void getMyInfo() {
        Call<User> request =  NetRetrofit.getInstance(this).getService().checkCurrentUserInfo();
        request.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.isSuccessful()){
                    myinfo = response.body();

                    Log.d(TAG,"gender: " + myinfo.getGender() +
                    "\nlat: " + myinfo.getLat() +
                    "\nlng: " + myinfo.getLng() +
                    "\nprofileImageUrl: " + myinfo.getProfileImageUrl() +
                    "\nbirth: " + myinfo.getBirth());

                } else {
                    try {
                        Log.e(TAG,"raw: " + response.raw());
                        Log.e(TAG,"code: " + response.code());
                        Log.e(TAG,"headers: " + response.headers());
                        Log.e(TAG,"error body: " + response.errorBody().string());
                        Toast.makeText(MainActivity.this, "code: " + response.code() + "error message: " + response.errorBody().string(), Toast.LENGTH_SHORT).show();
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
