package com.square.renov.swipevoicechat.Fragment;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.square.renov.swipevoicechat.Activity.FilterActivity;
import com.square.renov.swipevoicechat.Event.RefreshEvent;
import com.square.renov.swipevoicechat.Model.Profile;
import com.square.renov.swipevoicechat.Model.User;
import com.square.renov.swipevoicechat.Model.VoiceCard;
import com.square.renov.swipevoicechat.Network.NetRetrofit;
import com.square.renov.swipevoicechat.Network.ApiService;
import com.square.renov.swipevoicechat.R;
import com.square.renov.swipevoicechat.Util.AgeUtil;
import com.square.renov.swipevoicechat.Util.DistanceUtil;
import com.square.renov.swipevoicechat.Utils;
import com.square.renov.swipevoicechat.widget.VoicePlayerManager;
import com.square.renov.swipevoicechat.widget.VoicePlayerView;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.SwipeDirection;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.wasabeef.glide.transformations.BlurTransformation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CardFragment extends Fragment {

    private static final String TAG = CardFragment.class.getSimpleName();
    @BindView(R.id.activity_main_progress_bar)
    public ProgressBar progressBar;
    @BindView(R.id.activity_main_card_stack_view)
    public CardStackView cardStackView;
    @BindView(R.id.tv_new_story)
    public TextView btnNewStory;
    @BindView(R.id.tr_filter)
    public ConstraintLayout filterRow;
    //    private TouristSpotCardAdapter adapter;
    private UserCardAdapter adapter;

    private String[] permissions = {Manifest.permission.RECORD_AUDIO};
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    ApiService service = NetRetrofit.getInstance(getContext()).getService();

    User myInfo;

    public Unbinder unbinder;

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
        EventBus.getDefault().register(this);
        myInfo = (User) getArguments().getParcelable("user");
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


    @Subscribe
    public void onRefreshEvent(RefreshEvent refreshEvent){
        Log.e("event bus","onRefreshEvent(): " + CardFragment.class.getSimpleName());

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        unbinder.unbind();
    }

    private UserCardAdapter createUserCardAdapter() {
        final UserCardAdapter adapter = new UserCardAdapter(getContext());
        for(VoiceCard card : Utils.loadCards(getContext())){
            adapter.add(card);
        }

//        for (Profile profile : Utils.loadProfiles(getContext())) {
//            adapter.add(profile);
//        }
        return adapter;
    }

    private void setup() {
        cardStackView.setSwipeDirection(SwipeDirection.HORIZONTAL);
        cardStackView.setCardEventListener(new CardStackView.CardEventListener() {
            @Override
            public void onCardDragging(float percentX, float percentY) {
                Log.d("CardStackView", "onCardDragging");
            }

            @Override
            public void onCardSwiped(SwipeDirection direction) {
                Log.d("CardStackView", "onCardSwiped: " + direction.toString());
                Log.d("CardStackView", "topIndex: " + cardStackView.getTopIndex());
                if (direction == SwipeDirection.Right) {
                    showRecordDialog(false);
                }


                if (cardStackView.getTopIndex() == adapter.getCount() - 5) {
                    Log.d("CardStackView", "Paginate: " + cardStackView.getTopIndex());
                    paginate();
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

    private void showRecordDialog(boolean isNewStory) {
        ResultReceiver resultReceiver = new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                int OK_CODE = 1000;
                if (resultCode == OK_CODE && getActivity() != null) {
                    Toast.makeText(getActivity(), "음성이 발송되었습니다", Toast.LENGTH_SHORT).show();

                    String voice_url_path = resultData.getString(VoiceDialogFragment.EXTRA_RESULT_DATA);
                    Call<VoiceCard> request = null;

                    if(isNewStory){
                        Log.d(TAG,"voice Url : " + voice_url_path);
                        request = service.sendNewVoice(voice_url_path);
                    } else {
//                        request = service.sendChatVoice()
                    }

                    request.enqueue(new Callback<VoiceCard>() {
                        @Override
                        public void onResponse(Call<VoiceCard> call, Response<VoiceCard> response) {
                            if(response.isSuccessful()){
                                Toast.makeText(getContext(), "성공적으로 전송했습니다.", Toast.LENGTH_SHORT).show();
                            } else {
                                try {
                                    Log.e(TAG, "error code: " + response.code() + " error body: " + response.errorBody().string());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<VoiceCard> call, Throwable t) {

                        }
                    });


//                    user.setVoice(voice_url_path);
//
//                    LogUtil.d("voice resultCode: " + resultCode);
//                    LogUtil.d("voice url path: " + voice_url_path);
//
//                    voiceImageView.setImageResource(R.drawable.btn_play_profile);
//
//                    confirmButton.setVisibility(View.VISIBLE);
                }
//                List<NameValuePair> paramInfo = new ArrayList<>();
//                paramInfo.add(new BasicNameValuePair("userId", user.getId()));
//                paramInfo.add(new BasicNameValuePair("urlPath", fileUrl));
//                HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(Constants.URL_STORY_NEW_START;, StoryUpdate.class, paramInfo, HttpMethod.POST, getApplicationContext());
//                RequestFactory requestFactory = new RequestFactory();
//                requestFactory.setProgressHandler(new ProgressHandler(activity, false));
//                requestFactory.create(httpRequestVO, new HttpResponseCallback<StoryUpdate>() {
//                    @Override
//                    public void onResponse(StoryUpdate result) {
//                        // reponse: {"code":"OK"}
//                        LogUtil.d("result: " + result.toString());
////                        Log.e(TAG, result.toString());
//
//                        updateTime = result.getUpdateTime();
//                        SharedPrefHelper.getInstance(getApplicationContext()).setSharedPreferences(SharedPrefHelper.STORY_UPDATE_TIME, updateTime);
//                    }
//
//                    @Override
//                    public void onError(HttpNetworkError error) {
//                        LogUtil.e("onClickBtnNewStory", error);
////                        Log.e(TAG, "onClickBtnNewStory", error);
//                    }
//                }).execute();
            }
        };
        VoiceDialogFragment voiceDialogFragment = VoiceDialogFragment.newInstance(null, resultReceiver);
        String title;
        if(isNewStory)
             title = "새로운 이야기 시작하기";
        else
             title = "답장하기";
        voiceDialogFragment.setTitle(title);
        voiceDialogFragment.setDescription("3초 이상 녹음해주세요!");
        voiceDialogFragment.setExample("오늘 집에서 쉬는데, 저처럼 \n 평일인데 쉬시는분 있나요?");
        voiceDialogFragment.show(getFragmentManager(),"VoiceDialogFragment");

//        boolean wrapInScrollView = false;
//        MaterialDialog materialDialog = new MaterialDialog.Builder(getContext())
//                .title(isNewStory ? "새이야기 시작하기" : "답장하기")
//                .customView(R.layout.dialog_record, wrapInScrollView)
//                .positiveText("보내기")
//                .onPositive((dialog, which) -> {
//                    int currentPosition = cardStackView.getTopIndex() > 0 ? cardStackView.getTopIndex() - 1 : 0;
//                    EventBus.getDefault().post(new RefreshEvent(RefreshEvent.Action.SEND_NEW_STORY, currentPosition));
//                    Toast.makeText(getContext(), "정상적으로 답장을 보냈습니다.", Toast.LENGTH_SHORT).show();
//                })
//                .negativeText("취소")
//                .onNegative((dialog, which) -> {
//                    if(!isNewStory)
//                        reverse();
//                    else
//                        dialog.dismiss();
//                })
//                .show();
//
//
//
//        View view = materialDialog.getView();
//        TextView tvRecordAgain = (TextView) view.findViewById(R.id.tv_record_again);
//        tvRecordAgain.setVisibility(View.GONE);
//        tvRecordAgain.setText("다시하기");
//        TextView tvRecordDesc = (TextView) view.findViewById(R.id.tv_record_desc);
//        tvRecordDesc.setVisibility(View.VISIBLE);
//        tvRecordDesc.setText("3초 이상 녹음해주세요");
//        TextView tvExampleDesc = (TextView) view.findViewById(R.id.tv_example_desc);
//        tvExampleDesc.setText("오늘 집에서 쉬는데, 저처럼 평일인데 쉬시는 분 있나요?");
//        VoicePlayerManager voicePlayerManager = VoicePlayerManager.getInstance();
//        VoicePlayerView voicePlayerView = view.findViewById(R.id.voice_player_view);
//        voicePlayerView.setVoiceRecordListener(new VoicePlayerView.VoiceRecordListener() {
//            @Override
//            public void onRecord() {
////                voicePlayerManager.voicePlayStop();
////                voicePlayerManager.voiceRecord(getContext());
//                voicePlayerView.startRecordProgress(30000);
//            }
//
//            @Override
//            public void onStopRecord() {
////                voicePlayerManager.voiceRecordStop();
//                voicePlayerView.prepareVoicePlay();
//            }
//
//            @Override
//            public void onPlay() {
//                String url = "http://s3-ap-northeast-1.amazonaws.com/pesofts-image/voiceChat/20180528/3359781527498366440.m4a";
//                int duration = voicePlayerManager.voicePlay(url);
//                voicePlayerView.startVoicePlayProgress(duration);
//            }
//
//            @Override
//            public void onStopPlay() {
//                voicePlayerManager.voicePlayStop();
//            }
//        });
    }

    private void reload() {
        cardStackView.setVisibility(View.GONE);
        btnNewStory.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        filterRow.setVisibility(View.GONE);



        new Handler().postDelayed(() -> {
//                adapter = createTouristSpotCardAdapter();
            adapter = createUserCardAdapter();

            loadCard();

            cardStackView.setAdapter(adapter);
            cardStackView.setVisibility(View.VISIBLE);
//            TODO: 새로운 이야기 시작 버튼 보이는 시점
            btnNewStory.setVisibility(View.VISIBLE);
            filterRow.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            filterRow.setVisibility(View.VISIBLE);
        }, 1000);
    }

    //TODO: API 구현중
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
        Call<VoiceCard> response = NetRetrofit.getInstance(getContext()).getService().getRandomVoiceCard();
        response.enqueue(new Callback<VoiceCard>() {
            @Override
            public void onResponse(Call<VoiceCard> call, Response<VoiceCard> response) {
                if(response.isSuccessful()){
                    adapter.add(response.body());
                    adapter.notifyDataSetChanged();
                    Log.e(TAG, "load card success");
                } else {
                    Log.e(TAG, "load card error");
                    Log.e(TAG, "error code: " + response.code());
                    try {
                        Log.e(TAG, "error body: " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<VoiceCard> call, Throwable t) {

            }
        });
    }

    private LinkedList<VoiceCard> extractRemainingProfiles() {
        LinkedList<VoiceCard> voiceCards = new LinkedList<>();
        for (int i = cardStackView.getTopIndex(); i < adapter.getCount(); i++) {
            voiceCards.add(adapter.getItem(i));
        }
        return voiceCards;
    }

//    private void addFirst() {
//        LinkedList<TouristSpot> spots = extractRemainingProfiles();
//        spots.addFirst(createTouristSpot());
//        adapter.clear();
//        adapter.addAll(spots);
//        adapter.notifyDataSetChanged();
//    }

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

    @OnClick(R.id.tv_new_story)
    public void onClickNewStory() {
        showRecordDialog(true);
    }

    @OnClick(R.id.filter_shop)
    public void onClickFilterShop(){
        Toast.makeText(getContext(), "click filter shop", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.filter_setting)
    public void onClickFilterSetting(){
        Intent intent = new Intent(getActivity(), FilterActivity.class);
        startActivity(intent);

//        boolean wrapInScrollView = true;
//        new MaterialDialog.Builder(getContext())
//                .title(R.string.filter_title)
//                .customView(R.layout.dialog_filter, wrapInScrollView)
//                .positiveText("확인")
//                .show();
    }


    private void paginate() {
        cardStackView.setPaginationReserved();
        for (VoiceCard voiceCard: Utils.loadCards(this.getContext())) {
            adapter.add(voiceCard);
        }
//        adapter.addAll();
//        adapter.addAll(createTouristSpots());
        adapter.notifyDataSetChanged();
    }

    public void swipeTop() {
        List<VoiceCard> spots = extractRemainingProfiles();
        if (spots.isEmpty()) {
            return;
        }

        View target = cardStackView.getTopView();
        View targetOverlay = cardStackView.getTopView().getOverlayContainer();

        ValueAnimator rotation = ObjectAnimator.ofPropertyValuesHolder(
                target, PropertyValuesHolder.ofFloat("rotation", -10f));
        rotation.setDuration(500);
        ValueAnimator translateX = ObjectAnimator.ofPropertyValuesHolder(
                target, PropertyValuesHolder.ofFloat("translationX", 0f, -2000f));
        ValueAnimator translateY = ObjectAnimator.ofPropertyValuesHolder(
                target, PropertyValuesHolder.ofFloat("translationY", 0f, -2000f));
        translateX.setStartDelay(100);
        translateY.setStartDelay(100);
        translateX.setDuration(500);
        translateY.setDuration(500);
        AnimatorSet cardAnimationSet = new AnimatorSet();
        cardAnimationSet.playTogether(translateY);

        ObjectAnimator overlayAnimator = ObjectAnimator.ofFloat(targetOverlay, "alpha", 0f, 1f);
        overlayAnimator.setDuration(200);
        AnimatorSet overlayAnimationSet = new AnimatorSet();
        overlayAnimationSet.playTogether(overlayAnimator);

        cardStackView.swipe(SwipeDirection.Top, cardAnimationSet, overlayAnimationSet);
    }

    public void swipeLeft() {
        List<VoiceCard> spots = extractRemainingProfiles();
        if (spots.isEmpty()) {
            return;
        }

        View target = cardStackView.getTopView();
        View targetOverlay = cardStackView.getTopView().getOverlayContainer();

        ValueAnimator rotation = ObjectAnimator.ofPropertyValuesHolder(
                target, PropertyValuesHolder.ofFloat("rotation", -40f));
        rotation.setDuration(1000);

        ValueAnimator translateX = ObjectAnimator.ofPropertyValuesHolder(
                target, PropertyValuesHolder.ofFloat("translationX", 0f, -2000f));

        ValueAnimator translateY = ObjectAnimator.ofPropertyValuesHolder(
                target, PropertyValuesHolder.ofFloat("translationY", 0f, 500f));

//        translateX.setStartDelay(100);
//        translateY.setStartDelay(100);
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
        List<VoiceCard> spots = extractRemainingProfiles();
        if (spots.isEmpty()) {
            return;
        }

        View target = cardStackView.getTopView();
        View targetOverlay = cardStackView.getTopView().getOverlayContainer();

        ValueAnimator rotation = ObjectAnimator.ofPropertyValuesHolder(
                target, PropertyValuesHolder.ofFloat("rotation", 40f));
        rotation.setDuration(1000);
        ValueAnimator translateX = ObjectAnimator.ofPropertyValuesHolder(
                target, PropertyValuesHolder.ofFloat("translationX", 0f, 2000f));
        ValueAnimator translateY = ObjectAnimator.ofPropertyValuesHolder(
                target, PropertyValuesHolder.ofFloat("translationY", 0f, -500f));
//        translateX.setStartDelay(100);
//        translateY.setStartDelay(100);
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

    public class TouristSpot {
        public String name;
        public String city;
        public String url;

        public TouristSpot(String name, String city, String url) {
            this.name = name;
            this.city = city;
            this.url = url;
        }
    }

    public class UserCardAdapter extends ArrayAdapter<VoiceCard> {

        VoicePlayerManager voicePlayerManager = VoicePlayerManager.getInstance();

        public UserCardAdapter(@NonNull Context context) {
            super(context, 0);
        }

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
            holder.nameAgeTxt.setText(cardUser.getName() + ", " + AgeUtil.getAgeFromBirth(cardUser.getBirth()));
            if(myInfo != null)
                holder.locationNameTxt.setText("" + DistanceUtil.getDistanceFromLatLng(cardUser, myInfo) + "km");

            Glide.with(getContext())
                    .load(cardUser.getProfileImageUrl())
                    .apply(RequestOptions.bitmapTransform(new BlurTransformation(25, 3)))
                    .into(holder.profileImage);

            holder.rejectBtn.setOnClickListener(v -> swipeLeft());
            holder.acceptBtn.setOnClickListener(v -> swipeRight());
            holder.ivReport.setOnClickListener(v -> {
                new MaterialDialog.Builder(getContext())
                        .title("신고하기")
                        .items(R.array.report)
                        .itemsCallbackSingleChoice(0, (dialog, itemView, which, text) -> {
                            Toast.makeText(getContext(), "신고가 접수되었습니다", Toast.LENGTH_SHORT).show();
                            return true;
                        })
                        .positiveText("신고하기")
                        .negativeText("취소")
                        .show();
            });

            holder.voicePlayerView.prepareVoicePlay();
            holder.voicePlayerView.setVoiceRecordListener(new VoicePlayerView.VoiceRecordListener() {
                @Override
                public void onRecord() {

                }

                @Override
                public void onStopRecord() {

                }

                @Override
                public void onPlay() {
                    String url = "http://s3-ap-northeast-1.amazonaws.com/pesofts-image/voiceChat/20180528/3359781527498366440.m4a";
                    int duration = voicePlayerManager.voicePlay(url);
                    holder.voicePlayerView.startVoicePlayProgress(duration);
                    voicePlayerManager.voicePlayStop();
                }

                @Override
                public void onStopPlay() {

                }
            });

            return convertView;
        }

        @Override
        public int getPosition(@Nullable VoiceCard item) {
            return super.getPosition(item);
        }

        public class ViewHolder {
            @BindView(R.id.profileImageView)
            public ImageView profileImage;
            @BindView(R.id.voice_player_view)
            VoicePlayerView voicePlayerView;
            @BindView(R.id.nameAgeTxt)
            public TextView nameAgeTxt;
            @BindView(R.id.locationNameTxt)
            public TextView locationNameTxt;
            @BindView(R.id.rejectBtn)
            public Button rejectBtn;
            @BindView(R.id.acceptBtn)
            public Button acceptBtn;
            @BindView(R.id.iv_report)
            public ImageView ivReport;

            private ViewHolder(View view) {
                ButterKnife.bind(this, view);
            }
        }
    }
}
