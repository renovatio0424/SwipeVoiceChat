package com.example.renov.swipevoicechat.Fragment;

import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.renov.swipevoicechat.Activity.ChatActivity;
import com.example.renov.swipevoicechat.Adapter.SwipeController;
import com.example.renov.swipevoicechat.Adapter.SwipeControllerActions;
import com.example.renov.swipevoicechat.Event.RefreshEvent;
import com.example.renov.swipevoicechat.Model.Profile;
import com.example.renov.swipevoicechat.Model.VoiceChatRoom;
import com.example.renov.swipevoicechat.Network.NetRetrofit;
import com.example.renov.swipevoicechat.Network.ApiService;
import com.example.renov.swipevoicechat.R;
import com.example.renov.swipevoicechat.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import jp.wasabeef.glide.transformations.BlurTransformation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatRoomFragment extends Fragment {
    public Unbinder unbinder;

//    @BindView(R.id.materialLeanBack)
//    MaterialLeanBack materialLeanBack;

    @BindView(R.id.recyclerview)
    RecyclerView recyclerView;

    ApiService service = NetRetrofit.getInstance(getContext()).getService();
    public static ChatRoomFragment newInstance() {
        ChatRoomFragment chatRoomFragment = new ChatRoomFragment();
        return chatRoomFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        Log.e("event bus", "onstart()");
    }

    CardAdapter cardAdapter;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

//        cardAdapter = new CardAdapter(Utils.loadProfiles(getContext()));
        cardAdapter = new CardAdapter();

        loadChatRooms();
        cardAdapter.addItem(Utils.loadProfiles(getContext()).get(0));
        cardAdapter.addItem(Utils.loadProfiles(getContext()).get(1));
        cardAdapter.addItem(Utils.loadProfiles(getContext()).get(2));
        cardAdapter.addItem(Utils.loadProfiles(getContext()).get(3));

        SwipeController swipeController = new SwipeController(new SwipeControllerActions() {
            @Override
            public void onRightClicked(int position) {
                cardAdapter.profiles.remove(position);
                cardAdapter.notifyItemRemoved(position);
                cardAdapter.notifyItemRangeChanged(position, cardAdapter.getItemCount());
            }
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeController);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                swipeController.onDraw(c);
            }
        });

//        Bundle args = getArguments();
//        if(args != null){
//            int sendPosition = args.getInt("position",-1);
//
//            if(sendPosition != -1){
//                Profile add = Utils.loadProfiles(getContext()).get(sendPosition);
//                cardAdapter.addItem(add);
//            }
//        }

        recyclerView.setAdapter(cardAdapter);
        cardAdapter.notifyDataSetChanged();
//        Main Logic here
//        materialLeanBack.setCustomizer(textView -> textView.setTypeface(null, Typeface.BOLD));
//        materialLeanBack.setAdapter(new MaterialLeanBack.Adapter<MaterialLeanBack.ViewHolder>() {
//            @Override
//            public int getLineCount() {
//                return 2;
//            }
//
//            @Override
//            public int getCellsCount(int row) {
//                return Utils.loadProfiles(getContext()).size();
//            }
//
//            @Override
//            public MaterialLeanBack.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int row) {
//                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_user_view2, viewGroup, false);
//                return new MyViewHolder(view);
//            }
//
//            @Override
//            public void onBindViewHolder(MaterialLeanBack.ViewHolder viewHolder, int i) {
//                MyViewHolder holder = (MyViewHolder) viewHolder;
//                Profile profile = Utils.loadProfiles(getContext()).get(i);
//                holder.name.setText(profile.getName());
//                holder.chatDesc.setText(profile.getLocation());
//                Glide.with(getContext())
//                        .load(profile.getImageUrl())
//                        .apply(RequestOptions.centerCropTransform())
//                        .apply(RequestOptions.bitmapTransform(new BlurTransformation(25,3)))
//                        .into(holder.profileImage);
//
//                holder.acceptBtn.setVisibility(View.GONE);
//                holder.rejectBtn.setVisibility(View.GONE);
//                holder.superLikeBtn.setVisibility(View.GONE);
//                holder.voicePlayerView.setVisibility(View.GONE);
//                holder.reportBtn.setVisibility(View.GONE);
//
//                super.onBindViewHolder(viewHolder, i);
//            }
//
//            @Override
//            public String getTitleForRow(int row) {
//                String resultTitle = null;
//                if(row == 0)
//                    resultTitle = "매칭된 목소리";
//                else
//                    resultTitle = "호감한 목소리";
//                return resultTitle;
//            }
//
//            @Override
//            public boolean hasRowTitle(int row) {
//                return row == 0 || row == 1;
//            }
////            @Override
////            public boolean isCustomView(int row) {
////                return super.isCustomView(row);
////            }
//
////            @Override
////            public RecyclerView.ViewHolder getCustomViewForRow(ViewGroup viewGroup, int row) {
////                return super.getCustomViewForRow(viewGroup, row);
////            }
//
////            @Override
////            public void onBindCustomView(RecyclerView.ViewHolder viewHolder, int row) {
////                super.onBindCustomView(viewHolder, row);
////            }
//        });
    }

    private void loadChatRooms() {
        Call<ArrayList<VoiceChatRoom>> request = service.loadVoiceChatRoomList();
        request.enqueue(new Callback<ArrayList<VoiceChatRoom>>() {
            @Override
            public void onResponse(Call<ArrayList<VoiceChatRoom>> call, Response<ArrayList<VoiceChatRoom>> response) {
                if(response.isSuccessful()){
                    cardAdapter.addList(response.body());
                } else {

                }
            }

            @Override
            public void onFailure(Call<ArrayList<VoiceChatRoom>> call, Throwable t) {

            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_room, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Subscribe
    public void onRefreshEvent(RefreshEvent refreshEvent) {
        Log.e("event bus", "onRefreshEvent(): " + ChatRoomFragment.class.getSimpleName());
        if (refreshEvent.action == RefreshEvent.Action.SEND_NEW_STORY) {
            int sendPosition = refreshEvent.position;
            if (sendPosition != -1) {
                Profile add = Utils.loadProfiles(getContext()).get(sendPosition);
                cardAdapter.addItem(add);
                cardAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        Log.e("event bus", "onstop()");
        unbinder.unbind();
    }



    public class CardAdapter extends RecyclerView.Adapter<viewHolder> {

//        private List<VoiceChatRoom> rooms;
        private List<Profile> profiles;

        public CardAdapter() {
            profiles = new ArrayList<>();
//            rooms = new ArrayList<>();
        }

        public CardAdapter(List mProfiles) {
            profiles = mProfiles;
        }

//        public CardAdapter(List mRooms) {
//            this.rooms = mRooms;
//        }

        MultiTransformation multiTransformation = new MultiTransformation(new BlurTransformation(25, 3),
                new CircleCrop());

        @NonNull
        @Override
        public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_room, parent, false);
//            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) view.getLayoutParams();
//            layoutParams.width = layoutParams.width - 5;
//            view.requestLayout();
            view.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), ChatActivity.class);
                startActivity(intent);
            });
            viewHolder viewHolder = new viewHolder(view);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull viewHolder holder, int position) {
            Profile currentProfile = profiles.get(position);

            holder.lastTime.setText("오전 12:39");
            holder.chatDesc.setText(currentProfile.getDistance() + "km");
            holder.name.setText(currentProfile.getName() + ", " + currentProfile.getAge());
            Glide.with(getContext())
                    .load(currentProfile.getImageUrl())
                    .apply(RequestOptions.bitmapTransform(multiTransformation))
                    .into(holder.profileImage);

        }

        @Override
        public int getItemCount() {
            return profiles.size();
        }

        public void addList(ArrayList<VoiceChatRoom> rooms){
//            this.rooms = rooms;
        }

        public void addItem(Profile addItem) {
            profiles.add(addItem);
        }
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_profile)
        ImageView profileImage;
        @BindView(R.id.tv_name)
        TextView name;
        @BindView(R.id.tv_chat_desc)
        TextView chatDesc;
        @BindView(R.id.tv_last_time)
        TextView lastTime;

        public viewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
//    public class MyViewHolder extends MaterialLeanBack.ViewHolder{
//        @BindView(R.id.profileImageView)
//        ImageView profileImage;
//        @BindView(R.id.voice_player_view)
//        VoicePlayerView voicePlayerView;
//        @BindView(R.id.name)
//        TextView name;
//        @BindView(R.id.chatDesc)
//        TextView chatDesc;
//        @BindView(R.id.rejectBtn)
//        Button rejectBtn;
//        @BindView(R.id.superLikeBtn)
//        Button superLikeBtn;
//        @BindView(R.id.acceptBtn)
//        Button acceptBtn;
//        @BindView(R.id.iv_report)
//        ImageView reportBtn;
//
//        public MyViewHolder(View view){
//            super(view);
//            ButterKnife.bind(this, view);
//        }
//    }
}
