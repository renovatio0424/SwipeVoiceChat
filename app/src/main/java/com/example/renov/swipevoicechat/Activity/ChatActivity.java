package com.example.renov.swipevoicechat.Activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.renov.swipevoicechat.Model.Profile;
import com.example.renov.swipevoicechat.R;
import com.example.renov.swipevoicechat.Utils;
import com.example.renov.swipevoicechat.widget.VoicePlayerManager;

import butterknife.BindView;
import jp.wasabeef.glide.transformations.BlurTransformation;

public class ChatActivity extends AppCompatActivity {
//    @BindView(R.id.tv_my_name)
//    TextView myName;
//    @BindView(R.id.iv_my_profile)
//    ImageView myProfile;
//
//    @BindView(R.id.tv_other_name)
//    TextView otherName;
//    @BindView(R.id.iv_other_profile)
//    ImageView otherProfile;

    @BindView(R.id.btn_reply)
    Button btnReply;

    @BindView(R.id.iv_back)
    ImageView backButton;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Profile otherProfileData = Utils.loadProfiles(this).get(1);

        TextView myName = findViewById(R.id.tv_my_name);
        ImageView myProfile = findViewById(R.id.iv_my_profile);
        TextView otherName = findViewById(R.id.tv_other_name);
        ImageView otherProfile = findViewById(R.id.iv_other_profile);
        otherName.setText(otherProfileData.getName());

        MultiTransformation multiTransformation = new MultiTransformation(new BlurTransformation(25, 3),
                new CircleCrop());

        Glide.with(getApplicationContext())
                .load(otherProfileData.getImageUrl())
                .apply(RequestOptions.bitmapTransform(multiTransformation))
                .into(otherProfile);

        myName.setText("나");
        Glide.with(getApplicationContext())
                .load(R.drawable.com_facebook_profile_picture_blank_square)
                .apply(RequestOptions.bitmapTransform(multiTransformation))
                .into(myProfile);

        VoicePlayerManager voicePlayerManager = VoicePlayerManager.getInstance();


        Dialog dialog = new Dialog(ChatActivity.this);

        findViewById(R.id.btn_reply).setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                voicePlayerManager.voiceRecord(getApplicationContext());
                Toast.makeText(ChatActivity.this, "녹음을 시작합니다.", Toast.LENGTH_SHORT).show();
                dialog.setContentView(R.layout.dialog_reply);
                dialog.show();
                ((TextView) v).setText("보내려면 놓기");
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                voicePlayerManager.voiceRecordStop();
                Toast.makeText(ChatActivity.this, "녹음을 종료합니다.", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                ((TextView) v).setText("누르고 말하기");
            }

            return false;

        });
    }


//        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
//        View layout = inflater.inflate(R.layout.dialog_reply, null);
//
//        Toast myToast = new Toast(getApplicationContext());
//        myToast.setGravity(Gravity.CENTER_VERTICAL, 0, 100);
//        myToast.setDuration(Toast.LENGTH_SHORT);
//        myToast.setView(layout);
//        myToast.show();
}


