package com.square.renov.swipevoicechat.Activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarChangeListener;
import com.crystal.crystalrangeseekbar.interfaces.OnSeekbarChangeListener;
import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar;
import com.crystal.crystalrangeseekbar.widgets.CrystalSeekbar;
import com.square.renov.swipevoicechat.Model.Filter;
import com.square.renov.swipevoicechat.Model.User;
import com.square.renov.swipevoicechat.Network.NetRetrofit;
import com.square.renov.swipevoicechat.Network.ApiService;
import com.square.renov.swipevoicechat.R;
import com.square.renov.swipevoicechat.Util.SharedPrefHelper;
import com.square.renov.swipevoicechat.Util.Utils;

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
    @BindView(R.id.tv_title_bar)
    TextView titleBar;

    @BindView(R.id.rangebar_age)
    CrystalRangeSeekbar rangeBarAge;
    @BindView(R.id.sw_gender)
    ImageView swGender;
    boolean isCheckedGender;
    @BindView(R.id.tv_gender_desc)
    TextView tvGenderDesc;
    @BindView(R.id.radio_group_gender)
    RadioGroup rgGender;
    @BindView(R.id.radio_male)
    RadioButton radioMale;
    @BindView(R.id.radio_female)
    RadioButton radioFemale;
    @BindView(R.id.sw_active_user)
    ImageView swActiveUser;
    boolean isCheckedActiveUser;
    @BindView(R.id.sw_age_range)
    ImageView swAgeRange;
    boolean isCheckedAgeRange;
    @BindView(R.id.tv_age_range)
    TextView tvAgeRange;


    int AgeLeftValue = 0, AgeRightValue = 0, ageGap = 5;

    ApiService service = NetRetrofit.getInstance(this).getService();

    Unbinder unbinder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        unbinder = ButterKnife.bind(this);

        titleBar.setText("필터");

        initFilter();

        swGender.setOnClickListener(v -> {
            if (!isCheckedGender) {
                rgGender.setVisibility(View.VISIBLE);
                if(rgGender.getCheckedRadioButtonId() != -1)
                    tvGenderDesc.setVisibility(View.VISIBLE);
                else
                    tvGenderDesc.setVisibility(View.GONE);
                checkRadioButton(swGender, true);
            } else {
                rgGender.setVisibility(View.GONE);
                tvGenderDesc.setVisibility(View.GONE);
                checkRadioButton(swGender, false);
            }
        });

        rgGender.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_male) {
                setGenderDesc("M");
            } else if (checkedId == R.id.radio_female) {
                setGenderDesc("F");
            }
        });

        swActiveUser.setOnClickListener(v -> {
            if (!isCheckedActiveUser){
                checkRadioButton(swActiveUser, true);
            } else {
                checkRadioButton(swActiveUser, false);
            }
        });

        swAgeRange.setOnClickListener(v -> {
            if (!isCheckedAgeRange) {
                rangeBarAge.setVisibility(View.VISIBLE);
                tvAgeRange.setVisibility(View.VISIBLE);
                checkRadioButton(swAgeRange, true);
            } else {
                rangeBarAge.setVisibility(View.GONE);
                tvAgeRange.setVisibility(View.GONE);
                checkRadioButton(swAgeRange, false);
            }
        });

        rangeBarAge.setOnRangeSeekbarChangeListener((minValue, maxValue) -> tvAgeRange.setText(String.valueOf(minValue) + "~" + String.valueOf(maxValue) + "세"));
    }

    public void checkRadioButton(ImageView radioView, boolean isChecked){
        if(isChecked)
            radioView.setBackgroundResource(R.drawable.radio_on);
        else
            radioView.setBackgroundResource(R.drawable.radio_off);

        int id = radioView.getId();
        switch (id){
            case R.id.sw_gender:
                isCheckedGender = isChecked;
                break;
            case R.id.sw_active_user:
                isCheckedActiveUser = isChecked;
                break;
            case R.id.sw_age_range:
                isCheckedAgeRange = isChecked;
                break;
        }
    }

    private void initFilter() {
        Call<Filter> request = service.checkFilter();
        request.enqueue(new Callback<Filter>() {
            @Override
            public void onResponse(Call<Filter> call, Response<Filter> response) {
                if (response.isSuccessful()) {
                    Filter filter = response.body();

                    if (filter == null) {
                        checkRadioButton(swActiveUser, false);
                        checkRadioButton(swAgeRange, false);
                        checkRadioButton(swGender, false);
                        return;
                    }

                    checkRadioButton(swActiveUser, filter.getActiveUser());

                    if (filter.getAgeMin() == 0 && filter.getAgeMax() == 0) {
                        checkRadioButton(swAgeRange, false);
                    } else {
                        checkRadioButton(swAgeRange, true);
                        int minAge = 12, maxAge = 44;
                        if (filter.getAgeMin() > 12 && filter.getAgeMax() < 44) {
                            minAge = filter.getAgeMin();
                            maxAge = filter.getAgeMax();
                        }
                        AgeLeftValue = minAge;
                        AgeRightValue = maxAge;
                        rangeBarAge.setVisibility(View.VISIBLE);
                        tvAgeRange.setVisibility(View.VISIBLE);
                        rangeBarAge.setMinStartValue(minAge);
                        rangeBarAge.setMaxStartValue(maxAge);
                    }

                    //TODO: 성별 선택? 이성 보이스 듣기?
                    if (filter.getGender() != null) {
                        rgGender.setVisibility(View.VISIBLE);
                        tvGenderDesc.setVisibility(View.VISIBLE);
                        checkRadioButton(swGender, true);
                        isCheckedSameGender(filter.getGender());
                    } else {
                        checkRadioButton(swGender, false);
                    }

                    Log.e(TAG, "filter\n" +
                            "\nactive user: " + filter.getActiveUser() +
                            "\nage max: " + filter.getAgeMax() +
                            "\nage min: " + filter.getAgeMin() +
                            "\ngender: " + filter.getGender());
                } else {
                    try {
                        Utils.toastError(getApplicationContext(), response);
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

        myGenderButton = "F".equals(gender) ? radioFemale : radioMale;

        setGenderDesc(gender);
        myGenderButton.setChecked(true);
    }

    private void setGenderDesc(String gender) {
        tvGenderDesc.setVisibility(View.VISIBLE);
        String oppenentGender = "F".equals(gender) ? "'여성'" : "'남성'";
        String genderDesc = oppenentGender + "의 이야기를 5개 가져옵니다";
        tvGenderDesc.setText(genderDesc);
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
        Call<Filter> request = service.updateFilter(filter);
        request.enqueue(new Callback<Filter>() {
            @Override
            public void onResponse(Call<Filter> call, Response<Filter> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "update filter success");
                } else {
                    try {
                        Utils.toastError(getApplicationContext(), response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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

        result.setActiveUser(isCheckedActiveUser);

        if (isCheckedAgeRange) {
            result.setAgeMin((int) rangeBarAge.getSelectedMinValue());
            result.setAgeMax((int) rangeBarAge.getSelectedMaxValue());
        } else {
            result.setAgeMin(0);
            result.setAgeMax(0);
        }

        if (isCheckedGender) {
            User me = SharedPrefHelper.getInstance(this).getUserInfo();
            if ("F".equals(me.getGender())) {
                result.setGender("M");
            } else {
                result.setGender("F");
            }
        } else {
            result.setGender(null);
        }
        Log.e(TAG, "setFilter] is active user: " + result.getActiveUser());
        Log.e(TAG, "setFilter] max Age: " + result.getAgeMax());
        Log.e(TAG, "setFilter] min Age: " + result.getAgeMin());
        Log.e(TAG, "setFilter] gender: " + result.getGender());

        return result;
    }

    @OnClick(R.id.iv_back)
    public void onClickBack(){
        finish();
    }
}
