package com.example.renov.swipevoicechat.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListAdapter;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListItem;
import com.example.renov.swipevoicechat.R;
import com.example.renov.swipevoicechat.Utils;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class SignUpActivity extends AppCompatActivity {

    @BindView(R.id.tv_birthday)
    TextView tvBirthday;
    @BindView(R.id.group_gender)
    RadioGroup groupGender;
    @BindView(R.id.radio_male)
    RadioButton radioMale;
    @BindView(R.id.group_y_n)
    RadioGroup groupYN;
    @BindView(R.id.tv_start)
    TextView tvStart;


    Unbinder unbinder;

    MaterialSimpleListAdapter adapter = new MaterialSimpleListAdapter((dialog, index, item) -> {
        Toast.makeText(this, "click[" + index + "]: " + item.getContent(), Toast.LENGTH_SHORT).show();
        tvBirthday.setText(item.getContent());
        dialog.dismiss();
    });

    int birthday = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        unbinder = ButterKnife.bind(this);

        int year = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = year - 14 + 1; i > year - 44 + 1; i--) {
            adapter.add(new MaterialSimpleListItem.Builder(this)
                    .content(String.valueOf(i))
                    .backgroundColor(Color.WHITE)
                    .build());
        }

        groupGender.check(R.id.radio_male);
        groupYN.check(R.id.radio_yes);

        setBirthdayDialog(birthday);
    }

    @OnClick(R.id.tv_birthday)
    public void showBirthdayDialog() {
        setBirthdayDialog(0);
    }

    private void setBirthdayDialog(int birthday) {
        new MaterialDialog.Builder(this)
                .title("생년월일")
                .adapter(adapter, null)
                .limitIconToDefaultSize()
                .show();
    }

    @OnClick(R.id.tv_start)
    public void clickStartButton(){
        String result = "result: " + tvBirthday.getText() + "\n"
                + (groupGender.getCheckedRadioButtonId() == R.id.radio_male ? "male" : "female") + "\n"
                + (groupYN.getCheckedRadioButtonId() == R.id.radio_yes ? 'y' : 'n');
        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}
