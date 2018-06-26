package com.example.renov.swipevoicechat.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListAdapter;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListItem;
import com.example.renov.swipevoicechat.Model.Result;
import com.example.renov.swipevoicechat.Model.User;
import com.example.renov.swipevoicechat.Network.NetRetrofit;
import com.example.renov.swipevoicechat.R;
import com.example.renov.swipevoicechat.Util.SharedPrefHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = SignUpActivity.class.getSimpleName();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
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


    private FusedLocationProviderClient mFusedLocationClient;
    Location mlocation;

    Unbinder unbinder;

    MaterialSimpleListAdapter adapter = new MaterialSimpleListAdapter((dialog, index, item) -> {
        Toast.makeText(this, "click[" + index + "]: " + item.getContent(), Toast.LENGTH_SHORT).show();
        tvBirthday.setText(item.getContent());
        if (isCompleteForm())
            tvStart.setActivated(true);
        dialog.dismiss();
    });

    int birthday = 0;

    MaterialDialog birthdayDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up3);
        unbinder = ButterKnife.bind(this);

        tvStart.setActivated(false);

        int year = Calendar.getInstance().get(Calendar.YEAR);

        for (int i = year - 14 + 1; i > year - 44 + 1; i--) {
            adapter.add(new MaterialSimpleListItem.Builder(this)
                    .content(String.valueOf(i))
                    .backgroundColor(Color.WHITE)
                    .build());

        }

        groupGender.setOnCheckedChangeListener(((group, checkedId) -> {
            if (isCompleteForm()) tvStart.setActivated(true);
        }));

        groupYN.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_yes) {
                getLocation();
                if (isCompleteForm())
                    tvStart.setActivated(true);
            }

        });

        setBirthdayDialog(birthday);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }


    private void getLocation() {
//        TODO : 위치 권한 설정 -> 위치 설정 OK ? -> 위치 정보 가져오기
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION))
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            else
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        // Logic to handle location object
                        mlocation = new Location(location);
                        Toast.makeText(this, "lat: " + mlocation.getLatitude() + "lng: " + mlocation.getLongitude(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:
                // If request is cancelled, the result arrays are empty.

                for (String permission : permissions)
                    Log.e(TAG, "permission: " + permission);

                for (int i : grantResults)
                    Log.e(TAG, "grantResult: " + i);

                if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED))
                    getLocation();
                else {
                    Toast.makeText(this, "위치 권한 설정에 동의하셔야 서비스 이용이 가능합니다.", Toast.LENGTH_SHORT).show();
                    groupYN.check(R.id.radio_no);
                }

//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                    // permission was granted, yay! Do the
//                    // contacts-related task you need to do.
//
//                } else {
//
//                    // permission denied, boo! Disable the
//                    // functionality that depends on this permission.
//                    getLocation();
//                }
                return;

        }
    }

    @OnClick(R.id.iv_back)
    public void onClickBack() {
        finish();
    }

    @OnClick(R.id.tv_birthday)
    public void showBirthdayDialog() {
        birthdayDialog.show();
    }

    private void setBirthdayDialog(int birthday) {
        birthdayDialog = new MaterialDialog.Builder(this)
                .title("생년월일")
                .adapter(adapter, null)
                .limitIconToDefaultSize()
                .build();
    }


    String type;
    String token;
    String gender;
    String birth;
    String lat;
    String lng;

    @OnClick(R.id.tv_start)
    public void clickStartButton() {
        if (!isCompleteForm()) {
            return;
        }

        String result = "result: " + tvBirthday.getText() + "\n"
                + (groupGender.getCheckedRadioButtonId() == R.id.radio_male ? "male" : "female") + "\n"
                + (groupYN.getCheckedRadioButtonId() == R.id.radio_yes ? 'y' : 'n');

        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();

//        TODO: 회원가입 ㄱㄱ

        type = getIntent().getStringExtra("type");
        token = getIntent().getStringExtra("token");
        gender = (groupGender.getCheckedRadioButtonId() == R.id.radio_male ? "M" : "F");
        birth = tvBirthday.getText().toString();
        lat = String.valueOf(mlocation.getLatitude());
        lng = String.valueOf(mlocation.getLongitude());

        Call<User> response = NetRetrofit.getInstance(this).getService().register(token,
                type,
                gender,
                birth,
                lat,
                lng
        );


        response.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    String token = response.headers().get("HelloVoiceAuth");
                    Log.d(TAG, "token: " + token);
                    SharedPrefHelper.getInstance(SignUpActivity.this).setSharedPreferences(SharedPrefHelper.ACCESS_TOKEN, token);
                    moveToProfile(response.body());
                } else {
                    try {
                        Toast.makeText(SignUpActivity.this, "error body: " + response.errorBody().string(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "error body: " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(SignUpActivity.this, "error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void moveToProfile(User user) {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("user", user);
        startActivity(intent);
    }

    private boolean isCompleteForm() {
        if (!tvBirthday.getText().equals("클릭해주세요") &&
                groupGender.getCheckedRadioButtonId() != -1 &&
                groupYN.getCheckedRadioButtonId() != -1)
            return true;

        if (tvBirthday.getText().equals("클릭해주세요"))
            Toast.makeText(this, "출생 연도를 선택해주세요", Toast.LENGTH_SHORT).show();
        else if (groupGender.getCheckedRadioButtonId() == -1)
            Toast.makeText(this, "성별을 선택해주세요", Toast.LENGTH_SHORT).show();
        else if (groupYN.getCheckedRadioButtonId() == -1)
            Toast.makeText(this, "위치 정보 이용을 선택해주세요", Toast.LENGTH_SHORT).show();

        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}
