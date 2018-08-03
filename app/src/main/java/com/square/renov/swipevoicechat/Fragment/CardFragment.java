package com.square.renov.swipevoicechat.Fragment;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.square.renov.swipevoicechat.Activity.FilterActivity;
import com.square.renov.swipevoicechat.Activity.InviteActivity;
import com.square.renov.swipevoicechat.Activity.PointLogActivity;
import com.square.renov.swipevoicechat.Activity.RecordActivity;
import com.square.renov.swipevoicechat.Activity.ShopActivity;
import com.square.renov.swipevoicechat.Event.RefreshEvent;
import com.square.renov.swipevoicechat.Model.Filter;
import com.square.renov.swipevoicechat.Model.User;
import com.square.renov.swipevoicechat.Model.VoiceCard;
import com.square.renov.swipevoicechat.Model.VoiceChat;
import com.square.renov.swipevoicechat.Model.VoiceChatRoom;
import com.square.renov.swipevoicechat.Network.NetRetrofit;
import com.square.renov.swipevoicechat.Network.ApiService;
import com.square.renov.swipevoicechat.R;
import com.square.renov.swipevoicechat.Util.AgeUtil;
import com.square.renov.swipevoicechat.Util.DialogUtils;
import com.square.renov.swipevoicechat.Util.DistanceUtil;
import com.square.renov.swipevoicechat.Util.SharedPrefHelper;
import com.square.renov.swipevoicechat.Util.Utils;
import com.square.renov.swipevoicechat.widget.VoicePlayerManager;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.SwipeDirection;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.wasabeef.glide.transformations.BlurTransformation;
import okhttp3.internal.Util;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rm.com.audiowave.AudioWaveView;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class CardFragment extends Fragment {

    private static final String TAG = CardFragment.class.getSimpleName();

    @BindView(R.id.activity_main_card_stack_view)
    public CardStackView cardStackView;
    @BindView(R.id.fl_new_story)
    public FrameLayout btnNewStory;
    @BindView(R.id.tr_filter)
    public ConstraintLayout filterRow;
    @BindView(R.id.filter_luna)
    TextView filterLuna;
    @BindView(R.id.ib_reject)
    ImageButton rejectButton;
    @BindView(R.id.ib_accept)
    ImageButton acceptButton;
    @BindView(R.id.tv_empty)
    TextView tvEmpty;

    //    private TouristSpotCardAdapter adapter;
    private UserCardAdapter adapter;

    private String[] permissions = {Manifest.permission.RECORD_AUDIO};
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    int recordRequestCode = 1234;

    ApiService service = NetRetrofit.getInstance(getContext()).getService();

    User myInfo;

    public Unbinder unbinder;
    public VoiceCard pastCard;
    private boolean isActive;


    public static CardFragment newInstance(User myInfo) {
        CardFragment cardFragment = new CardFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("user", myInfo);
        cardFragment.setArguments(bundle);
        return cardFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Gson gson = new Gson();
        String jsonUserInfo = SharedPrefHelper.getInstance(getContext()).getSharedPreferences(SharedPrefHelper.USER_INFO, null);
        myInfo = gson.fromJson(jsonUserInfo, User.class);

    }

    @Override
    public void onStart() {
        super.onStart();
        isActive = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        isActive = false;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        Main Logic here
        setup();
        reload();
        ActivityCompat.requestPermissions(getActivity(), permissions, REQUEST_RECORD_AUDIO_PERMISSION);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_card, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    private void createUserCardAdapter() {
        if (adapter == null)
            adapter = new UserCardAdapter(getContext());

//        for(VoiceCard card : Utils.loadCards(getContext())){
//            adapter.add(card);
//        }
        adapter.addAll(Utils.loadCards(getContext()));
        cardStackView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        cardStackView.setVisibility(View.VISIBLE);
        btnNewStory.setVisibility(View.VISIBLE);
        filterRow.setVisibility(View.VISIBLE);
        filterRow.setVisibility(View.VISIBLE);

        try {
            int currentPosition = cardStackView.getTopIndex();
            pastCard = adapter.getItem(currentPosition);
            Log.d("CardStackView", "topIndex: " + cardStackView.getTopIndex());
            Log.d("CardStackView", "past card id: " + pastCard.getId());
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            pastCard = null;
            tvEmpty.setVisibility(View.VISIBLE);
        }
//        for (Profile profile : Utils.loadProfiles(getContext())) {
//            adapter.add(profile);
//        }
    }

    private void setup() {
        filterLuna.setText("" + myInfo.getLuna());
        cardStackView.setSwipeThreshold(0.5f);
        cardStackView.setSwipeDirection(SwipeDirection.HORIZONTAL);
        cardStackView.setCardEventListener(new CardStackView.CardEventListener() {
            @Override
            public void onCardDragging(float percentX, float percentY) {
                Log.d("CardStackView", "onCardDragging");
            }

            @Override
            public void onCardSwiped(SwipeDirection direction) {
                Log.d("CardStackView", "onCardSwiped: " + direction.toString());

                if (direction == SwipeDirection.Right) {
//                    showRecordDialog(false);
                    moveToRecordActivity(pastCard.getId());
                    int currentPosition = cardStackView.getTopIndex();
                    try {
                        pastCard = adapter.getItem(currentPosition);
                        Log.d("CardStackView", "on card swiped topIndex: " + cardStackView.getTopIndex());
                    }catch (IndexOutOfBoundsException e){
                        tvEmpty.setVisibility(View.VISIBLE);
                        acceptButton.setVisibility(View.GONE);
                        rejectButton.setVisibility(View.GONE);
                    }
                } else if (direction == SwipeDirection.Left) {
                    Call<VoiceCard> request = NetRetrofit.getInstance(getContext()).getService().passVoice(pastCard.getId(), "Pass", null);
                    request.enqueue(new Callback<VoiceCard>() {
                        @Override
                        public void onResponse(Call<VoiceCard> call, Response<VoiceCard> response) {
                            if (response.isSuccessful()) {
                                int currentPosition = cardStackView.getTopIndex();
                                try {
                                    pastCard = adapter.getItem(currentPosition);
                                    Log.d("CardStackView", "on card swiped topIndex: " + cardStackView.getTopIndex());
                                }catch (IndexOutOfBoundsException e){
                                    tvEmpty.setVisibility(View.VISIBLE);
                                    acceptButton.setVisibility(View.GONE);
                                    rejectButton.setVisibility(View.GONE);
                                }
                                adapter.remove(pastCard);
                            } else {
                                try {
                                    Utils.parseError(response);
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

                /**
                 * 5장 남았을 경우 새로 불러오기
                 * */
                if (adapter.getCount() - cardStackView.getTopIndex() < 5) {
                    Log.d("CardStackView", "Paginate: " + cardStackView.getTopIndex());
//                    loadCard();
                }
            }

            @Override
            public void onCardReversed() {
                Log.d("CardStackView", "onCardReversed");
            }

            @Override
            public void onCardMovedToOrigin() {
                Log.d("CardStackView", "onCardMovedToOrigin");
            }

            @Override
            public void onCardClicked(int index) {
                Log.d("CardStackView", "onCardClicked: " + index);
            }
        });
    }

    private void moveToRecordActivity(Integer chatId) {
        Intent intent = new Intent(getActivity(), RecordActivity.class);
        intent.putExtra("chatId", chatId);
        startActivityForResult(intent, recordRequestCode);
//        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == recordRequestCode && resultCode == RESULT_CANCELED) {
            cardStackView.reverse();
        }
    }

    private void setMyChat(long currentTime, String voiceUrl) {
        User me = SharedPrefHelper.getInstance(getContext()).getUserInfo();
        VoiceChat myChat = new VoiceChat();
        myChat.setSendTime(currentTime);
        myChat.setVoiceUser(me);
        myChat.setVoiceUrl(voiceUrl);
        SharedPrefHelper.getInstance(getContext()).setMyChat(myChat);
    }

    private void setOtherChat() {
        VoiceChat otherChat = new VoiceChat();
        otherChat.setSendTime(1530584220607L);
        otherChat.setVoiceUrl(pastCard.getVoiceUrl());
        otherChat.setVoiceUser(pastCard.getUser());

        SharedPrefHelper.getInstance(getContext()).setOtherChat(otherChat);
    }

    private VoiceChatRoom setChatRoom(long currentTime) {
        VoiceChatRoom voiceChatRoom = new VoiceChatRoom();
        voiceChatRoom.setId(1);
        voiceChatRoom.setLastChatDate(currentTime);
        voiceChatRoom.setOpponentUser(pastCard.getUser());
        voiceChatRoom.setLeaved(false);
        return voiceChatRoom;
    }

    private void reload() {
        cardStackView.setVisibility(View.GONE);
        btnNewStory.setVisibility(View.GONE);
//        filterRow.setVisibility(View.GONE);
        filterRow.setVisibility(View.VISIBLE);
        acceptButton.setVisibility(View.GONE);
        rejectButton.setVisibility(View.GONE);
//        createUserCardAdapter();
        loadCard();
    }

    Call<ArrayList<VoiceCard>> cardRequest;

    private void loadCard() {
        Log.e(TAG, "load card start");
//        Call<VoiceCard> request = service.getRandomVoiceCard();
//        request.enqueue(new Callback<VoiceCard>() {
//            @Override
//            public void onResponse(Call<VoiceCard> call, Response<VoiceCard> response) {
//
//                Log.e(TAG, "load card response");
//                if (response.isSuccessful()){
//                    Log.e(TAG, "VoiceCard: " + response.body().toString());
//                    adapter.add(response.body());
//                    adapter.notifyDataSetChanged();
//                    Log.e(TAG, "load card end");
//                } else {
//                    Log.e(TAG, "load card error");
//                    try {
//                        Toast.makeText(getContext(), "error code : " + response.code() + " error body : " + response.errorBody().string(), Toast.LENGTH_SHORT).show();
//                        Log.e(TAG, "error code : " + response.code() + " error body : " + response.errorBody().string());
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//
//            @Override
//            public void onFailure(Call<VoiceCard> call, Throwable t) {
//
//            }
//        });
        Call<Filter> request = NetRetrofit.getInstance(getContext()).getService().checkFilter();
        request.enqueue(new Callback<Filter>() {
            @Override
            public void onResponse(Call<Filter> call, Response<Filter> response) {
                if (response.isSuccessful()) {
//                    TODO : 버그 수정후 주석 제거
//                    Filter filter = response.body();
//                    if (filter == null || (filter.getAgeMax() == 0 && filter.getAgeMin() == 0 && filter.getGender() == null && !filter.getActiveUser())) {
//                        cardRequest = NetRetrofit.getInstance(getContext()).getService().getRandomVoiceCard();
//                    } else {
//                        cardRequest = NetRetrofit.getInstance(getContext()).getService().getFilteredRandomVoiceCard();
//                    }
                    cardRequest = NetRetrofit.getInstance(getContext()).getService().getRandomVoiceCard();
                    cardRequest.enqueue(new Callback<ArrayList<VoiceCard>>() {
                        @Override
                        public void onResponse(Call<ArrayList<VoiceCard>> call, Response<ArrayList<VoiceCard>> response) {
                            if (response.isSuccessful()) {
                                if (adapter == null) {
                                    adapter = new UserCardAdapter(getContext());
                                    cardStackView.setAdapter(adapter);
                                }

                                adapter.addAll(response.body());
                                Log.e(TAG, "voice card size: " + response.body().size());

//                                adapter.notifyDataSetChanged();

                                cardStackView.setVisibility(View.VISIBLE);
                                btnNewStory.setVisibility(View.VISIBLE);
                                filterRow.setVisibility(View.VISIBLE);
                                filterRow.setVisibility(View.VISIBLE);
                                acceptButton.setVisibility(View.VISIBLE);
                                rejectButton.setVisibility(View.VISIBLE);
                                Log.e(TAG, "load card success");

                                try {
                                    int currentPosition = cardStackView.getTopIndex();
                                    pastCard = adapter.getItem(currentPosition);
                                    Log.d("CardStackView", "load card topIndex: " + cardStackView.getTopIndex());
                                } catch (IndexOutOfBoundsException e) {
                                    e.printStackTrace();
                                    pastCard = null;
                                    tvEmpty.setVisibility(View.VISIBLE);
                                    acceptButton.setVisibility(View.GONE);
                                    rejectButton.setVisibility(View.GONE);
                                }

                            } else {
                                try {
                                    Utils.toastError(getContext(), response);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<ArrayList<VoiceCard>> call, Throwable t) {
                            Log.e(TAG, t.getMessage());
                            t.printStackTrace();
                        }
                    });
                } else {
                    try {
                        Utils.toastError(getContext(), response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<Filter> call, Throwable t) {
                Log.e(TAG, t.getMessage());
                t.printStackTrace();
            }
        });
    }

//    private LinkedList<VoiceCard> extractRemainingProfiles() {
//        LinkedList<VoiceCard> voiceCards = new LinkedList<>();
//        for (int i = cardStackView.getTopIndex(); i < adapter.getCount(); i++) {
//            voiceCards.add(adapter.getItem(i));
//        }
//        return voiceCards;
//    }

//    private void addFirst() {
//        LinkedList<TouristSpot> spots = extractRemainingProfiles();
//        spots.addFirst(createTouristSpot());
//        adapter.clear();
//        adapter.addAll(spots);
//        adapter.notifyDataSetChanged();
//    }
//
//    private void addLast() {
//        LinkedList<TouristSpot> spots = extractRemainingProfiles();
//        spots.addLast(createTouristSpot());
//        adapter.clear();
//        adapter.addAll(spots);
//        adapter.notifyDataSetChanged();
//    }
//
//    private void removeFirst() {
//        LinkedList<TouristSpot> spots = extractRemainingProfiles();
//        if (spots.isEmpty()) {
//            return;
//        }
//
//        spots.removeFirst();
//        adapter.clear();
//        adapter.addAll(spots);
//        adapter.notifyDataSetChanged();
//    }
//
//    private void removeLast() {
//        LinkedList<TouristSpot> spots = extractRemainingProfiles();
//        if (spots.isEmpty()) {
//            return;
//        }
//
//        spots.removeLast();
//        adapter.clear();
//        adapter.addAll(spots);
//        adapter.notifyDataSetChanged();
//    }
//

    @OnClick(R.id.fl_new_story)
    public void onClickNewStory() {
        moveToRecordActivity(-1);
    }

    @OnClick(R.id.filter_shop)
    public void onClickFilterShop() {
        Toast.makeText(getContext(), "click filter shop", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getActivity(), ShopActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.filter_setting)
    public void onClickFilterSetting() {
        Intent intent = new Intent(getActivity(), FilterActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.filter_event)
    public void onClickFilterEvent() {
        Intent intent = new Intent(getActivity(), InviteActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.filter_luna)
    public void onClickPointLog() {
        Intent intent = new Intent(getActivity(), PointLogActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.ib_accept)
    public void onClickAccept() {
        swipeRight();
    }

    @OnClick(R.id.ib_reject)
    public void onClickReject() {
        swipeLeft();
    }

    /**
     * 위로 스와이핑 하는 기능이 필요할 경우 사용 할것
     * */
//    public void swipeTop() {
//        List<VoiceCard> spots = extractRemainingProfiles();
//        if (spots.isEmpty()) {
//            return;
//        }
//
//        View target = cardStackView.getTopView();
//        View targetOverlay = cardStackView.getTopView().getOverlayContainer();
//
//        ValueAnimator rotation = ObjectAnimator.ofPropertyValuesHolder(
//                target, PropertyValuesHolder.ofFloat("rotation", -10f));
//        rotation.setDuration(500);
//        ValueAnimator translateX = ObjectAnimator.ofPropertyValuesHolder(
//                target, PropertyValuesHolder.ofFloat("translationX", 0f, -2000f));
//        ValueAnimator translateY = ObjectAnimator.ofPropertyValuesHolder(
//                target, PropertyValuesHolder.ofFloat("translationY", 0f, -2000f));
//        translateX.setStartDelay(100);
//        translateY.setStartDelay(100);
//        translateX.setDuration(500);
//        translateY.setDuration(500);
//        AnimatorSet cardAnimationSet = new AnimatorSet();
//        cardAnimationSet.playTogether(translateY);
//
//        ObjectAnimator overlayAnimator = ObjectAnimator.ofFloat(targetOverlay, "alpha", 0f, 1f);
//        overlayAnimator.setDuration(200);
//        AnimatorSet overlayAnimationSet = new AnimatorSet();
//        overlayAnimationSet.playTogether(overlayAnimator);
//
//        cardStackView.swipe(SwipeDirection.Top, cardAnimationSet, overlayAnimationSet);
//    }

    public void swipeLeft() {
        View target = cardStackView.getTopView();
        View targetOverlay = cardStackView.getTopView().getOverlayContainer();

        ValueAnimator rotation = ObjectAnimator.ofPropertyValuesHolder(
                target, PropertyValuesHolder.ofFloat("rotation", -40f));
        rotation.setDuration(1000);

        ValueAnimator translateX = ObjectAnimator.ofPropertyValuesHolder(
                target, PropertyValuesHolder.ofFloat("translationX", 0f, -2000f));

        ValueAnimator translateY = ObjectAnimator.ofPropertyValuesHolder(
                target, PropertyValuesHolder.ofFloat("translationY", 0f, 500f));
        translateX.setDuration(1000);
        translateY.setDuration(1000);
        AnimatorSet cardAnimationSet = new AnimatorSet();
        cardAnimationSet.playTogether(rotation, translateX, translateY);

        ObjectAnimator overlayAnimator = ObjectAnimator.ofFloat(targetOverlay, "alpha", 0f, 1f);
        overlayAnimator.setDuration(200);
        AnimatorSet overlayAnimationSet = new AnimatorSet();
        overlayAnimationSet.playTogether(overlayAnimator);

        cardStackView.swipe(SwipeDirection.Left, cardAnimationSet, overlayAnimationSet);
    }

    public void swipeRight() {
        View target = cardStackView.getTopView();
        View targetOverlay = cardStackView.getTopView().getOverlayContainer();

        ValueAnimator rotation = ObjectAnimator.ofPropertyValuesHolder(
                target, PropertyValuesHolder.ofFloat("rotation", 40f));
        rotation.setDuration(1000);
        ValueAnimator translateX = ObjectAnimator.ofPropertyValuesHolder(
                target, PropertyValuesHolder.ofFloat("translationX", 0f, 2000f));
        ValueAnimator translateY = ObjectAnimator.ofPropertyValuesHolder(
                target, PropertyValuesHolder.ofFloat("translationY", 0f, -500f));
        translateX.setDuration(1000);
        translateY.setDuration(1000);
        AnimatorSet cardAnimationSet = new AnimatorSet();
        cardAnimationSet.playTogether(rotation, translateX, translateY);

        ObjectAnimator overlayAnimator = ObjectAnimator.ofFloat(targetOverlay, "alpha", 0f, 1f);
        overlayAnimator.setDuration(200);
        AnimatorSet overlayAnimationSet = new AnimatorSet();
        overlayAnimationSet.playTogether(overlayAnimator);

        cardStackView.swipe(SwipeDirection.Right, cardAnimationSet, overlayAnimationSet);

    }

    private void reverse() {
        cardStackView.reverse();
    }

    public class UserCardAdapter extends ArrayAdapter<VoiceCard> {
        int duration;
        int CURRENT_STATE = 2;
        final int STATE_PLAY = 1;
        final int STATE_STOP = 2;
        byte[] sample = Utils.getSampleWave();

        public UserCardAdapter(@NonNull Context context) {
            super(context, 0);
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.card_user_view, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            VoiceCard voiceCard = getItem(position);
            User cardUser = voiceCard.getUser();

            SpannableStringBuilder s = Utils.setNameAndAge(cardUser.getName(), AgeUtil.getAgeFromBirth(cardUser.getBirth()));
            s.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.age_white_color)), cardUser.getName().length(), s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            holder.nameAgeTxt.setText(s);

            if (myInfo != null)
                holder.tvDistance.setText("" + DistanceUtil.getDistanceFromLatLng(cardUser, myInfo) + "km");

            Glide.with(getContext())
                    .load(cardUser.getProfileImageUrl())
//                    .apply(RequestOptions.bitmapTransform(new BlurTransformation(25, 3)))
                    .into(holder.profileImage);

            holder.ivReport.setOnClickListener(v -> {
                MaterialDialog reportDialog = new MaterialDialog.Builder(getActivity())
                        .customView(R.layout.dialog_report, false)
                        .show();

                DialogUtils.initDialogView(reportDialog, getContext());
                RadioGroup radioGroup = (RadioGroup) reportDialog.findViewById(R.id.radioGroup);
                TextView cancelButton = (TextView) reportDialog.findViewById(R.id.tv_cancel);
                cancelButton.setOnClickListener(v1 -> reportDialog.dismiss());
                TextView reportButton = (TextView) reportDialog.findViewById(R.id.tv_report);
                reportButton.setOnClickListener(v1 -> {
                    int currentPosition = cardStackView.getTopIndex();
                    VoiceCard reportCard = adapter.getItem(currentPosition);
                    RadioButton selectButton = radioGroup.findViewById(radioGroup.getCheckedRadioButtonId());

                    if (selectButton == null) {
                        Toast.makeText(getContext(), "이유를 선택해주세요", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Call<VoiceCard> request = service.passVoice(reportCard.getId(), "Report", selectButton.getText().toString());
                    request.enqueue(new Callback<VoiceCard>() {
                        @Override
                        public void onResponse(Call<VoiceCard> call, Response<VoiceCard> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(), "신고가 접수되었습니다", Toast.LENGTH_SHORT).show();
                                reportDialog.dismiss();
                            } else {
                                try {
                                    Utils.toastError(getActivity(), response);
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
                });
            });
            holder.tvCreateTime.setText(Html.fromHtml(Utils.getCardCreatedAt(voiceCard.getCreatedAt())));
            holder.waveView.setScaledData(sample);
            holder.waveView.setEnabled(false);

            duration = VoicePlayerManager.getInstance().voicePlay(voiceCard.getVoiceUrl());
            Log.e(TAG, "duration: " + duration);
            VoicePlayerManager.getInstance().voicePlayStop();

            ObjectAnimator progressAnim = ObjectAnimator.ofFloat(holder.waveView, "progress", 0F, 100F);
            progressAnim.setInterpolator(new LinearInterpolator());
            progressAnim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    CURRENT_STATE = STATE_STOP;
                    holder.playButton.post(() -> {
                        holder.playButton.setImageResource(R.drawable.ic_reload_white);
                    });
                    holder.tvPlayState.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            progressAnim.setDuration(duration);

            holder.playButton.setOnClickListener((View v) -> {

                switch (CURRENT_STATE) {
                    case STATE_STOP:
                        CURRENT_STATE = STATE_PLAY;
                        //PLAY

                        getActivity().runOnUiThread(()->{
                            if(holder.waveView.getVisibility() == View.INVISIBLE){
                                holder.waveView.setVisibility(View.VISIBLE);
                                holder.tvTimeStart.setVisibility(View.VISIBLE);
                                holder.tvTimeEnd.setVisibility(View.VISIBLE);
                            }
                            holder.tvPlayState.setVisibility(View.VISIBLE);
                            holder.tvPlayState.setText("터치하여 일시 정지하기");
                            holder.playButton.setImageResource(R.drawable.ic_pause);
                        });


                        if (progressAnim.isPaused()) {
                            progressAnim.resume();
                            VoicePlayerManager.getInstance().voicePlayResume();
                        } else {
                            progressAnim.start();
                            VoicePlayerManager.getInstance().voicePlay(voiceCard.getVoiceUrl());
                        }

                        break;

                    case STATE_PLAY:
                        CURRENT_STATE = STATE_STOP;
                        if (progressAnim != null) {
//                            progressAnim.cancel();
                            progressAnim.pause();
                        }

                        getActivity().runOnUiThread(()->{
                            holder.tvTimeStart.setText(Utils.getPlayTimeFormat(0));
                            holder.tvPlayState.setVisibility(View.VISIBLE);
                            holder.tvPlayState.setText("터치하여 재생하기");
                            holder.playButton.setImageResource(R.drawable.ic_play);
                        });

                        VoicePlayerManager.getInstance().voicePlayPause();
                        break;
                }

            });
            holder.tvTimeStart.setText(Utils.getPlayTimeFormat(0));
            holder.tvTimeEnd.setText(Utils.getPlayTimeFormat(duration));
            holder.tvPlayState.setVisibility(View.INVISIBLE);


            return convertView;
        }

        @Override
        public int getPosition(@Nullable VoiceCard item) {
            return super.getPosition(item);
        }

        @Override
        public int getCount() {
            return super.getCount();
        }

        @Nullable
        @Override
        public VoiceCard getItem(int position) {
            return super.getItem(position);
        }

        public class ViewHolder {
            @BindView(R.id.profileImageView)
            ImageView profileImage;
            @BindView(R.id.nameAgeTxt)
            TextView nameAgeTxt;
            @BindView(R.id.tv_distance)
            TextView tvDistance;
            @BindView(R.id.tv_create_time)
            TextView tvCreateTime;
            @BindView(R.id.tv_report)
            TextView ivReport;
            @BindView(R.id.wave)
            AudioWaveView waveView;
            @BindView(R.id.iv_play_button)
            ImageView playButton;
            @BindView(R.id.tv_time_start)
            TextView tvTimeStart;
            @BindView(R.id.tv_time_end)
            TextView tvTimeEnd;
            @BindView(R.id.tv_play_state)
            TextView tvPlayState;

            private ViewHolder(View view) {
                ButterKnife.bind(this, view);
            }
        }
    }
}
