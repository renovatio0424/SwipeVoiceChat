package com.example.renov.swipevoicechat.Fragment;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.renov.swipevoicechat.Card.UserCard;
import com.example.renov.swipevoicechat.Profile;
import com.example.renov.swipevoicechat.R;
import com.example.renov.swipevoicechat.Utils;
import com.example.renov.swipevoicechat.widget.VoicePlayerView;
import com.github.florent37.materialleanback.MaterialLeanBack;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import jp.wasabeef.glide.transformations.BlurTransformation;

public class ChatRoomFragment extends Fragment {
    public Unbinder unbinder;

    @BindView(R.id.materialLeanBack)
    MaterialLeanBack materialLeanBack;

    public static ChatRoomFragment newInstance(){
        ChatRoomFragment chatRoomFragment = new ChatRoomFragment();
        return chatRoomFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        Main Logic here
        materialLeanBack.setCustomizer(textView -> textView.setTypeface(null, Typeface.BOLD));

        materialLeanBack.setAdapter(new MaterialLeanBack.Adapter<MaterialLeanBack.ViewHolder>() {
            @Override
            public int getLineCount() {
                return 2;
            }

            @Override
            public int getCellsCount(int row) {
                return Utils.loadProfiles(getContext()).size();
            }

            @Override
            public MaterialLeanBack.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int row) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_user_view, viewGroup, false);
                return new MyViewHolder(view);
            }

            @Override
            public void onBindViewHolder(MaterialLeanBack.ViewHolder viewHolder, int i) {
                MyViewHolder holder = (MyViewHolder) viewHolder;
                Profile profile = Utils.loadProfiles(getContext()).get(i);
                holder.nameAgeTxt.setText(profile.getName());
                holder.locationNameTxt.setText(profile.getLocation());
                Glide.with(getContext())
                        .load(profile.getImageUrl())
                        .apply(RequestOptions.bitmapTransform(new BlurTransformation(25,3)))
                        .into(holder.profileImage);

                holder.acceptBtn.setVisibility(View.GONE);
                holder.rejectBtn.setVisibility(View.GONE);
                holder.superLikeBtn.setVisibility(View.GONE);
                holder.voicePlayerView.setVisibility(View.GONE);

                super.onBindViewHolder(viewHolder, i);
            }

            @Override
            public String getTitleForRow(int row) {
                String resultTitle = null;
                if(row == 0)
                    resultTitle = "매칭된 목소리";
                else
                    resultTitle = "호감한 목소리";
                return resultTitle;
            }

            @Override
            public boolean hasRowTitle(int row) {
                return row == 0 || row == 1;
            }

//            @Override
//            public boolean isCustomView(int row) {
//                return super.isCustomView(row);
//            }

//            @Override
//            public RecyclerView.ViewHolder getCustomViewForRow(ViewGroup viewGroup, int row) {
//                return super.getCustomViewForRow(viewGroup, row);
//            }

//            @Override
//            public void onBindCustomView(RecyclerView.ViewHolder viewHolder, int row) {
//                super.onBindCustomView(viewHolder, row);
//            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_room, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    public class MyViewHolder extends MaterialLeanBack.ViewHolder{
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

        public MyViewHolder(View view){
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
