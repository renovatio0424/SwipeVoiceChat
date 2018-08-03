package com.square.renov.swipevoicechat.Activity;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.square.renov.swipevoicechat.Model.PointLog;
import com.square.renov.swipevoicechat.Network.NetRetrofit;
import com.square.renov.swipevoicechat.R;
import com.square.renov.swipevoicechat.Util.Utils;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PointLogActivity extends AppCompatActivity {
    @BindView(R.id.recyclerview)
    RecyclerView recyclerView;
    @BindView(R.id.tv_title_bar)
    TextView titleBar;

    PointLogAdapter pointLogAdapter;
    Unbinder unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point_log);
        unbinder = ButterKnife.bind(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        pointLogAdapter = new PointLogAdapter(this, Utils.loadPointLogs(this));
        loadPointList();

        titleBar.setText("루나 사용 내역");
    }

    private void loadPointList() {
        Call<ArrayList<PointLog>> request = NetRetrofit.getInstance(this).getService().loadLunaLogList(100,0);
        request.enqueue(new Callback<ArrayList<PointLog>>() {
            @Override
            public void onResponse(Call<ArrayList<PointLog>> call, Response<ArrayList<PointLog>> response) {
                if(response.isSuccessful()){
                    pointLogAdapter = new PointLogAdapter(getApplicationContext(), response.body());
                    recyclerView.setAdapter(pointLogAdapter);
                    pointLogAdapter.notifyDataSetChanged();
                } else {
                    try {
                        Utils.toastError(getApplicationContext(), response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ArrayList<PointLog>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @OnClick(R.id.iv_back)
    public void onClickBack() {
        finish();
    }

    public class PointLogAdapter extends RecyclerView.Adapter {
        private Context context;
        private ArrayList<PointLog> pointLogList;

        public PointLogAdapter(Context context, ArrayList<PointLog> pointLogList) {
            this.context = context;
            this.pointLogList = pointLogList;
        }

        public class PointLogHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.date_text)
            TextView dateText;
            @BindView(R.id.desc_text)
            TextView descText;
            @BindView(R.id.point_diff_text)
            TextView pointDiffText;
            @BindView(R.id.adjust_point_text)
            TextView adjustPointText;

            public PointLogHolder(View v) {
                super(v);
                ButterKnife.bind(this, v);
            }
        }

        @Override
        public PointLogHolder onCreateViewHolder(ViewGroup parent,
                                                 int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_point_log, parent, false);
            PointLogHolder vh = new PointLogHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            final PointLog pointLog = pointLogList.get(position);
            PointLogHolder pointLogHolder = (PointLogHolder) holder;

            pointLogHolder.dateText.setText(getDate(String.valueOf(pointLog.getCreatedAt())));
            pointLogHolder.descText.setText(pointLog.getEvent().getMessage());
            pointLogHolder.pointDiffText.setText("" + pointLog.getAmount());
            pointLogHolder.adjustPointText.setText("" + pointLog.getRemain());
        }

        @Override
        public int getItemCount() {
            return pointLogList.size();
        }

        private String getDate(String createdAt) {
            long timestamp = Long.valueOf(createdAt);
            String time = "";
            try {
                SimpleDateFormat df = new SimpleDateFormat("yy.MM.dd");
                Timestamp stamp = new Timestamp(timestamp);
                Date date = new Date(stamp.getTime());
                time = df.format(date);
            } catch (Exception e) {
            }

            return time;
        }

    }

}
