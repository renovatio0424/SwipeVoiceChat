package com.square.renov.swipevoicechat.Activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.AccessToken;
import com.kakao.kakaolink.v2.KakaoLinkResponse;
import com.kakao.kakaolink.v2.KakaoLinkService;
import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;
import com.square.renov.swipevoicechat.Model.User;
import com.square.renov.swipevoicechat.R;
import com.square.renov.swipevoicechat.Util.SharedPrefHelper;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class InviteActivity extends AppCompatActivity {
    private static final String TAG = InviteActivity.class.getSimpleName();
    @BindView(R.id.fl_invite_kakao)
    FrameLayout inviteKakao;
    @BindView(R.id.tv_title_bar)
            TextView titleBar;

    Unbinder unbinder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);
        unbinder = ButterKnife.bind(this);
        titleBar.setText("친구 초대하기");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @OnClick(R.id.fl_invite_kakao)
    public void inviteKakao(){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            new MaterialDialog.Builder(this)
                    .title("초대 코드를 복사했습니다")
                    .content("친구에게 전달해주세요 :)")
                    .positiveText("확인")
                    .show();

            copyClipBoard();
            return;
        }

        InviteKakao();
    }

    private void InviteKakao() {
        String templateId = "11289";
        Map<String, String> templateArgs = new HashMap<>();

//        String userId = SharedPrefHelper.getInstance(this).getSharedPreferences(SharedPrefHelper.USER_ID,null);
        User me = SharedPrefHelper.getInstance(this).getUserInfo();
        String recommend = idToCode((long) me.getId());

        templateArgs.put("recommend",recommend);
        KakaoLinkService.getInstance().sendCustom(this, templateId, templateArgs, new ResponseCallback<KakaoLinkResponse>() {
            @Override
            public void onFailure(ErrorResult errorResult) {
                Toast.makeText(InviteActivity.this, "카카오톡 초대 실패했습니다.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, errorResult.getErrorMessage());
            }

            @Override
            public void onSuccess(KakaoLinkResponse result) {

            }
        });
    }

    private String idToCode(Long id) {
        return Long.toString(id.longValue(), 36).toUpperCase();
    }

    private void copyClipBoard() {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("label", "[sori]\n익명의 목소리 SNS, Sori에 당신을 초대합니다\n설치하기 > https://goo.gl/fYcSx6\n추천코드는 [" + idToCode(Long.valueOf(getIntent().getStringExtra(AccessToken.USER_ID_KEY))) + "]입니다");
        if (clipboardManager != null) {
            clipboardManager.setPrimaryClip(clipData);
        }
    }

    public void enabledSendButton(TextView sendButton, boolean enabled){
        if(enabled){
            sendButton.setBackgroundResource(R.drawable.background_round_main_color);
            sendButton.setTextColor(getResources().getColor(R.color.main_color));
        }
        else {
            sendButton.setBackgroundResource(R.drawable.background_grey_stroke);
            sendButton.setTextColor(getResources().getColor(R.color.grey));
        }

        sendButton.setEnabled(enabled);
    }

    @OnClick(R.id.insert_recommend_code)
    public void insertRecommendCode(){
        MaterialDialog reportDialog = new MaterialDialog.Builder(InviteActivity.this)
                .customView(R.layout.dialog_code, false)
                .show();

        TextView tvSend = (TextView) reportDialog.findViewById(R.id.tv_send_code);
        TextView tvCancel = (TextView) reportDialog.findViewById(R.id.tv_cancel);
        EditText etInviteCode = (EditText) reportDialog.findViewById(R.id.et_code);

        etInviteCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() == 0)
                    enabledSendButton(tvSend, false);
                else
                    enabledSendButton(tvSend, true);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        tvSend.setOnClickListener(v -> {
            Toast.makeText(this, "send code", Toast.LENGTH_SHORT).show();
        });
        tvCancel.setOnClickListener(v -> {
            reportDialog.dismiss();
        });
    }

    @OnClick(R.id.iv_back)
    public void onClickBack(){
        finish();
    }
}
