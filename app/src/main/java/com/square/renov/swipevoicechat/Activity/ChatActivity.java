package com.square.renov.swipevoicechat.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.DefaultRetryPolicy;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.request.RequestOptions;
import com.square.renov.swipevoicechat.Event.RefreshEvent;
import com.square.renov.swipevoicechat.Fragment.ChatRoomFragment;
import com.square.renov.swipevoicechat.Model.User;
import com.square.renov.swipevoicechat.Model.VoiceCard;
import com.square.renov.swipevoicechat.Model.VoiceChat;
import com.square.renov.swipevoicechat.Model.VoiceChatRoom;
import com.square.renov.swipevoicechat.Network.NetRetrofit;
import com.square.renov.swipevoicechat.Network.network.RequestManager;
import com.square.renov.swipevoicechat.Network.network.VolleyMultipartRequest;
import com.square.renov.swipevoicechat.R;
import com.square.renov.swipevoicechat.Util.DialogUtils;
import com.square.renov.swipevoicechat.Util.ImageUtil;
import com.square.renov.swipevoicechat.Util.RealmHelper;
import com.square.renov.swipevoicechat.Util.SharedPrefHelper;
import com.square.renov.swipevoicechat.Util.Utils;
import com.square.renov.swipevoicechat.widget.VoiceBubble;
import com.square.renov.swipevoicechat.widget.VoicePlayerManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import jp.wasabeef.glide.transformations.BlurTransformation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = ChatActivity.class.getSimpleName();

    @BindView(R.id.tv_title_bar)
    TextView tvTitle;
    @BindView(R.id.tv_record_time)
    TextView tvRecordTime;
    @BindView(R.id.bottom_sheet)
    ConstraintLayout bottomSheet;

    BottomSheetBehavior bottomSheetBehavior;

    @BindView(R.id.iv_back)
    ImageView backButton;

    @BindView(R.id.recyclerview)
    RecyclerView rvChatList;

    @BindView(R.id.tv_title)
    TextView titleReply;

    @BindView(R.id.gradation)
    View viewGradation;

    @BindView(R.id.btn_reply)
    TextView btnReply;

    ChatAdapter chatAdapter;

    Unbinder unbinder;

    MultiTransformation multiTransformation = new MultiTransformation(new BlurTransformation(25, 3),
            new FitCenter(), new CircleCrop());

    RecordTimerTask mTask;

    private boolean isActive;
    private boolean leaved = false;

    int chatRoomId;
    User me;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        unbinder = ButterKnife.bind(this);

        chatRoomId = getIntent().getIntExtra("chatRoomId", -1);

        if (chatRoomId == -1) {
            Toast.makeText(this, "잘못된 방번호 입니다.", Toast.LENGTH_SHORT).show();
            finish();
        }

        String opponentName = getIntent().getStringExtra("opponentName");
        titleReply.setText(opponentName + "에게 답변하기");
        tvTitle.setText(opponentName);

        //TODO 샘플 데이터 삽입
//        loadSampleChatList();
        initLeavedRoom();
        initChatList();
//        Profile otherProfileData = Utils.loadProfiles(this).get(1);
//
//        otherName.setText(otherProfileData.getName());
//
//        MultiTransformation multiTransformation = new MultiTransformation(new BlurTransformation(25, 3),
//                new CircleCrop());
//
//        Glide.with(getApplicationContext())
//                .load(otherProfileData.getImageUrl())
//                .apply(RequestOptions.bitmapTransform(multiTransformation))
//                .into(otherProfile);
        initSendDialog();

        setBottomSheet();
        EventBus.getDefault().register(this);
    }

    private void initLeavedRoom() {
        Realm realm = RealmHelper.getRealm(RealmHelper.CHAT_ROOM);
        VoiceChatRoom thisRoom = realm.where(VoiceChatRoom.class).equalTo("id", chatRoomId).findFirst();
        leaved = thisRoom.getLeaved();
        realm.close();
    }

    @Override
    protected void onStart() {
        super.onStart();
        isActive = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadChatList(me.getName());
    }

    @Override
    protected void onPause() {
        super.onPause();
        chatAdapter.stopVoicePlay();
        stopTimer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        isActive = false;
    }

    private void setBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }

    long startTime, endTime;
    int replyCount = 0;
    Timer recordTimer;

    @SuppressLint("ClickableViewAccessibility")
    private void initSendDialog() {
        btnReply.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (leaved) {
                    Toast.makeText(this, "상대방이 채팅방을 나가서 더이상 대화를 하실 수 없습니다.", Toast.LENGTH_SHORT).show();
                    return false;
                }

                stopTimer();

                startTime = System.currentTimeMillis();
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                tvRecordTime.setText("00:00");
                ((TextView) v).setText("보내려면 놓기");
                viewGradation.setVisibility(View.VISIBLE);

                if (replyCount > 2 && startTime - endTime < 30 * 1000) {
                    Toast.makeText(this, "30초 뒤에 다시 실행해주세요", Toast.LENGTH_SHORT).show();
                    long downTime = SystemClock.uptimeMillis();
                    long upTime = SystemClock.uptimeMillis() + 10;
                    btnReply.dispatchTouchEvent(MotionEvent.obtain(downTime, upTime, MotionEvent.ACTION_UP, 0, 0, 0));
                    return false;
                }

                VoicePlayerManager.getInstance().voiceRecord(this);
                mTask = new RecordTimerTask();
                recordTimer = new Timer();
                recordTimer.schedule(mTask, 0, 10);

            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                if(leaved){
                    return false;
                }

                endTime = System.currentTimeMillis();

                ((TextView) v).setText("누르고 말하기");
                viewGradation.setVisibility(View.GONE);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

                if (replyCount > 2 && startTime - endTime < 30 * 1000) {
                    return false;
                }

                if (endTime - startTime < 3000) {
                    Toast.makeText(this, "3초 이상 녹음해주셔야되요. 다시 녹음해주세요", Toast.LENGTH_SHORT).show();
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                    replyCount++;
                    stopTimer();
                    return false;
                }
//                recordMeterDialogFragment.dismiss();
                stopTimer();
                VoicePlayerManager.getInstance().voiceRecordStop();
                getUploadVoiceInfoAndUploadVoice();
            }
            return false;
        });
    }

    private void stopTimer() {
        if (mTask != null) {
            mTask.cancel();
            mTask = null;
        }

        if (recordTimer != null) {
            recordTimer.cancel();
            recordTimer.purge();
            recordTimer = null;
        }

    }

    @Subscribe
    public void onRefreshEvent(RefreshEvent refreshEvent) {
        Log.e("event bus", "onRefreshEvent(): " + ChatRoomFragment.class.getSimpleName());
        if (refreshEvent.action == RefreshEvent.Action.STATUS_CHANGE && isActive) {
            loadChatList(me.getName());
        }
    }

    private void initChatList() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvChatList.setLayoutManager(linearLayoutManager);
        rvChatList.setHasFixedSize(true);
        rvChatList.setItemViewCacheSize(50);
        rvChatList.setDrawingCacheEnabled(true);
        rvChatList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        me = SharedPrefHelper.getInstance(getApplicationContext()).getUserInfo();
        String myName = me.getName();

        loadChatList(myName);
//        loadSampleChatList(myName);
    }

//    private void loadSampleChatList(String myName) {
////        chatAdapter = new ChatAdapter(Utils.loadChats(this), myName);
//        ArrayList<VoiceChat> chatlist = SharedPrefHelper.getInstance(getApplicationContext()).getChatList();
//        for (VoiceChat itChat : chatlist) {
//            Log.e(TAG, itChat.getVoiceUrl());
//            Log.e(TAG, itChat.getVoiceUser().getName());
//        }
//        chatAdapter = new ChatAdapter(chatlist, myName);
//        rvChatList.setAdapter(chatAdapter);
//        chatAdapter.notifyDataSetChanged();
//    }

    Realm realm = RealmHelper.getRealm(RealmHelper.CHAT);

    private void loadChatList(String myName) {
        RealmResults<VoiceChat> results = realm.where(VoiceChat.class).equalTo("chatRoomId", chatRoomId).findAllSorted("sendTime");

        Log.d(TAG, "load chat list : " + Utils.needToDataUpdate(this, SharedPrefHelper.CHAT_DATA_UPDATE_TIME));
        Log.d(TAG, "load chat list size : " + results.size());
        //        if(Utils.needToDataUpdate(this, SharedPrefHelper.CHAT_DATA_UPDATE_TIME))


        if (results.size() > 0 && !Utils.needToDataUpdate(this, SharedPrefHelper.CHAT_DATA_UPDATE_TIME)) {
            Log.d(TAG, "load realm");
            ArrayList<VoiceChat> chatList = new ArrayList<>();
            chatList.addAll(results);

            chatAdapter = new ChatAdapter(chatList, myName);
            rvChatList.setAdapter(chatAdapter);
            rvChatList.scrollToPosition(chatAdapter.getItemCount() - 1);

            chatAdapter.notifyDataSetChanged();

        } else {
            Call<ArrayList<VoiceChat>> request = NetRetrofit.getInstance(getApplicationContext()).getService().loadVoiceChatList(chatRoomId, 1000, 0);
            request.enqueue(new Callback<ArrayList<VoiceChat>>() {
                @Override
                public void onResponse(Call<ArrayList<VoiceChat>> call, Response<ArrayList<VoiceChat>> response) {
                    if (response.isSuccessful()) {
                        chatAdapter = new ChatAdapter(response.body(), myName);
                        rvChatList.setAdapter(chatAdapter);
                        rvChatList.scrollToPosition(chatAdapter.getItemCount() - 1);
                        chatAdapter.notifyDataSetChanged();

                        for (VoiceChat itChat : response.body()) {
                            itChat.setChatRoomId(chatRoomId);
                            realm.executeTransaction(realm1 -> {
                                realm.copyToRealmOrUpdate(itChat);
                            });
                        }

                        SharedPrefHelper.getInstance(getApplicationContext()).setSharedPreferences(SharedPrefHelper.CHAT_DATA_UPDATE_TIME, System.currentTimeMillis());
                    } else {
                        try {
                            Utils.toastError(getApplicationContext(), response);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ArrayList<VoiceChat>> call, Throwable t) {
                    t.printStackTrace();
                    Log.e(TAG, "error: " + t.getMessage());
                }
            });
        }
    }

    private void getUploadVoiceInfoAndUploadVoice() {
        File voiceFile = new File(VoicePlayerManager.getInstance().getFileName());

        if (voiceFile == null) {
            Toast.makeText(this, "녹음을 먼저 해주세요!", Toast.LENGTH_SHORT).show();
            return;
        }

        int fileSize = (int) voiceFile.length();

        Call<Map> call = NetRetrofit.getInstance(getApplicationContext()).getService().getUploadMetaData("voice", fileSize);
        call.enqueue(new Callback<Map>() {
            @Override
            public void onResponse(Call<Map> call, retrofit2.Response<Map> response) {

                if (response.isSuccessful()) {
                    uploadVoice(response.body(), voiceFile);
                } else {
                    try {
                        Utils.toastError(getApplicationContext(), response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<Map> call, Throwable t) {

            }
        });
    }

    public void uploadVoice(Map updateInfo, File temptFile) {
        final String uploadImagePath = "https://" + updateInfo.get("Host") + "/" + updateInfo.get("key");

        Log.d(TAG, "upload image path: " + uploadImagePath);

        String filePath = ImageUtil.getFilePathFromUri(getTempUri(temptFile), getApplicationContext());
        if (filePath == null || "".equals(filePath)) {
            return;
        }

        MaterialDialog progressDialog = new MaterialDialog.Builder(ChatActivity.this)
                .content("상대방에게 답장을 보내는 중 입니다 . . .")
                .progress(true, 0)
                .show();

        DialogUtils.initDialogView(progressDialog, this);

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest("https://hellovoicebucket.s3.amazonaws.com",
                response -> {
                    Log.d(TAG, "onResponse : " + response);
                    progressDialog.dismiss();

                    //TODO SEND CHAT VOICE @PARAM URL = uploadImagepath
                    if (chatRoomId != -1) {
                        int duration = 0;
                        try {
                            duration = VoicePlayerManager.getInstance().getPlayTime(uploadImagePath, getApplicationContext());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Log.d(TAG, "reply chat id: " + chatRoomId);
                        Call<VoiceCard> newVoiceRequest = NetRetrofit.getInstance(getApplicationContext()).getService().sendChatVoice(chatRoomId, uploadImagePath, duration);
                        newVoiceRequest.enqueue(new Callback<VoiceCard>() {
                            @Override
                            public void onResponse(Call<VoiceCard> call, retrofit2.Response<VoiceCard> response) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(ChatActivity.this, "답장을 성공적으로 보냈습니다!", Toast.LENGTH_SHORT).show();
                                    SharedPrefHelper.getInstance(getApplicationContext()).setSharedPreferences(SharedPrefHelper.CHAT_DATA_UPDATE_TIME, 0L);
                                    loadChatList(me.getName());
                                } else {
                                    try {
                                        Utils.toastError(getApplicationContext(), response);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<VoiceCard> call, Throwable t) {
                                t.printStackTrace();
                            }
                        });
                    }
                },
                error -> {
                    Log.e(TAG, "onErrorResponse : " + error.getMessage());
                    Toast.makeText(ChatActivity.this, "다시 보내주세요", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                });

        updateInfo.remove("Host");
        updateInfo.remove("uploadImagePath");
        multipartRequest.addStringParams(updateInfo);
        multipartRequest.addAttachment(VolleyMultipartRequest.MEDIA_TYPE_JPEG, "file", new File(filePath));
        multipartRequest.buildRequest();
        multipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestManager.addRequest(multipartRequest, "ProfileMultipart");
    }

    private Uri getTempUri(File temptFile) {
        Uri uri = null;
        try {
            uri = Uri.fromFile(temptFile);
        } catch (Exception e) {
            Log.w(TAG, "getTempUri fail : " + e.getMessage());
        }
        return uri;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED){
            long downTime = SystemClock.uptimeMillis();
            long upTime = SystemClock.uptimeMillis() + 10;
            btnReply.dispatchTouchEvent(MotionEvent.obtain(downTime, upTime, MotionEvent.ACTION_UP, 0, 0, 0));
        }

        unbinder.unbind();
        EventBus.getDefault().unregister(this);
        realm.close();

//        SharedPrefHelper.getInstance(getApplicationContext()).removeSharedPreferences(SharedPrefHelper.MY_CHAT);
//        SharedPrefHelper.getInstance(getApplicationContext()).removeSharedPreferences(SharedPrefHelper.OTHER_CHAT);

    }

    @OnClick(R.id.iv_back)
    public void onClickBack() {
        finish();
    }

    private class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int ME = 0;
        private static final int OPPONENT = 1;
        private static final int LEAVE = 2;
        private List<VoiceChat> chats;
        private String myName;
        private String OpponentName = null;
        private VoicePlayerManager voicePlayerManager;
        private VoiceBubble pastVoiceBubble = null;

        public ChatAdapter(List chats, String myName) {
            voicePlayerManager = VoicePlayerManager.getInstance();
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
                            .inflate(R.layout.item_chat_me, parent, false);
                    return new MyChatHolder(view);
                case OPPONENT:
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_chat_other, parent, false);
                    return new OtherChatHolder(view);
                case LEAVE:
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_chat_leave, parent, false);
                    return new FooterHolder(view);
                default:
                    return null;
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            int type = getItemViewType(position);
            VoiceChat chat;
            switch (type) {
                case ME:
                    chat = chats.get(position);
                    bindMyChat(chat, holder);
                    break;

                case OPPONENT:
                    chat = chats.get(position);
                    bindOpponentChat(chat, holder);
                    break;

                case LEAVE:
                    bindLeaveChat(holder);
                    break;
            }
        }

        public void stopVoicePlay(){
            if(pastVoiceBubble != null && pastVoiceBubble.getVoiceBubbleState() == VoiceBubble.STATE_PLAY)
                pastVoiceBubble.clickPlayButton();
        }

        private void bindMyChat(VoiceChat chat, RecyclerView.ViewHolder holder) {
            MyChatHolder thisHolder = (MyChatHolder) holder;
//            try {
//                int playTime = voicePlayerManager.getPlayTime(chat.getVoiceUrl(), getApplicationContext());
//                thisHolder.chatBubble.setPlayTime(playTime);
//            } catch (IOException e) {
//                e.printStackTrace();
//            voicePlayerManager.voicePlayStop();
//            }
            thisHolder.chatBubble.setPlayTime(chat.getSeconds());
            thisHolder.chatBubble.setVoiceFileUrl(chat.getVoiceUrl());
            thisHolder.chatBubble.setVoicePlayListener(new VoiceBubble.VoicePlayListener() {
                @Override
                public void onPlay() {
                    if(pastVoiceBubble != null && pastVoiceBubble.getVoiceBubbleState() == VoiceBubble.STATE_PLAY){
                        pastVoiceBubble.clickPlayButton();

                    }
                    pastVoiceBubble = thisHolder.chatBubble;
                    if(pastVoiceBubble != null)
                        Log.d(TAG, "past voice state: " + pastVoiceBubble.getVoiceBubbleState());
                    voicePlayerManager.voicePlay(chat.getVoiceUrl());
                }

                @Override
                public void onResume() {
                    if(pastVoiceBubble != null && pastVoiceBubble.getVoiceBubbleState() == VoiceBubble.STATE_PLAY)
                        pastVoiceBubble.clickPlayButton();
                    pastVoiceBubble = thisHolder.chatBubble;
                    if(pastVoiceBubble != null)
                        Log.d(TAG, "past voice state: " + pastVoiceBubble.getVoiceBubbleState());
                    voicePlayerManager.voicePlayResume();
                }

                @Override
                public void onPause() {
                    if(pastVoiceBubble != null && pastVoiceBubble.getVoiceBubbleState() == VoiceBubble.STATE_PLAY)
                        pastVoiceBubble.clickPlayButton();
                    pastVoiceBubble = thisHolder.chatBubble;
                    if(pastVoiceBubble != null)
                        Log.d(TAG, "past voice state: " + pastVoiceBubble.getVoiceBubbleState());
                    voicePlayerManager.voicePlayPause();
                }

                @Override
                public void onStopPlay() {
                    if(pastVoiceBubble != null && pastVoiceBubble.getVoiceBubbleState() == VoiceBubble.STATE_PLAY)
                        pastVoiceBubble.clickPlayButton();
                    pastVoiceBubble = thisHolder.chatBubble;
                    if(pastVoiceBubble != null)
                        Log.d(TAG, "past voice state: " + pastVoiceBubble.getVoiceBubbleState());
                    voicePlayerManager.voicePlayStop();
                }
            });
//            Log.e(TAG, "play time : " + playTime);
            thisHolder.tvChatTime.setText(Utils.setChatTime(chat.getSendTime()));

        }

        private void bindOpponentChat(VoiceChat chat, RecyclerView.ViewHolder holder) {
            OtherChatHolder thisHolder = (OtherChatHolder) holder;
            User voiceUser = chat.getVoiceUser();

            Glide.with(getApplicationContext())
                    .load(voiceUser.getProfileImageUrl())
                    .apply(RequestOptions.bitmapTransform(multiTransformation))
                    .into(thisHolder.ivOtherProfile);

            thisHolder.tvChatTime.setText(Utils.setChatTime(chat.getSendTime()));
            thisHolder.tvOtherName.setText(voiceUser.getName());

//            int playTime = voicePlayerManager.voicePlay(chat.getVoiceUrl());
//            voicePlayerManager.voicePlayStop();
//            thisHolder.chatBubble.setPlayTime(playTime);

            try {
                int playTime = voicePlayerManager.getPlayTime(chat.getVoiceUrl(), getApplicationContext());
                thisHolder.chatBubble.setPlayTime(playTime);
            } catch (IOException e) {
                e.printStackTrace();
            }
            thisHolder.chatBubble.setVoiceFileUrl(chat.getVoiceUrl());
            thisHolder.chatBubble.setVoicePlayListener(new VoiceBubble.VoicePlayListener() {
                @Override
                public void onPlay() {
                    if(pastVoiceBubble != null && pastVoiceBubble.getVoiceBubbleState() == VoiceBubble.STATE_PLAY){
                        pastVoiceBubble.clickPlayButton();

                    }
                    pastVoiceBubble = thisHolder.chatBubble;
                    if(pastVoiceBubble != null)
                        Log.d(TAG, "past voice state: " + pastVoiceBubble.getVoiceBubbleState());
                    voicePlayerManager.voicePlay(chat.getVoiceUrl());
                }

                @Override
                public void onResume() {
                    if(pastVoiceBubble != null && pastVoiceBubble.getVoiceBubbleState() == VoiceBubble.STATE_PLAY)
                        pastVoiceBubble.clickPlayButton();
                    pastVoiceBubble = thisHolder.chatBubble;
                    if(pastVoiceBubble != null)
                        Log.d(TAG, "past voice state: " + pastVoiceBubble.getVoiceBubbleState());
                    voicePlayerManager.voicePlayResume();
                }

                @Override
                public void onPause() {
                    if(pastVoiceBubble != null && pastVoiceBubble.getVoiceBubbleState() == VoiceBubble.STATE_PLAY)
                        pastVoiceBubble.clickPlayButton();
                    pastVoiceBubble = thisHolder.chatBubble;
                    if(pastVoiceBubble != null)
                        Log.d(TAG, "past voice state: " + pastVoiceBubble.getVoiceBubbleState());
                    voicePlayerManager.voicePlayPause();
                }

                @Override
                public void onStopPlay() {
                    if(pastVoiceBubble != null && pastVoiceBubble.getVoiceBubbleState() == VoiceBubble.STATE_PLAY)
                        pastVoiceBubble.clickPlayButton();
                    pastVoiceBubble = thisHolder.chatBubble;
                    if(pastVoiceBubble != null)
                        Log.d(TAG, "past voice state: " + pastVoiceBubble.getVoiceBubbleState());
                    voicePlayerManager.voicePlayStop();
                }
            });
        }

        private void bindLeaveChat(RecyclerView.ViewHolder holder){
            FooterHolder thisHolder = (FooterHolder) holder;
            if(leaved){
                thisHolder.layoutLeave.setVisibility(View.VISIBLE);
                thisHolder.leaveMessage.setText("상대방이 방을 나가셨습니다.");
            } else {
                thisHolder.layoutLeave .setVisibility(View.GONE);
            }
        }

        /**
         * 0 -> me
         * 1 -> opponent
         * 2 -> leave
         */
        @Override
        public int getItemViewType(int position) {
            Log.d(TAG, "1)" + (chats.size() + 1) + " : 2)" + position);

            if (chats.size() == position)
                return LEAVE;
            else {
                VoiceChat currentChat = chats.get(position);
                User voiceUser = currentChat.getVoiceUser();

                if (myName.equals(voiceUser.getName()))
                    return ME;
                else {
                    return OPPONENT;
                }
            }
        }

        @Override
        public int getItemCount() {
            return chats.size() + 1;
        }

        public void addItem(VoiceChat add) {
            chats.add(add);
            notifyDataSetChanged();
        }
    }

    public class OtherChatHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_other_profile)
        ImageView ivOtherProfile;
        @BindView(R.id.tv_other_name)
        TextView tvOtherName;
        @BindView(R.id.chat_bubble)
        VoiceBubble chatBubble;
        @BindView(R.id.tv_chat_time)
        TextView tvChatTime;


        public OtherChatHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public class MyChatHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_chat_time)
        TextView tvChatTime;
        @BindView(R.id.chat_bubble)
        VoiceBubble chatBubble;

        public MyChatHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public class FooterHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.constraint_layout_leave)
        ConstraintLayout layoutLeave;
        @BindView(R.id.tv_leave_message)
        TextView leaveMessage;

        public FooterHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public class RecordTimerTask extends TimerTask {
        long maxRecordTime = 30 * 1000;
        long time = 0L;
        private boolean isPause = false;

        /**
         * record time == 0 -> 아직 녹음을 안한 상태에는 녹음 타이머
         * record time != 0 -> 녹음을 한상태
         * time >= recordTime -> 재생이 끈난 상태
         * else -> 재생중
         */
        @Override
        public void run() {
            runOnUiThread(() -> {
                if (isPause) {
                    return;
                }

                time += 10L;
                if (time <= maxRecordTime) {
                    tvRecordTime.setText(Utils.setRecordTimer(time) + " / " + Utils.setRecordTimer(maxRecordTime));
                } else {
                    tvRecordTime.setText(Utils.setRecordTimer(maxRecordTime) + " / " + Utils.setRecordTimer(maxRecordTime));
                    long downTime = SystemClock.uptimeMillis();
                    long upTime = SystemClock.uptimeMillis() + 10;
                    btnReply.dispatchTouchEvent(MotionEvent.obtain(downTime, upTime, MotionEvent.ACTION_UP, 0, 0, 0));

                    stopTimer();
                    return;
                }
            });
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


