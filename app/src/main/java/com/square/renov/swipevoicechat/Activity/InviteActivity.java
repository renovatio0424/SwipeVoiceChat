package com.square.renov.swipevoicechat.Activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.square.renov.swipevoicechat.R;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class InviteActivity extends AppCompatActivity {

    Unbinder unbinder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);
        unbinder = ButterKnife.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @OnClick(R.id.insert_recommend_code)
    public void insertRecommendCode(){
        new MaterialDialog.Builder(this)
                .title("코드 입력")
                .content("초대 코드를 입력하면, \n루나가 지급됩니다.")
                .inputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
                .inputRangeRes(2, 20, R.color.colorAccent)
                .input("ex) 6VAC", null, (dialog, input) -> {
                    Toast.makeText(this, "input: " + input.toString(), Toast.LENGTH_SHORT).show();
                })
                .positiveText("사용하기")
                .negativeText("취소")
                .show();

    }
}
