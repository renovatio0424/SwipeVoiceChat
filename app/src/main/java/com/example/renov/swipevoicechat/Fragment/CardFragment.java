package com.example.renov.swipevoicechat.Fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.renov.swipevoicechat.Activity.MainActivity;
import com.example.renov.swipevoicechat.Activity.RetrofitActivity;
import com.example.renov.swipevoicechat.Card.UserCard;
import com.example.renov.swipevoicechat.Card.VoiceRecordCard;
import com.example.renov.swipevoicechat.Card.VoiceRecordCard2;
import com.example.renov.swipevoicechat.Profile;
import com.example.renov.swipevoicechat.R;
import com.example.renov.swipevoicechat.Utils;
import com.mindorks.placeholderview.SwipeDecor;
import com.mindorks.placeholderview.SwipePlaceHolderView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class CardFragment extends Fragment {

    @BindView(R.id.swipeView)
    public SwipePlaceHolderView mSwipeView;
    private Context mContext;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    public Unbinder unbinder;

    public static CardFragment newInstance(){
        CardFragment cardFragment = new CardFragment();
        return cardFragment;
    }

    private boolean permissionToRecordAccepted = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mContext = getContext();

        ActivityCompat.requestPermissions(getActivity(), permissions, REQUEST_RECORD_AUDIO_PERMISSION);


        mSwipeView.getBuilder()
                .setDisplayViewCount(3)
                .setSwipeDecor(new SwipeDecor()
                        .setPaddingTop(20)
                        .setRelativeScale(0.01f)
                        .setSwipeInMsgLayoutId(R.layout.swipe_right_msg_view)
                        .setSwipeOutMsgLayoutId(R.layout.swipe_left_msg_view));

        for(Profile profile : Utils.loadProfiles(this.getContext())){
                mSwipeView.addView(new UserCard(mContext, profile, mSwipeView));
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_card,container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}
