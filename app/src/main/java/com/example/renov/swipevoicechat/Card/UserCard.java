package com.example.renov.swipevoicechat.Card;

import android.content.Context;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.renov.swipevoicechat.Profile;
import com.example.renov.swipevoicechat.R;
import com.example.renov.swipevoicechat.widget.VoicePlayerManager;
import com.example.renov.swipevoicechat.widget.VoicePlayerView;
import com.mindorks.placeholderview.SwipeDirection;
import com.mindorks.placeholderview.SwipePlaceHolderView;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;
import com.mindorks.placeholderview.annotations.swipe.SwipeCancelState;
import com.mindorks.placeholderview.annotations.swipe.SwipeIn;
import com.mindorks.placeholderview.annotations.swipe.SwipeInState;
import com.mindorks.placeholderview.annotations.swipe.SwipeOut;
import com.mindorks.placeholderview.annotations.swipe.SwipeOutDirectional;
import com.mindorks.placeholderview.annotations.swipe.SwipeOutState;

import jp.wasabeef.glide.transformations.BlurTransformation;

@Layout(R.layout.card_user_view)
public class UserCard {
    @View(R.id.profileImageView)
    private ImageView profileImageView;

    @View(R.id.nameAgeTxt)
    private TextView nameAgeTxt;

    @View(R.id.locationNameTxt)
    private TextView locationNameTxt;

    @View(R.id.voice_player_view)
    VoicePlayerView voicePlayerView;

    @View(R.id.acceptBtn)
    Button acceptBtn;

    @View(R.id.rejectBtn)
    Button rejectBtn;

    @View(R.id.superLikeBtn)
    Button superLikeBtn;

    @View(R.id.swipe_top_msg_view)
    TextView swipeTopMsgView;

    private VoicePlayerManager voicePlayerManager;

    private Profile mProfile;
    private Context mContext;
    private SwipePlaceHolderView mSwipeView;

    public UserCard(Context context, Profile profile, SwipePlaceHolderView swipeView) {
        mContext = context;
        mProfile = profile;
        mSwipeView = swipeView;
        mSwipeView.enableTouchSwipe();
    }

    @Resolve
    private void onResolved(){
        Log.d("onResolved", "profile image url: " + mProfile.getImageUrl());

        voicePlayerManager = VoicePlayerManager.getInstance();
        voicePlayerView.prepareVoicePlay();
        voicePlayerView.setVoiceRecordListener(new VoicePlayerView.VoiceRecordListener() {
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
                voicePlayerView.startVoicePlayProgress(duration);
            }

            @Override
            public void onStopPlay() {
                voicePlayerManager.voicePlayStop();
            }
        });

        Glide.with(mContext)
                .load(mProfile.getImageUrl())
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(25,3)))
                .into(profileImageView);
        nameAgeTxt.setText(mProfile.getName() + ", " + mProfile.getAge());
        locationNameTxt.setText(mProfile.getLocation());

        acceptBtn.setOnClickListener(v -> mSwipeView.doSwipe(true));
        rejectBtn.setOnClickListener(v -> mSwipeView.doSwipe(false));

    }

    @SwipeOut
    private void onSwipedOut(){
        Log.d("EVENT", "onSwipedOut");
        mSwipeView.addView(this);
    }

    @SwipeCancelState
    private void onSwipeCancelState(){
        Log.d("EVENT", "onSwipeCancelState");
    }

    @SwipeIn
    private void onSwipeIn(){
        Log.d("EVENT", "onSwipedIn");
    }

    @SwipeInState
    private void onSwipeInState(){
        Log.d("EVENT", "onSwipeInState");
    }

    @SwipeOutState
    private void onSwipeOutState(){
        Log.d("EVENT", "onSwipeOutState");
    }

    @SwipeOutDirectional
    public void onSwipeOutDirectional(SwipeDirection direction){
        Log.d("DEBUG", "SwipeOutDirectional " + direction.name());

        if(direction.getDirection() == SwipeDirection.TOP.getDirection()){
            swipeTopMsgView.setVisibility(android.view.View.VISIBLE);
        } else {
            swipeTopMsgView.setVisibility(android.view.View.GONE);
        }
    }
}
