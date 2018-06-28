package com.square.renov.swipevoicechat.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.square.renov.swipevoicechat.Activity.LogInActivity;
import com.square.renov.swipevoicechat.Activity.ShopActivity;
import com.square.renov.swipevoicechat.Model.Result;
import com.square.renov.swipevoicechat.Model.User;
import com.square.renov.swipevoicechat.Network.NetRetrofit;
import com.square.renov.swipevoicechat.R;
import com.square.renov.swipevoicechat.Util.AgeUtil;
import com.square.renov.swipevoicechat.Util.SharedPrefHelper;
import com.igaworks.IgawCommon;
import com.igaworks.adpopcorn.IgawAdpopcorn;
import com.onesignal.OneSignal;
import com.tapjoy.TJActionRequest;
import com.tapjoy.TJConnectListener;
import com.tapjoy.TJError;
import com.tapjoy.TJPlacement;
import com.tapjoy.TJPlacementListener;
import com.tapjoy.Tapjoy;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.wasabeef.glide.transformations.BlurTransformation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingFragment2 extends Fragment {

    @BindView(R.id.iv_profile)
    ImageView profileImage;
    @BindView(R.id.iv_setting_detail)
    ImageView ivSettingDetail;
    @BindView(R.id.iv_edit_profile)
    ImageView ivEditProfile;
    @BindView(R.id.iv_edit_name)
    ImageView ivEditName;
    @BindView(R.id.tv_name)
    TextView nameAgeInfo;


    User myInfo;
    public Unbinder unbinder;
    MultiTransformation multiTransformation = new MultiTransformation(new BlurTransformation(25, 1),
            new CircleCrop(),
            new FitCenter());


    public static SettingFragment2 newInstance(User user) {
        SettingFragment2 settingFragment = new SettingFragment2();
        Bundle bundle = new Bundle();
        bundle.putParcelable("user", user);
        settingFragment.setArguments(bundle);
        return settingFragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Gson gson = new Gson();
        String jsonUserInfo = SharedPrefHelper.getInstance(getContext()).getSharedPreferences(SharedPrefHelper.USER_INFO, null);
        myInfo = gson.fromJson(jsonUserInfo, User.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (myInfo == null || myInfo.getProfileImageUrl() == null || "".equals(myInfo.getProfileImageUrl()))
            Glide.with(getContext())
                    .load(R.drawable.com_facebook_profile_picture_blank_square)
                    .apply(RequestOptions.bitmapTransform(multiTransformation))
                    .into(profileImage);
        else
            Glide.with(getContext())
                    .load(myInfo.getProfileImageUrl())
                    .apply(RequestOptions.bitmapTransform(multiTransformation))
                    .into(profileImage);

        nameAgeInfo.setText(myInfo.getName() + ", " + AgeUtil.getAgeFromBirth(myInfo.getBirth()));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting2, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }


    @OnClick(R.id.layout_free)
    public void onClickFree() {
        Toast.makeText(getContext(), "click free", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.layout_invite)
    public void onClickInvite() {
        Toast.makeText(getContext(), "click invite", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.layout_shop)
    public void onClickShop() {
        Intent intent = new Intent(getContext(), ShopActivity.class);
        startActivity(intent);
    }

    @OnClick({R.id.layout_profile, R.id.iv_edit_profile})
    public void onClickProfile() {
        new MaterialDialog.Builder(getContext())
                .title("프로필 설정")
                .items(R.array.profile)
                .itemsCallback((dialog, view, which, text) -> Toast.makeText(getContext(), "click: " + text.toString(), Toast.LENGTH_SHORT).show())
                .show();
    }

    @OnClick(R.id.iv_setting_detail)
    public void onClickSettingDetail() {
        //TODO: 로그아웃 기능 구현중
        Call<Result> response = NetRetrofit.getInstance(getContext()).getService().logout();
        response.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    SharedPrefHelper.getInstance(getContext()).removeAllSharedPreferences();
                    goToLogin();
                }

                switch (response.code()) {
                    default:
                        Toast.makeText(getContext(), "code: " + response.code() + "message: " + response.body(), Toast.LENGTH_SHORT).show();
                        break;
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void goToLogin() {
        Intent intent = new Intent(getContext(), LogInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @OnClick(R.id.iv_edit_name)
    public void onClickEditName() {
        Toast.makeText(getContext(), "click edit name", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.tv_shop)
    public void onClickGoToShop() {
        Intent intent = new Intent(getContext(), ShopActivity.class);
        startActivity(intent);
    }

}
