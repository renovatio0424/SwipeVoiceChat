package com.square.renov.swipevoicechat.Activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ClipData;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.square.renov.swipevoicechat.Fragment.RecordMeterDialogFragment;
import com.square.renov.swipevoicechat.Model.Profile;
import com.square.renov.swipevoicechat.Model.User;
import com.square.renov.swipevoicechat.Model.VoiceChat;
import com.square.renov.swipevoicechat.R;
import com.square.renov.swipevoicechat.Util.SharedPrefHelper;
import com.square.renov.swipevoicechat.Util.Utils;
import com.square.renov.swipevoicechat.widget.VoicePlayerManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.wasabeef.glide.transformations.BlurTransformation;

public class ChatActivity extends AppCompatActivity {

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

    MultiTransformation multiTransformation = new MultiTransformation(new BlurTransformation(25, 3),
            new CircleCrop());

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        unbinder = ButterKnife.bind(this);

//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
//        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
//        rvChatList.setLayoutManager(linearLayoutManager);
//        Gson gson = new Gson();
//        String myInfo = SharedPrefHelper.getInstance(getApplicationContext()).getSharedPreferences(SharedPrefHelper.USER_INFO, null);
//
//        User me = gson.fromJson(myInfo, User.class);
//        String myName = me.getName();
//        ChatAdapter chatAdapter = new ChatAdapter(Utils.loadChats(getApplicationContext()), myName);
//        rvChatList.setAdapter(chatAdapter);

        Profile otherProfileData = Utils.loadProfiles(this).get(1);

        otherName.setText(otherProfileData.getName());

        MultiTransformation multiTransformation = new MultiTransformation(new BlurTransformation(25, 3),
                new CircleCrop());

        Glide.with(getApplicationContext())
                .load(otherProfileData.getImageUrl())
                .apply(RequestOptions.bitmapTransform(multiTransformation))
                .into(otherProfile);

        int chatId = 1;
        RecordMeterDialogFragment recordMeterDialogFragment = RecordMeterDialogFragment.newInstance(chatId);


        findViewById(R.id.btn_reply).setOnTouchListener((v, event) -> {

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                Toast.makeText(ChatActivity.this, "녹음을 시작합니다.", Toast.LENGTH_SHORT).show();
                recordMeterDialogFragment.show(getSupportFragmentManager(), "RecordMeterDialogFragment");
                ((TextView) v).setText("보내려면 놓기");
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                recordMeterDialogFragment.dismiss();
                Toast.makeText(ChatActivity.this, "녹음을 종료합니다.", Toast.LENGTH_SHORT).show();
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
    public void onClickBack() {
        finish();
    }

    private class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int ME = 0;
        private static final int OPPONENT = 1;
        private List<VoiceChat> chats;
        private String myName;

        public ChatAdapter(List chats, String myName) {
            this.chats = chats;
            this.myName = myName;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = null;
            switch (viewType) {
                case ME:
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_chat_other, parent, false);
                    return new MyChatHolder(view);
                case OPPONENT:
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_chat_me2, parent, false);
                    return new OtherChatHolder(view);
                default:
                    return null;
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            VoiceChat chat = chats.get(position);
            int type = getItemViewType(position);

            switch (type) {
                case ME:
//                    bindMyChat(chat, holder);
                    break;

                case OPPONENT:
                    bindOpponentChat(chat, holder);
                    break;
            }
        }

        private void bindOpponentChat(VoiceChat chat, RecyclerView.ViewHolder holder) {
            OtherChatHolder thisHolder = (OtherChatHolder) holder;
            User voiceUser = chat.getVoiceUser();

//            int time = VoicePlayerManager.getInstance().
            try {
                VoicePlayerManager.getInstance().getPlayTime(chat.getVoiceUrl());
            } catch (IOException e) {
                e.printStackTrace();
            }

            Glide.with(getApplicationContext())
                    .load(voiceUser.getProfileImageUrl())
                    .apply(RequestOptions.bitmapTransform(multiTransformation))
                    .into(thisHolder.ivOtherProfile);

//            thisHolder.tvChatTime.setText();
        }


        /**
         * 0 -> me
         * 1 -> opponent
         */
        @Override
        public int getItemViewType(int position) {
            VoiceChat currentChat = chats.get(position);
            User voiceUser = currentChat.getVoiceUser();
            if (myName.equals(voiceUser.getName()))
                return ME;
            else
                return OPPONENT;
        }

        @Override
        public int getItemCount() {
            return chats.size();
        }
    }

    public class OtherChatHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_other_profile)
        ImageView ivOtherProfile;
        @BindView(R.id.tv_other_name)
        TextView tvOtherName;
        @BindView(R.id.ib_other_play)
        ImageButton ibOtherPlay;
        @BindView(R.id.pb_other_progress)
        ProgressBar pbOtherProgress;
        @BindView(R.id.tv_other_voice_time)
        TextView tvOtherVoiceTime;
        @BindView(R.id.tv_chat_time)
        TextView tvChatTime;


        public OtherChatHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    private class MyChatHolder extends RecyclerView.ViewHolder {

        public MyChatHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
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


