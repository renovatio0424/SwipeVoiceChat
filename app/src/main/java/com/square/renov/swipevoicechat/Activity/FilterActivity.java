package com.square.renov.swipevoicechat.Activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.appyvet.rangebar.RangeBar;
import com.square.renov.swipevoicechat.Model.Filter;
import com.square.renov.swipevoicechat.Model.User;
import com.square.renov.swipevoicechat.Network.NetRetrofit;
import com.square.renov.swipevoicechat.Network.ApiService;
import com.square.renov.swipevoicechat.R;
import com.square.renov.swipevoicechat.Util.SharedPrefHelper;

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
    @BindView(R.id.rangebar_age)
    RangeBar rangeBarAge;
    @BindView(R.id.sw_gender)
    Switch swGender;
    @BindView(R.id.radio_group_gender)
    RadioGroup rgGender;
    @BindView(R.id.radio_male)
    RadioButton radioMale;
    @BindView(R.id.radio_female)
    RadioButton radioFemale;
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
                rgGender.setVisibility(View.VISIBLE);
            else
                rgGender.setVisibility(View.GONE);
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

                    if (filter == null){
                        swActiveUser.setChecked(false);
                        swAgeRange.setChecked(false);
                        swGender.setChecked(false);
                        return;
                    }

                    swActiveUser.setChecked(filter.getActiveUser());




                    if(filter.getAgeMin() == 0 && filter.getAgeMax() == 0){
                        swAgeRange.setChecked(false);
                    } else {
                        swAgeRange.setChecked(true);
                        int minAge = 12, maxAge = 44;

                        if (filter.getAgeMin() < 12 && filter.getAgeMax() > 44) {
                            minAge = filter.getAgeMin();
                            maxAge = filter.getAgeMax();
                        }
                        rangeBarAge.setRangePinsByValue((float) minAge, (float) maxAge);
                    }




                    //TODO: 성별 선택? 이성 보이스 듣기?
                    if(filter.getGender() != null){
                        swGender.setChecked(true);
                        isCheckedSameGender(filter.getGender());
                    } else {
                        swGender.setChecked(false);
                    }

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

    private void isCheckedSameGender(String gender) {
        User me = SharedPrefHelper.getInstance(this).getUserInfo();
        RadioButton myGenderButton;
        if(gender.equals(me.getGender())){
          myGenderButton = "F".equals(me.getGender()) ? radioFemale : radioMale;
        } else {
          myGenderButton = "F".equals(me.getGender()) ? radioMale : radioFemale;
        }
        myGenderButton.setChecked(true);
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
        Call<Filter> resqeust = service.updateFilter(filter);
        resqeust.enqueue(new Callback<Filter>() {
            @Override
            public void onResponse(Call<Filter> call, Response<Filter> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "update filter success");
                }
            }

            @Override
            public void onFailure(Call<Filter> call, Throwable t) {
                t.printStackTrace();
                Log.e(TAG, "error message: " + t.getMessage());
            }
        });
        super.onDestroy();
        unbinder.unbind();

    }

    private Filter setFilter() {
        Filter result = new Filter();

        result.setActiveUser(swActiveUser.isChecked());

        if (swAgeRange.isChecked()) {
            result.setAgeMin(Integer.valueOf(rangeBarAge.getLeftPinValue()));
            result.setAgeMax(Integer.valueOf(rangeBarAge.getRightPinValue()));
        } else {
            result.setAgeMin(0);
            result.setAgeMax(0);
        }
        //TODO: 상대방 이성 보기?
        if (swGender.isChecked()){
            User me = SharedPrefHelper.getInstance(this).getUserInfo();
            if("F".equals(me.getGender())){
                result.setGender("M");
            } else {
                result.setGender("F");
            }
        } else {
            result.setGender(null);
        }
        Log.e(TAG, "is active user: " + result.getActiveUser());
        Log.e(TAG, "max Age: " + result.getAgeMax());
        Log.e(TAG, "min Age: " + result.getAgeMin());
        Log.e(TAG, "gender: " + result.getGender());

        return result;
    }
}
