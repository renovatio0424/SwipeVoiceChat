package com.example.renov.swipevoicechat.Fragment;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.renov.swipevoicechat.Profile;
import com.example.renov.swipevoicechat.R;
import com.example.renov.swipevoicechat.Utils;
import com.example.renov.swipevoicechat.widget.VoicePlayerManager;
import com.example.renov.swipevoicechat.widget.VoicePlayerView;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.SwipeDirection;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.wasabeef.glide.transformations.BlurTransformation;

public class InfoFragment extends Fragment {

    @BindView(R.id.activity_main_progress_bar)
    public ProgressBar progressBar;
    @BindView(R.id.activity_main_card_stack_view)
    public CardStackView cardStackView;
    @BindView(R.id.tv_new_story)
    public TextView btnNewStory;
//    private TouristSpotCardAdapter adapter;
    private UserCardAdapter adapter;

    public Unbinder unbinder;

    public static InfoFragment newInstance(){
        InfoFragment infoFragment = new InfoFragment();
        return infoFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        Main Logic here
        setup();
        reload();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    private UserCardAdapter createUserCardAdapter(){
        final UserCardAdapter adapter = new UserCardAdapter(getContext());
        for(Profile profile : Utils.loadProfiles(this.getContext())){
            adapter.add(profile);
        }
        return adapter;
    }

    private void setup() {
        cardStackView.setCardEventListener(new CardStackView.CardEventListener() {
            @Override
            public void onCardDragging(float percentX, float percentY) {
                Log.d("CardStackView", "onCardDragging");
            }

            @Override
            public void onCardSwiped(SwipeDirection direction) {
                Log.d("CardStackView", "onCardSwiped: " + direction.toString());
                Log.d("CardStackView", "topIndex: " + cardStackView.getTopIndex());
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

    private void reload() {
        cardStackView.setVisibility(View.GONE);
        btnNewStory.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        new Handler().postDelayed(() -> {
//                adapter = createTouristSpotCardAdapter();
            adapter = createUserCardAdapter();
            cardStackView.setAdapter(adapter);
            cardStackView.setVisibility(View.VISIBLE);
//            TODO: 새로운 이야기 시작 버튼 보이는 시점
            btnNewStory.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }, 1000);
    }

    private LinkedList<Profile> extractRemainingProfiles() {
        LinkedList<Profile> profiles = new LinkedList<>();
        for (int i = cardStackView.getTopIndex(); i < adapter.getCount(); i++) {
            profiles.add(adapter.getItem(i));
        }
        return profiles;
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
    public void onClickNewStory(){
        boolean wrapInScrollView = false;
        MaterialDialog materialDialog = new MaterialDialog.Builder(getContext())
                .title("새이야기 시작하기")
                .customView(R.layout.dialog_record, wrapInScrollView)
                .positiveText("보내기")
                .negativeText("취소")
                .show();

        View view = materialDialog.getView();
        TextView tvRecordAgain = (TextView) view.findViewById(R.id.tv_record_again);
        tvRecordAgain.setVisibility(View.GONE);
        tvRecordAgain.setText("다시하기");
        TextView tvRecordDesc = (TextView) view.findViewById(R.id.tv_record_desc);
        tvRecordDesc.setVisibility(View.VISIBLE);
        tvRecordDesc.setText("3초 이상 녹음해주세요");
        TextView tvExampleDesc = (TextView) view.findViewById(R.id.tv_example_desc);
        tvExampleDesc.setText("오늘 집에서 쉬는데, 저처럼 평일인데 쉬시는 분 있나요?");
    }

    private void paginate() {
        cardStackView.setPaginationReserved();
        for(Profile profile : Utils.loadProfiles(this.getContext())){
            adapter.add(profile);
        }
//        adapter.addAll();
//        adapter.addAll(createTouristSpots());
        adapter.notifyDataSetChanged();
    }

    public void swipeTop() {
        List<Profile> spots = extractRemainingProfiles();
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
        List<Profile> spots = extractRemainingProfiles();
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
        List<Profile> spots = extractRemainingProfiles();
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

    public class UserCardAdapter extends ArrayAdapter<Profile> {

        VoicePlayerManager voicePlayerManager = VoicePlayerManager.getInstance();

        public UserCardAdapter(@NonNull Context context) {
            super(context, 0);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            ViewHolder holder;

            if(convertView == null){
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.card_user_view, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Profile userProfile = getItem(position);
            holder.nameAgeTxt.setText(userProfile.getName());
            holder.locationNameTxt.setText(userProfile.getLocation());
            Glide.with(getContext())
                    .load(userProfile.getImageUrl())
                    .apply(RequestOptions.bitmapTransform(new BlurTransformation(25,3)))
                    .into(holder.profileImage);

            holder.rejectBtn.setOnClickListener(v -> swipeLeft());
            holder.acceptBtn.setOnClickListener(v -> swipeRight());
            holder.superLikeBtn.setOnClickListener(v -> swipeTop());

            holder.ivReport.setOnClickListener(v -> {
                new MaterialDialog.Builder(getContext())
                        .title("신고하기")
                        .items(R.array.report)
                        .itemsCallbackSingleChoice(0, (dialog, itemView, which, text) -> {
                            Toast.makeText(getContext(), "select position: " + which, Toast.LENGTH_SHORT).show();
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
            @BindView(R.id.superLikeBtn)
            public Button superLikeBtn;
            @BindView(R.id.acceptBtn)
            public Button acceptBtn;
            @BindView(R.id.iv_report)
            public ImageView ivReport;

            private ViewHolder(View view){
                ButterKnife.bind(this, view);
            }
        }
    }
}
