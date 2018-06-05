package com.example.renov.swipevoicechat.Card;

import android.content.Context;
import android.transition.CircularPropagation;
import android.util.Log;
import android.widget.Button;

import com.example.renov.swipevoicechat.R;
import com.example.renov.swipevoicechat.widget.VoicePlayerView;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.mindorks.placeholderview.SwipePlaceHolderView;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;
import com.mindorks.placeholderview.annotations.swipe.SwipeCancelState;
import com.mindorks.placeholderview.annotations.swipe.SwipeIn;
import com.mindorks.placeholderview.annotations.swipe.SwipeInState;
import com.mindorks.placeholderview.annotations.swipe.SwipeOut;
import com.mindorks.placeholderview.annotations.swipe.SwipeOutState;

import butterknife.BindColor;

@Layout(R.layout.card_voice_record_view2)
public class VoiceRecordCard2 {
    @View(R.id.circularProgressBar)
    CircularProgressBar circularProgressBar;

    @View(R.id.record_button)
    Button recordButton;

    @BindColor(R.color.colorPrimary)
    int colorPrimary;
    @BindColor(R.color.colorAccent)
    int colorAccent;

    private Context mContext;
    private SwipePlaceHolderView mSwipeView;

    private boolean isRecord = false;
    public VoiceRecordCard2(Context context, SwipePlaceHolderView swipeView) {
        mContext = context;
        mSwipeView = swipeView;
        mSwipeView.disableTouchSwipe();
    }

    @Resolve
    private void onResolved(){
        isRecord = false;

        recordButton.setOnClickListener(v -> circularProgressBar.setProgressWithAnimation(100,30000));
        circularProgressBar.setColor(mContext.getResources().getColor(colorPrimary));
        circularProgressBar.setBackgroundColor(mContext.getResources().getColor(colorAccent));
        circularProgressBar.setProgressBarWidth(30);
        circularProgressBar.setBackgroundProgressBarWidth(10);

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
