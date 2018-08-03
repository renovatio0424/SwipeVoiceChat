package com.square.renov.swipevoicechat.Activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.cunoraz.gifview.library.GifView;
import com.facebook.AccessToken;
import com.facebook.Profile;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.gson.Gson;
import com.onesignal.OneSignal;
import com.square.renov.swipevoicechat.Model.Result;
import com.square.renov.swipevoicechat.Model.User;
import com.square.renov.swipevoicechat.Network.NetRetrofit;
import com.square.renov.swipevoicechat.R;
import com.square.renov.swipevoicechat.Util.SharedPrefHelper;
import com.square.renov.swipevoicechat.Util.Utils;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoadingActivity extends AppCompatActivity {
    private static final String TAG = LoadingActivity.class.getSimpleName();
    @BindView(R.id.iv_moon)
    ImageView moon;
    Unbinder unbinder;
    long duration = 4000;
    public String SNSTYPE_GOOGLE = "GOOGLE";
    public String SNSTYPE_FACEBOOK = "FACEBOOK";
    GoogleSignInClient mGoogleSignInClient;
    GoogleSignInAccount account;
    private boolean isLogged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        unbinder = ButterKnife.bind(this);

        setBadge(getApplicationContext(), 0);

        String tutorial = SharedPrefHelper.getInstance(this).getSharedPreferences(SharedPrefHelper.TUTORAIL, null);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_rotate);
        animation.setDuration(duration);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                AutoLogIn();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Log.e(TAG, "animation end");
                Intent intent;
                if (tutorial == null) {
                    intent = new Intent(LoadingActivity.this, TutorialActivity.class);
                    SharedPrefHelper.getInstance(LoadingActivity.this).setSharedPreferences(SharedPrefHelper.TUTORAIL, "true");
                } else if(isLogged){
                    intent = new Intent(LoadingActivity.this, MainActivity.class);
                } else {
                    intent = new Intent(LoadingActivity.this, LogInActivity.class);
                }
                finish();
                startActivity(intent);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        moon.startAnimation(animation);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    private void AutoLogIn() {
//        TODO: 로그인시 토큰값 발행 -> 어플 재실행시 토큰값 확인후 바로 로그인
//        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
        account = GoogleSignIn.getLastSignedInAccount(this);
//        // Check for existing Google Sign In account, if the user is already signed in
//        // the GoogleSignInAccount will be non-null.
//        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
//        boolean isFacebookLoggedIn = accessToken != null && !accessToken.isExpired();
//        boolean isGoogleLoggedIn = account != null;
//
//        if (isFacebookLoggedIn && isOurUser | isGoogleLoggedIn && isOurUser) {
//            return true;
//        } else {
//            return false;
//        }
        String accessToken = SharedPrefHelper.getInstance(LoadingActivity.this).getSharedPreferences(SharedPrefHelper.ACCESS_TOKEN, null);
        String snsType = SharedPrefHelper.getInstance(LoadingActivity.this).getSharedPreferences(SharedPrefHelper.SNS_TYPE, null);

        if (accessToken != null && snsType != null) {
            if (snsType.equals(SNSTYPE_FACEBOOK))
                accessToken = AccessToken.getCurrentAccessToken().getToken();
            else if (snsType.equals(SNSTYPE_GOOGLE)){
                accessToken = GoogleSignIn.getLastSignedInAccount(this).getIdToken();
            } else {
                Toast.makeText(this, "로그인 한적 없는 유저입니다", Toast.LENGTH_SHORT).show();
                return;
            }

            Call<User> call = NetRetrofit.getInstance(this).getService().login(accessToken, snsType);
            call.enqueue(returnCallback(snsType, accessToken));
        }
    }

    private Callback<User> returnCallback(String snsType, String Token) {
        return new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                // Signed in successfully, show authenticated UI.
                if (response.isSuccessful()) {
//                    hellovoiceauth token set
                    Log.d(TAG, "header: " + response.headers());
                    String token = response.headers().get("HelloVoiceAuth");
                    Log.d(TAG, "token: " + token);

                    User myInfo = response.body();

                    Gson gson = new Gson();
                    SharedPrefHelper.getInstance(LoadingActivity.this).setSharedPreferences(SharedPrefHelper.ACCESS_TOKEN, token);
                    SharedPrefHelper.getInstance(LoadingActivity.this).setSharedPreferences(SharedPrefHelper.SNS_TYPE, snsType);
                    SharedPrefHelper.getInstance(LoadingActivity.this).setSharedPreferences(SharedPrefHelper.USER_INFO, gson.toJson(myInfo));

                    Log.d(TAG,"gender: " + myInfo.getGender() +
                            "\nlat: " + myInfo.getLat() +
                            "\nlng: " + myInfo.getLng() +
                            "\nprofileImageUrl: " + myInfo.getProfileImageUrl() +
                            "\nbirth: " + myInfo.getBirth());

                    Log.e(TAG, "user.tostring(): " + gson.toJson(myInfo));

                    if(myInfo.getProfileImageUrl() != null && !"".equals(myInfo.getProfileImageUrl())){
                        isLogged = true;
                        OneSignal.sendTag("userId", String.valueOf(myInfo.getId()));
                    }
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
                Toast.makeText(LoadingActivity.this, "fail: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
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

        if(count == 0)
            SharedPrefHelper.getInstance(this).setSharedPreferences(SharedPrefHelper.BADGE_COUNT, count);

        context.sendBroadcast(intent);
    }

}
