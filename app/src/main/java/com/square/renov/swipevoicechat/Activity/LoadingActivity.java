package com.square.renov.swipevoicechat.Activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
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
import com.square.renov.swipevoicechat.Model.SystemCheck;
import com.square.renov.swipevoicechat.Model.User;
import com.square.renov.swipevoicechat.MyApplication;
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

//        Utils.getHashKey(this);

        setBadge(getApplicationContext(), 0);

        String tutorial = SharedPrefHelper.getInstance(this).getSharedPreferences(SharedPrefHelper.TUTORAIL, null);

        moon.setVisibility(View.INVISIBLE);

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_rotate);
        animation.setDuration(duration);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                moon.setVisibility(View.VISIBLE);
                AutoLogIn();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Log.e(TAG, "animation end");
                Intent intent;
                if (tutorial == null) {
                    intent = new Intent(LoadingActivity.this, TutorialActivity.class);
                    SharedPrefHelper.getInstance(LoadingActivity.this).setSharedPreferences(SharedPrefHelper.TUTORAIL, "true");
                } else if (isLogged) {
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

        Call<SystemCheck> request = NetRetrofit.getInstance(this).getService().checkSystem();
        request.enqueue(new Callback<SystemCheck>() {
            @Override
            public void onResponse(Call<SystemCheck> call, Response<SystemCheck> response) {
                if (response.isSuccessful()) {
                    if (!response.body().isUsableVersion(MyApplication.getInstance().getAppVersionName())) {
                        MaterialDialog logoutDialog = new MaterialDialog.Builder(getApplicationContext())
                                .customView(R.layout.dialog_code, false)
                                .cancelable(false)
                                .show();

                        TextView tvTitle = (TextView) logoutDialog.findViewById(R.id.tv_title);
                        TextView tvContent = (TextView) logoutDialog.findViewById(R.id.tv_content);
                        TextView tvSend = (TextView) logoutDialog.findViewById(R.id.tv_send_code);
                        TextView tvCancel = (TextView) logoutDialog.findViewById(R.id.tv_cancel);
                        EditText etReason = (EditText) logoutDialog.findViewById(R.id.et_code);

                        etReason.setVisibility(View.GONE);
                        tvTitle.setText("앱이 업데이트 되었습니다!");
                        tvContent.setText("지금 바로 최신 버전으로 업데이트 해주세요 :)");
                        tvCancel.setText("Sori 종료");
                        tvSend.setText("바로 업데이트");

                        tvCancel.setOnClickListener(v -> {
                            finish();
                        });
                        tvSend.setOnClickListener(v -> {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName()));
                            startActivity(intent);
                        });

                    } else {
                        moon.startAnimation(animation);
                    }
                } else {
                        moon.startAnimation(animation);
                }
            }

            @Override
            public void onFailure(Call<SystemCheck> call, Throwable t) {

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    private void AutoLogIn() {
//        TODO: 로그인시 토큰값 발행 -> 어플 재실행시 토큰값 확인후 바로 로그인


        String snsAccessToken = null;

        String snsType = SharedPrefHelper.getInstance(LoadingActivity.this).getSharedPreferences(SharedPrefHelper.SNS_TYPE, null);

        if (SNSTYPE_GOOGLE.equals(snsType)) {
            account = GoogleSignIn.getLastSignedInAccount(this);
            snsAccessToken = account.getIdToken();
        } else if (SNSTYPE_FACEBOOK.equals(snsType)) {
            snsAccessToken = AccessToken.getCurrentAccessToken().getToken();
        }

        if (snsAccessToken != null && snsType != null) {
            Call<User> call = NetRetrofit.getInstance(this).getService().login(snsAccessToken, snsType);
            call.enqueue(returnCallback(snsType, snsAccessToken));
        }
    }

    private Callback<User> returnCallback(String snsType, String snsAccessToken) {
        return new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                // Signed in successfully, show authenticated UI.
                if (response.isSuccessful()) {
//                    hellovoiceauth token set
//                    Log.d(TAG, "header: " + response.headers());
                    String token = response.headers().get("HelloVoiceAuth");
//                    Log.d(TAG, "token: " + token);

                    User myInfo = response.body();

                    Gson gson = new Gson();
                    SharedPrefHelper.getInstance(LoadingActivity.this).setSharedPreferences(SharedPrefHelper.ACCESS_TOKEN, token);
                    SharedPrefHelper.getInstance(LoadingActivity.this).setSharedPreferences(SharedPrefHelper.SNS_TYPE, snsType);
                    SharedPrefHelper.getInstance(LoadingActivity.this).setSharedPreferences(SharedPrefHelper.USER_INFO, gson.toJson(myInfo));

                    Log.d(TAG, "gender: " + myInfo.getGender() +
                            "\nlat: " + myInfo.getLat() +
                            "\nlng: " + myInfo.getLng() +
                            "\nprofileImageUrl: " + myInfo.getProfileImageUrl() +
                            "\nbirth: " + myInfo.getBirth());

                    Log.e(TAG, "user.tostring(): " + gson.toJson(myInfo));

                    if (myInfo.getProfileImageUrl() != null && !"".equals(myInfo.getProfileImageUrl())) {
                        isLogged = true;
                        OneSignal.sendTag("userId", String.valueOf(myInfo.getId()));
                    }
                } else {

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

        context.sendBroadcast(intent);
    }

}
