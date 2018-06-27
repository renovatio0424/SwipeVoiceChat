package com.square.renov.swipevoicechat.Card;

import android.content.Context;
import android.speech.tts.Voice;
import android.util.Log;
import android.widget.Button;

import com.square.renov.swipevoicechat.widget.VoicePlayerManager;
import com.square.renov.swipevoicechat.widget.VoicePlayerView;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;

import com.square.renov.swipevoicechat.R;
import com.mindorks.placeholderview.SwipePlaceHolderView;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.swipe.SwipeCancelState;
import com.mindorks.placeholderview.annotations.swipe.SwipeIn;
import com.mindorks.placeholderview.annotations.swipe.SwipeInState;
import com.mindorks.placeholderview.annotations.swipe.SwipeOut;
import com.mindorks.placeholderview.annotations.swipe.SwipeOutState;

@Layout(R.layout.card_voice_record_view)
public class VoiceRecordCard {
    @View(R.id.record_button)
    Button recordButton;

    @View(R.id.voiceplayerview)
    VoicePlayerView voicePlayerView;

    private VoicePlayerManager voicePlayerManager;
    private Context mContext;
    private SwipePlaceHolderView mSwipeView;
    private boolean isRecord = false;


    public VoiceRecordCard(Context context, SwipePlaceHolderView swipeView) {
        mContext = context;
        mSwipeView = swipeView;
        mSwipeView.disableTouchSwipe();
        voicePlayerManager = VoicePlayerManager.getInstance();
    }

    @Resolve
    private void onResolved(){
        isRecord = false;

        voicePlayerView.setVoiceRecordListener(new VoicePlayerView.VoiceRecordListener() {
            @Override
            public void onRecord() {
                if (mContext != null) {
                    voicePlayerView.startRecordProgress(30000);
                    voicePlayerManager.voiceRecord(mContext);
                }
            }

            @Override
            public void onStopRecord() {
                voicePlayerManager.voiceRecordStop();
                mSwipeView.enableTouchSwipe();
            }

            @Override
            public void onPlay() {
                int duration = voicePlayerManager.voicePlay(mContext);
                voicePlayerView.startVoicePlayProgress(duration);
                mSwipeView.disableTouchSwipe();
            }

            @Override
            public void onStopPlay() {
                voicePlayerManager.voicePlayStop();
                mSwipeView.enableTouchSwipe();
            }
        });

        recordButton.setOnClickListener(v -> {
            if(isRecord){
                mSwipeView.enableTouchSwipe();
                isRecord = false;
            } else{
                mSwipeView.disableTouchSwipe();
                isRecord = true;
            }
        });
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
}
