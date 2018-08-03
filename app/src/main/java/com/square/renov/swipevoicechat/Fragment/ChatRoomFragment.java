package com.square.renov.swipevoicechat.Fragment;

import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.request.RequestOptions;
import com.square.renov.swipevoicechat.Activity.ChatActivity;
import com.square.renov.swipevoicechat.Adapter.SwipeController;
import com.square.renov.swipevoicechat.Adapter.SwipeControllerActions;
import com.square.renov.swipevoicechat.Event.RefreshEvent;
import com.square.renov.swipevoicechat.Model.User;
import com.square.renov.swipevoicechat.Model.VoiceChatRoom;
import com.square.renov.swipevoicechat.Network.NetRetrofit;
import com.square.renov.swipevoicechat.Network.ApiService;
import com.square.renov.swipevoicechat.R;
import com.square.renov.swipevoicechat.Util.AgeUtil;
import com.square.renov.swipevoicechat.Util.DistanceUtil;
import com.square.renov.swipevoicechat.Util.RealmHelper;
import com.square.renov.swipevoicechat.Util.SharedPrefHelper;
import com.square.renov.swipevoicechat.Util.Utils;
import com.square.renov.swipevoicechat.widget.EmptyRecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;
import jp.wasabeef.glide.transformations.BlurTransformation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatRoomFragment extends Fragment {
    private static final String TAG = ChatRoomFragment.class.getSimpleName();
    public Unbinder unbinder;

    @BindView(R.id.recyclerview)
    EmptyRecyclerView recyclerView;
    @BindView(R.id.empty_view)
    ConstraintLayout emptyView;
    private boolean isActive;

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

    ChatRoomAdapter cardAdapter;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        //TODO: 샘플 데이터 삽입

//        cardAdapter = new ChatRoomAdapter(Utils.loadRooms(getContext())){
//            @NonNull
//            @Override
//            public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//                viewHolder viewHolder = super.onCreateViewHolder(parent, viewType);
//                viewHolder.itemView.setOnClickListener(v -> {
//                    int position = viewHolder.getAdapterPosition();
//                    int chatRoomId = cardAdapter.getChatRoomId(position);
//                    Intent intent = new Intent(getContext(), ChatActivity.class);
//                    intent.putExtra("chatRoomId", chatRoomId);
//                    startActivity(intent);
//                });
//
//                return viewHolder;
//            }
//        };

        SwipeController swipeController = new SwipeController(new SwipeControllerActions() {
            @Override
            public void onRightClicked(int position) {
                int chatRoomId = cardAdapter.getChatRoomId(position);

                Call<VoiceChatRoom> request = NetRetrofit.getInstance(getContext()).getService().leaveVoiceChatRoom(chatRoomId);
                request.enqueue(new Callback<VoiceChatRoom>() {
                    @Override
                    public void onResponse(Call<VoiceChatRoom> call, Response<VoiceChatRoom> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "방나가기", Toast.LENGTH_SHORT).show();
                            realm.executeTransactionAsync(realm1 -> {
                                RealmResults<VoiceChatRoom> result = realm1.where(VoiceChatRoom.class).equalTo("id", chatRoomId).findAll();
                                result.deleteAllFromRealm();
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
                    public void onFailure(Call<VoiceChatRoom> call, Throwable t) {
                        t.printStackTrace();
                    }
                });

                cardAdapter.rooms.remove(position);
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

    @Override
    public void onStart() {
        super.onStart();
        isActive = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadChatRooms();
    }

    @Override
    public void onStop() {
        super.onStop();
        isActive = false;
    }

    Realm realm = RealmHelper.getRealm(RealmHelper.CHAT_ROOM);

    private void loadChatRooms() {
        RealmResults<VoiceChatRoom> results = realm.where(VoiceChatRoom.class).findAllSorted("createdAt", Sort.DESCENDING);

        if (results.size() > 0 & !Utils.needToDataUpdate(getContext(), SharedPrefHelper.CHAT_ROOM_DATA_UPDATE_TIME)) {
            Log.d(TAG, "load realm");
            RealmQuery<VoiceChatRoom> newRoomQuery = realm.where(VoiceChatRoom.class);
            newRoomQuery.equalTo("isNewRoom", true)
                    .findAll();
            RealmResults<VoiceChatRoom> newRoomResults = newRoomQuery.findAll();
            newRoomResults = newRoomResults.sort("createdAt", Sort.DESCENDING);

            RealmQuery<VoiceChatRoom> oldRoomQuery = realm.where(VoiceChatRoom.class);
            oldRoomQuery.equalTo("isNewRoom", false)
                    .findAll();
            RealmResults<VoiceChatRoom> oldRoomResults = oldRoomQuery.findAll();
            oldRoomResults.sort("createdAt", Sort.DESCENDING);

            ArrayList<VoiceChatRoom> roomList = new ArrayList<>();

            roomList.addAll(newRoomResults);
            roomList.addAll(oldRoomResults);

            if (cardAdapter == null) {
                cardAdapter = new ChatRoomAdapter(roomList) {
                    @NonNull
                    @Override
                    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        viewHolder viewHolder = super.onCreateViewHolder(parent, viewType);

                        viewHolder.itemView.setOnClickListener(v -> {
                            int position = viewHolder.getAdapterPosition();
                            int chatRoomId = cardAdapter.getChatRoomId(position);
                            String OpponentUserName = cardAdapter.getChatRoom(position).getOpponentUser().getName();
                            moveToChatActivity(chatRoomId, OpponentUserName);
                        });
                        return viewHolder;
                    }
                };
            } else {
                cardAdapter.setList(roomList);
            }

            recyclerView.setEmptyView(emptyView);
            recyclerView.setAdapter(cardAdapter);
            cardAdapter.notifyDataSetChanged();
        } else {
            Log.d(TAG, "load server data");
            Call<ArrayList<VoiceChatRoom>> request = service.loadVoiceChatRoomList();
            request.enqueue(new Callback<ArrayList<VoiceChatRoom>>() {
                @Override
                public void onResponse(Call<ArrayList<VoiceChatRoom>> call, Response<ArrayList<VoiceChatRoom>> response) {
                    if (response.isSuccessful()) {
                        if (cardAdapter == null) {
                            cardAdapter = new ChatRoomAdapter(response.body()) {
                                @NonNull
                                @Override
                                public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                    viewHolder viewHolder = super.onCreateViewHolder(parent, viewType);

                                    viewHolder.itemView.setOnClickListener(v -> {
                                        int position = viewHolder.getAdapterPosition();
                                        int chatRoomId = cardAdapter.getChatRoomId(position);
                                        String OpponentUserName = cardAdapter.getChatRoom(position).getOpponentUser().getName();
                                        moveToChatActivity(chatRoomId, OpponentUserName);

                                    });

                                    return viewHolder;
                                }
                            };
                        } else {
                            cardAdapter.setList(response.body());
                        }

                        recyclerView.setEmptyView(emptyView);
                        recyclerView.setAdapter(cardAdapter);
                        cardAdapter.notifyDataSetChanged();

                        for (VoiceChatRoom itRoom : response.body()) {
                            realm.executeTransaction(realm -> realm.copyToRealmOrUpdate(itRoom));
                        }

                        SharedPrefHelper.getInstance(getContext()).setSharedPreferences(SharedPrefHelper.CHAT_ROOM_DATA_UPDATE_TIME, System.currentTimeMillis());

                    } else {
                        try {
                            Utils.toastError(getContext(), response);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ArrayList<VoiceChatRoom>> call, Throwable t) {
                    t.printStackTrace();
                    Log.e(TAG, "message: " + t.getMessage());
                }
            });
        }

    }

    private void moveToChatActivity(int chatRoomId, String opponentUserName) {
        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra("chatRoomId", chatRoomId);
        intent.putExtra("opponentName", opponentUserName);
        startActivity(intent);
        realm.executeTransactionAsync(realm1 -> {
            VoiceChatRoom oldRoom = realm1.where(VoiceChatRoom.class).equalTo("id", chatRoomId).findFirst();
            oldRoom.setNewRoom(false);
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
//        if (refreshEvent.action == RefreshEvent.Action.SEND_NEW_STORY) {
//            int sendPosition = refreshEvent.position;
//            if (sendPosition != -1) {
//                VoiceChatRoom add = Utils.loadRooms(getContext()).get(sendPosition);
//                cardAdapter.addItem(add);
//                cardAdapter.notifyDataSetChanged();
//            }
//        } else if(refreshEvent.action == RefreshEvent.Action.NEW_CHAT_ROOM) {
//            VoiceChatRoom newRoom = refreshEvent.voiceChatRoom;
//            if (newRoom != null){
//                cardAdapter.addItem(newRoom);
//                cardAdapter.notifyDataSetChanged();
//            }
//        }
        if (refreshEvent.action == RefreshEvent.Action.STATUS_CHANGE &&
                RefreshEvent.TYPE_REPLY.equals(refreshEvent.type) &&
                isActive) {
            loadChatRooms();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        Log.e("event bus", "onstop()");
        unbinder.unbind();

    }

    public class ChatRoomAdapter extends RecyclerView.Adapter<viewHolder> {

        private ArrayList<VoiceChatRoom> rooms;

        public ChatRoomAdapter() {
            rooms = new ArrayList<>();
        }

        public ChatRoomAdapter(ArrayList<VoiceChatRoom> rooms) {
            if (this.rooms == null) {
                this.rooms = new ArrayList<VoiceChatRoom>();
            }
            this.rooms = rooms;
        }

        public void setList(ArrayList<VoiceChatRoom> rooms) {
            this.rooms = rooms;
        }

        MultiTransformation multiTransformation = new MultiTransformation(new BlurTransformation(25, 3),
                new FitCenter(), new CircleCrop());

        @NonNull
        @Override
        public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_room, parent, false);
//            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) view.getLayoutParams();
//            layoutParams.width = layoutParams.width - 5;
//            view.requestLayout();
            viewHolder viewHolder = new viewHolder(view);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull viewHolder holder, int position) {
            VoiceChatRoom currentRoom = rooms.get(position);

            long time = (currentRoom.getLastChatDate() == 0L ? currentRoom.getCreatedAt() : currentRoom.getLastChatDate());
            User opponentUser = currentRoom.getOpponentUser();
            User me = SharedPrefHelper.getInstance(getContext()).getUserInfo();

            holder.lastTime.setText(Utils.setChatTime(time));
            if(opponentUser != null){
                holder.chatDesc.setText(DistanceUtil.getDistanceFromLatLng(opponentUser, me) + "km");
                holder.name.setText(opponentUser.getName());
                holder.age.setText("" + AgeUtil.getAgeFromBirth(opponentUser.getBirth()));
                Glide.with(getContext())
                        .load(opponentUser.getProfileImageUrl())
                        .apply(RequestOptions.bitmapTransform(multiTransformation))
                        .into(holder.profileImage);
            }
            if (currentRoom.isNewRoom())
                holder.newBadge.setVisibility(View.VISIBLE);
            else
                holder.newBadge.setVisibility(View.GONE);
        }

        @Override
        public int getItemCount() {
            return rooms.size();
        }

        public VoiceChatRoom getChatRoom(int position) {
            return this.rooms.get(position);
        }

        public int getChatRoomId(int position) {
            return this.rooms.get(position).getId();
        }

        public void addItem(VoiceChatRoom addItem) {
            rooms.add(addItem);
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
        @BindView(R.id.tv_age)
        TextView age;
        @BindView(R.id.tv_new_badge)
        TextView newBadge;

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
