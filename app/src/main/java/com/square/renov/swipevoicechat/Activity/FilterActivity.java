package com.square.renov.swipevoicechat.Activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.appyvet.rangebar.RangeBar;
import com.square.renov.swipevoicechat.Model.Filter;
import com.square.renov.swipevoicechat.Network.NetRetrofit;
import com.square.renov.swipevoicechat.Network.ApiService;
import com.square.renov.swipevoicechat.R;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FilterActivity extends AppCompatActivity {
    private static final String TAG = FilterActivity.class.getSimpleName();
    @BindView(R.id.radioGroup_age)
    RadioGroup radioGroupAge;
    @BindView(R.id.rangebar_age)
    RangeBar rangeBarAge;
    @BindView(R.id.sw_gender)
    Switch swGender;
    @BindView(R.id.sw_active_user)
    Switch swActiveUser;
    @BindView(R.id.sw_age_range)
    Switch swAgeRange;
    @BindView(R.id.tv_age_range)
    TextView tvAgeRange;

    ApiService service = NetRetrofit.getInstance(this).getService();

    Unbinder unbinder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        unbinder = ButterKnife.bind(this);

        initFilter();


        swGender.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked)
                radioGroupAge.setVisibility(View.VISIBLE);
            else
                radioGroupAge.setVisibility(View.GONE);
        });

        swAgeRange.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            if (isChecked)
                rangeBarAge.setVisibility(View.VISIBLE);
            else
                rangeBarAge.setVisibility(View.GONE);
        }));


        rangeBarAge.setOnRangeBarChangeListener((rangeBar, leftPinIndex, rightPinIndex, leftPinValue, rightPinValue) -> tvAgeRange.setText(leftPinValue + "~" + rightPinValue + "세"));
    }

    private void initFilter() {
        Call<Filter> request = service.checkFilter();
        request.enqueue(new Callback<Filter>() {
            @Override
            public void onResponse(Call<Filter> call, Response<Filter> response) {
                if (response.isSuccessful()) {
                    Filter filter = response.body();

                    if (filter == null)
                        return;

                    swActiveUser.setChecked(filter.getActiveUser());
                    swAgeRange.setChecked(true);
                    rangeBarAge.setRangePinsByValue((float) filter.getAgeMin(), (float) filter.getAgeMax());
//                    TODO: 성별 선택? 이성 보이스 듣기?
                    swGender.setChecked(true);
                    Log.e(TAG, "filter\n" +
                            "\nactive user: " + filter.getActiveUser() +
                            "\nage max: " + filter.getAgeMax() +
                            "\nage min: " + filter.getAgeMin() +
                            "\ngender: " + filter.getGender());
                } else {
                    try {
                        Log.e(TAG, "code: " + response.code() + "\nerror body: " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<Filter> call, Throwable t) {

            }
        });
    }

    @OnClick(R.id.sw_gender)
    public void onClickSwitchGender() {

    }

    @OnClick(R.id.sw_age_range)
    public void onClickSwitchAgeRange() {

    }

    @Override
    protected void onDestroy() {
        //TODO: 필터 업데이트
        Filter filter = setFilter();
        service.updateFilter(filter);

        super.onDestroy();
        unbinder.unbind();

    }

    private Filter setFilter() {
        Filter result = new Filter();
        if (swActiveUser.isChecked())
            result.setActiveUser(true);

        if (swAgeRange.isChecked()) {
            result.setAgeMin(rangeBarAge.getLeft());
            result.setAgeMax(rangeBarAge.getRight());
        }
//      TODO: 상대방 이성 보기?
//        if (swGender.isChecked())
//            result.setGender();
        return result;
    }
}
