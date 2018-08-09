package com.square.renov.swipevoicechat.Fragment;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
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
import android.view.animation.LinearInterpolator;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.request.RequestOptions;
import com.square.renov.swipevoicechat.Activity.EventActivity;
import com.square.renov.swipevoicechat.Activity.FilterActivity;
import com.square.renov.swipevoicechat.Activity.PointLogActivity;
import com.square.renov.swipevoicechat.Activity.RecordActivity;
import com.square.renov.swipevoicechat.Activity.ShopActivity;
import com.square.renov.swipevoicechat.Model.Filter;
import com.square.renov.swipevoicechat.Model.Result;
import com.square.renov.swipevoicechat.Model.User;
import com.square.renov.swipevoicechat.Model.VoiceCard;
import com.square.renov.swipevoicechat.Model.VoiceChatRoom;
import com.square.renov.swipevoicechat.Network.NetRetrofit;
import com.square.renov.swipevoicechat.Network.ApiService;
import com.square.renov.swipevoicechat.R;
import com.square.renov.swipevoicechat.Util.AgeUtil;
import com.square.renov.swipevoicechat.Util.DialogUtils;
import com.square.renov.swipevoicechat.Util.DistanceUtil;
import com.square.renov.swipevoicechat.Util.RealmHelper;
import com.square.renov.swipevoicechat.Util.SharedPrefHelper;
import com.square.renov.swipevoicechat.Util.Utils;
import com.square.renov.swipevoicechat.widget.BounceBgView;
import com.square.renov.swipevoicechat.widget.VoicePlayerManager;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.SwipeDirection;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.realm.Realm;
import io.realm.RealmResults;
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
    @BindView(R.id.empty_view_bounce_bg)
    ConstraintLayout emptyLayout;
    @BindView(R.id.id_bg_profile)
    ImageView bgProfile;
    @BindView(R.id.id_bg_bounce)
    BounceBgView bgBounce;


    //    private TouristSpotCardAdapter adapter;
    private UserCardAdapter adapter;

    private String[] permissions = {Manifest.permission.RECORD_AUDIO};
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    int recordRequestCode = 1234;
    int filterRequestCode = 1235;

    ApiService service = NetRetrofit.getInstance(getContext()).getService();

    User myInfo;

    public Unbinder unbinder;
    public VoiceCard pastCard;
    private boolean isActive;

    MultiTransformation multiTransformation = new MultiTransformation(new CircleCrop(),
            new FitCenter());


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
        myInfo = SharedPrefHelper.getInstance(getActivity()).getUserInfo();
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
//        if (pastCard != null) {
//            RealmResults<VoiceCard> results = realm.where(VoiceCard.class).equalTo("id", pastCard.getId()).findAll();
//            realm.executeTransaction(realm1 -> {
//                if (results.size() > 0)
//                    results.deleteAllFromRealm();
//                Log.d(TAG, "delete filter card");
//            });
//        }
    }


    @Override
    public void onDestroyView() {

        super.onDestroyView();
        bgBounce.stopBounceAnimation();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        Main Logic here
        setup();
        reload();
        initEmptyView();
//        visibleEmptyView();
        ActivityCompat.requestPermissions(getActivity(), permissions, REQUEST_RECORD_AUDIO_PERMISSION);
    }

    private void initEmptyView() {
        Glide.with(getContext())
                .load(myInfo.getProfileImageUrl())
                .apply(RequestOptions.bitmapTransform(multiTransformation))
                .into(bgProfile);
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

    /**
     * 샘플 데이터 삽입용
     */
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
            visibleEmptyView();

        }
//        for (Profile profile : Utils.loadProfiles(getContext())) {
//            adapter.add(profile);
//        }
    }

    private void visibleEmptyView() {
        emptyLayout.setVisibility(View.VISIBLE);
        cardStackView.setVisibility(View.GONE);
        acceptButton.setVisibility(View.GONE);
        rejectButton.setVisibility(View.GONE);
        btnNewStory.setVisibility(View.VISIBLE);
        filterRow.setVisibility(View.VISIBLE);
        bgBounce.startBounceAnimation();
    }

    private void invisibleEmptyView() {
        cardStackView.setVisibility(View.VISIBLE);
        btnNewStory.setVisibility(View.VISIBLE);
        filterRow.setVisibility(View.VISIBLE);
        acceptButton.setVisibility(View.VISIBLE);
        rejectButton.setVisibility(View.VISIBLE);
        emptyLayout.setVisibility(View.GONE);
        bgBounce.stopBounceAnimation();
    }

    Realm realm = RealmHelper.getRealm(RealmHelper.FILTER_CARD);

    private void setup() {
//        filterLuna.setText("" + myInfo.getLuna());
        filterLuna.setText(Utils.setLunaCount(myInfo.getLuna()));
        cardStackView.setSwipeThreshold(0.5f);
        cardStackView.setSwipeDirection(SwipeDirection.HORIZONTAL);
        cardStackView.setCardEventListener(new CardStackView.CardEventListener() {
            @Override
            public void onCardDragging(float percentX, float percentY) {
                Log.d("CardStackView", "onCardDragging");
                Log.d("CardStackView", "top index : " + cardStackView.getTopIndex());
            }

            @Override
            public void onCardSwiped(SwipeDirection direction) {
                Log.d("CardStackView", "onCardSwiped: " + direction.toString());

                if (adapter.CURRENT_STATE == adapter.STATE_PLAY)
                    adapter.cardPlayStop();

                if (direction == SwipeDirection.Right) {
//                    showRecordDialog(false);
                    moveToRecordActivity(pastCard.getId());
                    int currentPosition = cardStackView.getTopIndex();
                    try {
                        pastCard = adapter.getItem(currentPosition);
                        Log.d("CardStackView", "on card swiped topIndex: " + cardStackView.getTopIndex());
                    } catch (IndexOutOfBoundsException e) {
                        visibleEmptyView();
                    }
                } else if (direction == SwipeDirection.Left) {
//                    try {
//                        int currentPosition = cardStackView.getTopIndex();
//                        pastCard = adapter.getItem(currentPosition);
//                        Log.d("CardStackView", "on card swiped topIndex: " + cardStackView.getTopIndex());
//                    } catch (IndexOutOfBoundsException e) {
//                        visibleEmptyView();
//                    }
                    //TODO 필터 데이터 삭제 시점1 건너뛰기
                    Call<VoiceCard> request = NetRetrofit.getInstance(getContext()).getService().passVoice(pastCard.getId(), "Pass", null);
                    request.enqueue(new Callback<VoiceCard>() {
                        @Override
                        public void onResponse(Call<VoiceCard> call, Response<VoiceCard> response) {
                            RealmResults<VoiceCard> results = realm.where(VoiceCard.class).equalTo("id", pastCard.getId()).findAll();
                            realm.executeTransaction(realm1 -> {
                                if (results.size() > 0) {
                                    results.deleteAllFromRealm();
                                }
                                Log.d(TAG, "delete filter card");
                            });

                            if (response.isSuccessful()) {
                                try {
                                    int currentPosition = cardStackView.getTopIndex();
                                    if (adapter != null)
                                        pastCard = adapter.getItem(currentPosition);
                                    else
                                        pastCard = null;


                                    Log.d("CardStackView", "on card swiped topIndex: " + cardStackView.getTopIndex());
                                } catch (IndexOutOfBoundsException e) {
                                    visibleEmptyView();
                                }
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
                }

                /**
                 * 5장 남았을 경우 새로 불러오기
                 * */
                if (adapter.getCount() - cardStackView.getTopIndex() < 5) {
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

    private void paginate() {
        cardStackView.setPaginationReserved();
        adapter.addAll();
        adapter.notifyDataSetChanged();
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

        if (requestCode == recordRequestCode) {
            if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, "RESULT_CANCELED");
                cardStackView.reverse();
            } else if (resultCode == RESULT_OK) {
                Log.d(TAG, "RESULT_OK");
                //TODO : 필터 데이터 삭제 시점 2 -> 답장하기
                if (data.hasExtra("chatRoomId") && data.getIntExtra("chatRoomId", -1) != -1) {
                    realm.executeTransaction(realm1 -> {
                        RealmResults<VoiceCard> results = realm1.where(VoiceCard.class).equalTo("id", pastCard.getId()).findAll();
                        results.deleteAllFromRealm();
                    });
                }
            } else {
                Log.d(TAG, "ELSE");
            }
        } else if (requestCode == filterRequestCode && resultCode == RESULT_OK) {
            Log.d(TAG, "filter ok");
            if (adapter != null) {
                adapter.clear();
                adapter.notifyDataSetChanged();
            }
            loadCard();
        }
    }

//    private void setMyChat(long currentTime, String voiceUrl) {
//        User me = SharedPrefHelper.getInstance(getContext()).getUserInfo();
//        VoiceChat myChat = new VoiceChat();
//        myChat.setSendTime(currentTime);
//        myChat.setVoiceUser(me);
//        myChat.setVoiceUrl(voiceUrl);
//        SharedPrefHelper.getInstance(getContext()).setMyChat(myChat);
//    }
//    private void setOtherChat() {
//        VoiceChat otherChat = new VoiceChat();
//        otherChat.setSendTime(1530584220607L);
//        otherChat.setVoiceUrl(pastCard.getVoiceUrl());
//        otherChat.setVoiceUser(pastCard.getUser());
//
//        SharedPrefHelper.getInstance(getContext()).setOtherChat(otherChat);
//    }

    private VoiceChatRoom setChatRoom(long currentTime) {
        VoiceChatRoom voiceChatRoom = new VoiceChatRoom();
        voiceChatRoom.setId(1);
        voiceChatRoom.setLastChatDate(currentTime);
        voiceChatRoom.setOpponentUser(pastCard.getUser());
        voiceChatRoom.setLeaved(false);
        return voiceChatRoom;
    }

    private void reload() {
        visibleEmptyView();
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
                    Filter filter = response.body();
                    if (isNoFilter(filter)) {
                        cardRequest = NetRetrofit.getInstance(getContext()).getService().getRandomVoiceCard();
                        cardRequest.enqueue(new Callback<ArrayList<VoiceCard>>() {
                            @Override
                            public void onResponse(Call<ArrayList<VoiceCard>> call, Response<ArrayList<VoiceCard>> response) {
                                if (response.isSuccessful()) {

                                    if (response.body().size() == 0) {
                                        visibleEmptyView();
                                        return;
                                    }

                                    if (adapter == null) {
                                        adapter = new UserCardAdapter(getContext());
                                        cardStackView.setAdapter(adapter);
                                    }

                                    cardStackView.setPaginationReserved();
                                    adapter.addAll(response.body());
                                    adapter.notifyDataSetChanged();

                                    Log.e(TAG, "voice card size: " + response.body().size());
                                    invisibleEmptyView();

                                    Log.e(TAG, "load card success");

                                    try {
                                        int currentPosition = cardStackView.getTopIndex();
                                        pastCard = adapter.getItem(currentPosition);
                                        Log.d("CardStackView", "load card topIndex: " + cardStackView.getTopIndex());
                                    } catch (IndexOutOfBoundsException e) {
                                        e.printStackTrace();
                                        pastCard = null;
                                        visibleEmptyView();
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
                        //TODO : 필터 카드 불러오기 시점
                        RealmResults<VoiceCard> results = realm.where(VoiceCard.class).findAll();
                        if (results.size() != 0) {
                            if (adapter == null) {
                                adapter = new UserCardAdapter(getContext());
                                cardStackView.setAdapter(adapter);
                            }

                            cardStackView.setPaginationReserved();
                            adapter.addAll(results);
                            adapter.notifyDataSetChanged();

                            invisibleEmptyView();

                            Log.e(TAG, "load realm card success");

                            try {
                                int currentPosition = cardStackView.getTopIndex();
                                pastCard = adapter.getItem(currentPosition);
                                Log.d("CardStackView", "load realm card topIndex: " + cardStackView.getTopIndex());
                            } catch (IndexOutOfBoundsException e) {
                                e.printStackTrace();
                                pastCard = null;
                                visibleEmptyView();
                            }
                        } else {
                            cardRequest = NetRetrofit.getInstance(getContext()).getService().getFilteredRandomVoiceCard();
                            cardRequest.enqueue(new Callback<ArrayList<VoiceCard>>() {
                                @Override
                                public void onResponse(Call<ArrayList<VoiceCard>> call, Response<ArrayList<VoiceCard>> response) {
                                    if (response.isSuccessful()) {
                                        if (isNoFilter(filter) && response.body().size() < 5) {
                                            //TODO 필터 부족시 랜덤 카드 전환 팝업 구현
                                            showNotEnoughFilterCardPopUp();
                                            Filter nullFilter = new Filter();
                                            nullFilter.setAgeMin(null);
                                            nullFilter.setAgeMax(null);
                                            nullFilter.setGender(null);
                                            nullFilter.setActiveUser(false);
                                            Call<Filter> filterCall = NetRetrofit.getInstance(getContext()).getService().updateFilter(nullFilter);
                                            filterCall.enqueue(new Callback<Filter>() {
                                                @Override
                                                public void onResponse(Call<Filter> call, Response<Filter> response) {
                                                    if (response.isSuccessful()) {
                                                        loadCard();
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
                                                    t.printStackTrace();
                                                }
                                            });
                                            return;
                                        }


                                        if (response.body().size() == 0) {
                                            visibleEmptyView();
                                            return;
                                        }

                                        if (adapter == null) {
                                            adapter = new UserCardAdapter(getContext());
                                            cardStackView.setAdapter(adapter);
                                        }

                                        cardStackView.setPaginationReserved();
                                        adapter.addAll(response.body());
                                        adapter.notifyDataSetChanged();

                                        //TODO 필터 카드 저장
                                        for (VoiceCard itCard : response.body()) {
                                            realm.executeTransaction(realm1 -> {
                                                realm1.insertOrUpdate(itCard);
                                            });
                                        }

                                        Log.e(TAG, "voice card size: " + response.body().size());
                                        invisibleEmptyView();

                                        Log.e(TAG, "load card success");

                                        try {
                                            int currentPosition = cardStackView.getTopIndex();
                                            pastCard = adapter.getItem(currentPosition);
                                            Log.d("CardStackView", "load card topIndex: " + cardStackView.getTopIndex());
                                        } catch (IndexOutOfBoundsException e) {
                                            e.printStackTrace();
                                            pastCard = null;
                                            visibleEmptyView();
                                        }
                                    } else {
                                        try {
                                            Result result = Utils.parseError(response);
                                            if ("NotEnoughCash".equals(result.getCode())) {
                                                //TODO 루나 부족할 경우 팝업
                                                showNotEnoughCashPopUp();

                                                Filter nullFilter = new Filter();
                                                nullFilter.setAgeMin(null);
                                                nullFilter.setAgeMax(null);
                                                nullFilter.setGender(null);
                                                nullFilter.setActiveUser(false);
                                                Call<Filter> filterCall = NetRetrofit.getInstance(getContext()).getService().updateFilter(nullFilter);

                                                filterCall.enqueue(new Callback<Filter>() {
                                                    @Override
                                                    public void onResponse(Call<Filter> call, Response<Filter> response) {
                                                        if (response.isSuccessful()) {
                                                            loadCard();
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
                                                        t.printStackTrace();
                                                    }
                                                });

                                            } else {
                                                Utils.toastError(getContext(), response);
                                            }
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
                        }
                    }
//                    cardRequest = NetRetrofit.getInstance(getContext()).getService().getRandomVoiceCard();
//                    cardRequest.enqueue(new Callback<ArrayList<VoiceCard>>() {
//                        @Override
//                        public void onResponse(Call<ArrayList<VoiceCard>> call, Response<ArrayList<VoiceCard>> response) {
//                            if (response.isSuccessful()) {
//                                if(isNoFilter(filter) && response.body().size() < 5){
//                                    //TODO 필터 부족시 랜덤 카드 전환 팝업 구현
//                                    Call<Filter> filterCall = NetRetrofit.getInstance(getContext()).getService().updateFilter(null);
//                                    filterCall.enqueue(new Callback<Filter>() {
//                                        @Override
//                                        public void onResponse(Call<Filter> call, Response<Filter> response) {
//                                            if(response.isSuccessful()){
//                                                loadCard();
//                                            }else {
//                                                try {
//                                                    Utils.toastError(getContext(), response);
//                                                } catch (IOException e) {
//                                                    e.printStackTrace();
//                                                }
//                                            }
//                                        }
//
//                                        @Override
//                                        public void onFailure(Call<Filter> call, Throwable t) {
//                                            t.printStackTrace();
//                                        }
//                                    });
//                                    return;
//                                }
//
//
//                                if (response.body().size() == 0) {
//                                    visibleEmptyView();
//                                    return;
//                                }
//
//                                if (adapter == null) {
//                                    adapter = new UserCardAdapter(getContext());
//                                    cardStackView.setAdapter(adapter);
//                                }
//
//                                cardStackView.setPaginationReserved();
//                                adapter.addAll(response.body());
//                                adapter.notifyDataSetChanged();
//
//                                Log.e(TAG, "voice card size: " + response.body().size());
//                                invisibleEmptyView();
//
//                                Log.e(TAG, "load card success");
//
//                                try {
//                                    int currentPosition = cardStackView.getTopIndex();
//                                    pastCard = adapter.getItem(currentPosition);
//                                    Log.d("CardStackView", "load card topIndex: " + cardStackView.getTopIndex());
//                                } catch (IndexOutOfBoundsException e) {
//                                    e.printStackTrace();
//                                    pastCard = null;
//                                    visibleEmptyView();
//                                }
//
//                            } else {
//                                try {
//                                    Result result = Utils.parseError(response);
//                                    if("캐시가 부족합니다.".equals(result.getMessage())){
//                                        //TODO 루나 부족할 경우 팝업
//                                        Call<Filter> filterCall = NetRetrofit.getInstance(getContext()).getService().updateFilter(null);
//                                        filterCall.enqueue(new Callback<Filter>() {
//                                            @Override
//                                            public void onResponse(Call<Filter> call, Response<Filter> response) {
//                                                if(response.isSuccessful()){
//                                                    loadCard();
//                                                }else {
//                                                    try {
//                                                        Utils.toastError(getContext(), response);
//                                                    } catch (IOException e) {
//                                                        e.printStackTrace();
//                                                    }
//                                                }
//                                            }
//
//                                            @Override
//                                            public void onFailure(Call<Filter> call, Throwable t) {
//                                                t.printStackTrace();
//                                            }
//                                        });
//                                    }
//
//                                    Utils.toastError(getContext(), response);
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        }
//
//                        @Override
//                        public void onFailure(Call<ArrayList<VoiceCard>> call, Throwable t) {
//                            Log.e(TAG, t.getMessage());
//                            t.printStackTrace();
//                        }
//                    });
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

    private void showNotEnoughCashPopUp() {
        User me = SharedPrefHelper.getInstance(getActivity()).getUserInfo();
        int needLuna = 5 - me.getLuna();
        if(needLuna < 0)
            return;

        //TODO : 루나 차감 미구현으로 주석
        MaterialDialog reportDialog = new MaterialDialog.Builder(getActivity())
                .customView(R.layout.dialog_code, false)
                .show();

        DialogUtils.initDialogView(reportDialog, getActivity());

        TextView tvTitle = (TextView) reportDialog.findViewById(R.id.tv_title);
        TextView tvContent = (TextView) reportDialog.findViewById(R.id.tv_content);
        TextView tvConfirm = (TextView) reportDialog.findViewById(R.id.tv_send_code);
        TextView tvCancel = (TextView) reportDialog.findViewById(R.id.tv_cancel);
        EditText etInviteCode = (EditText) reportDialog.findViewById(R.id.et_code);

        etInviteCode.setVisibility(View.GONE);

        tvTitle.setText("루나가 부족합니다.");

        tvContent.setText(Html.fromHtml(getString(R.string.dialog_not_enough_filter_luna, needLuna)));
        tvConfirm.setText("충전하러 가기");
        tvConfirm.setEnabled(true);
        tvConfirm.setBackgroundResource(R.drawable.button_text_background);
        tvConfirm.setTextColor(getResources().getColorStateList(R.color.button_text_color));
        tvConfirm.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ShopActivity.class);
            startActivity(intent);
            reportDialog.dismiss();
        });

        tvCancel.setText("취소");
        tvCancel.setOnClickListener(v -> {
            reportDialog.dismiss();
        });
    }

    private void showNotEnoughFilterCardPopUp() {
        //TODO : 루나 차감 미구현으로 주석
        MaterialDialog reportDialog = new MaterialDialog.Builder(getActivity())
                .customView(R.layout.dialog_code, false)
                .show();

        DialogUtils.initDialogView(reportDialog, getActivity());

        TextView tvTitle = (TextView) reportDialog.findViewById(R.id.tv_title);
        TextView tvContent = (TextView) reportDialog.findViewById(R.id.tv_content);
        TextView tvConfirm = (TextView) reportDialog.findViewById(R.id.tv_send_code);
        TextView tvCancel = (TextView) reportDialog.findViewById(R.id.tv_cancel);
        EditText etInviteCode = (EditText) reportDialog.findViewById(R.id.et_code);

        etInviteCode.setVisibility(View.GONE);

        tvTitle.setText("필터가 적용된 카드가 부족해요...");
        tvContent.setText("필터가 자동적으로 해제됩니다. 먼저 내 이야기를 올려보세요");
        tvConfirm.setText("이야기 올리러 가기");
        tvConfirm.setEnabled(true);
        tvConfirm.setBackgroundResource(R.drawable.button_text_background);
        tvConfirm.setTextColor(getResources().getColorStateList(R.color.button_text_color));
        tvConfirm.setOnClickListener(v -> {
            moveToRecordActivity(-1);
            reportDialog.dismiss();
        });

        tvCancel.setText("확인");
        tvCancel.setOnClickListener(v -> {
            reportDialog.dismiss();
        });
    }

    private boolean isNoFilter(Filter filter) {
        return filter == null || (filter.getAgeMax() == null && filter.getAgeMin() == null && filter.getGender() == null && !filter.getActiveUser());
    }

    @Override
    public void onPause() {
        super.onPause();
        if (adapter != null)
            adapter.cardPlayStop();
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
        Intent intent = new Intent(getActivity(), ShopActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.filter_setting)
    public void onClickFilterSetting() {
        Intent intent = new Intent(getActivity(), FilterActivity.class);
//        startActivity(intent);
        startActivityForResult(intent, filterRequestCode);
    }

    @OnClick(R.id.filter_event)
    public void onClickFilterEvent() {
        Intent intent = new Intent(getActivity(), EventActivity.class);
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
     */
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


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (adapter == null)
            return;

        if (!isVisibleToUser)
            adapter.cardPlayStop();
    }

    public class UserCardAdapter extends ArrayAdapter<VoiceCard> {
        int duration;
        int CURRENT_STATE = 2;
        final int STATE_PLAY = 1;
        final int STATE_STOP = 2;
        byte[] sample = Utils.getSampleWave();
        private ImageView lastPlayButton;

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
                convertView = inflater.inflate(R.layout.item_voice_card, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (CURRENT_STATE == STATE_PLAY) {
                Log.d(TAG, "current state1 : " + CURRENT_STATE);
                holder.playButton.performClick();
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
                if (CURRENT_STATE == STATE_PLAY)
                    holder.playButton.performClick();

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

            try {
                duration = VoicePlayerManager.getInstance().getPlayTime(voiceCard.getVoiceUrl(), getActivity());
                Log.e(TAG, "duration: " + duration);
            } catch (IOException e) {
                e.printStackTrace();
            }

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

            if (duration != -1)
                progressAnim.setDuration(duration);

            holder.playButton.setOnClickListener((View v) -> {
                lastPlayButton = (ImageView) v;
                Log.d(TAG, "current state2 : " + CURRENT_STATE);
                switch (CURRENT_STATE) {
                    case STATE_STOP:
                        CURRENT_STATE = STATE_PLAY;
                        //PLAY

                        getActivity().runOnUiThread(() -> {
                            if (holder.waveView.getVisibility() == View.INVISIBLE) {
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

                        getActivity().runOnUiThread(() -> {
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

        public void cardPlayStop() {
            if (lastPlayButton == null) {
                return;
            }

            if (CURRENT_STATE == STATE_PLAY) {
                lastPlayButton.performClick();
            }
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
