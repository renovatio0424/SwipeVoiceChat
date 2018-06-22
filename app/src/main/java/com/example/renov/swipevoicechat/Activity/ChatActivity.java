package com.example.renov.swipevoicechat.Activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.renov.swipevoicechat.Model.Profile;
import com.example.renov.swipevoicechat.Model.VoiceChat;
import com.example.renov.swipevoicechat.R;
import com.example.renov.swipevoicechat.Utils;
import com.example.renov.swipevoicechat.widget.VoicePlayerManager;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.wasabeef.glide.transformations.BlurTransformation;

public class ChatActivity extends AppCompatActivity {
    @BindView(R.id.tv_my_name)
    TextView myName;
    @BindView(R.id.iv_my_profile)
    ImageView myProfile;
    @BindView(R.id.tv_other_name)
    TextView otherName;
    @BindView(R.id.iv_other_profile)
    ImageView otherProfile;
    @BindView(R.id.btn_reply)
    Button btnReply;

    @BindView(R.id.iv_back)
    ImageView backButton;

//    @BindView(R.id.rv_chatlist)
//    RecyclerView rvChatList;

    ChatAdapter chatAdapter;

    Unbinder unbinder;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        unbinder = ButterKnife.bind(this);
//        rvChatList.setAdapter();

        Profile otherProfileData = Utils.loadProfiles(this).get(1);

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @OnClick(R.id.iv_back)
    public void onClickBack(){
        finish();
    }

    private class ChatAdapter extends RecyclerView.Adapter<Holder> {
        private ArrayList<VoiceChat> chats;

        public ChatAdapter(ArrayList chats) {
            this.chats = chats;
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = null;
            switch (viewType) {
                //My Chat
                case 0:
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_chat_me, parent, false);
                    break;
                //Opponent Chat
                case 1:
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_chat_other, parent, false);
                    break;
            }

            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {

        }

        @Override
        public int getItemViewType(int position) {
            VoiceChat currentChat = chats.get(position);

            //내챗 상대방챗 구분점

            return 0;
        }

        @Override
        public int getItemCount() {
            return chats.size();
        }
    }

    private class Holder extends RecyclerView.ViewHolder {
        public Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
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


