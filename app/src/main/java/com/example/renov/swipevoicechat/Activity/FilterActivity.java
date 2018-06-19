package com.example.renov.swipevoicechat.Activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.appyvet.rangebar.RangeBar;
import com.example.renov.swipevoicechat.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class FilterActivity extends AppCompatActivity {
    @BindView(R.id.radioGroup_age)
    RadioGroup radioGroupAge;
    @BindView(R.id.rangebar_age)
    RangeBar rangeBarAge;
    @BindView(R.id.sw_gender)
    Switch swGender;
    @BindView(R.id.sw_age_range)
    Switch swAgeRange;
    @BindView(R.id.tv_age_range)
    TextView tvAgeRange;

    Unbinder unbinder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        unbinder = ButterKnife.bind(this);

        swGender.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked)
                radioGroupAge.setVisibility(View.VISIBLE);
            else
                radioGroupAge.setVisibility(View.GONE);
        });

        swAgeRange.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            if(isChecked)
                rangeBarAge.setVisibility(View.VISIBLE);
            else
                rangeBarAge.setVisibility(View.GONE);
        }));


        rangeBarAge.setOnRangeBarChangeListener((rangeBar, leftPinIndex, rightPinIndex, leftPinValue, rightPinValue) -> tvAgeRange.setText(leftPinValue + "~" + rightPinValue + "세"));
    }

    @OnClick(R.id.sw_gender)
    public void onClickSwitchGender() {

    }

    @OnClick(R.id.sw_age_range)
    public void onClickSwitchAgeRange() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}
