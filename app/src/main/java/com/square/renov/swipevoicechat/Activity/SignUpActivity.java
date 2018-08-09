package com.square.renov.swipevoicechat.Activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListPopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.square.renov.swipevoicechat.Model.Result;
import com.square.renov.swipevoicechat.Model.User;
import com.square.renov.swipevoicechat.Network.NetRetrofit;
import com.square.renov.swipevoicechat.R;
import com.square.renov.swipevoicechat.Util.AdbrixUtil;
import com.square.renov.swipevoicechat.Util.SharedPrefHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.square.renov.swipevoicechat.Util.Utils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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

    @BindView(R.id.tv_male)
    TextView tvMale;
    @BindView(R.id.tv_female)
    TextView tvFemale;
    boolean isMale = true;
    @BindView(R.id.group_y_n)
    Button locationAgreement;
    boolean isAgreeLocationTerms = false;
    @BindView(R.id.tv_start)
    TextView tvStart;
    @BindView(R.id.spinner)
    Spinner spinner;


    private FusedLocationProviderClient mFusedLocationClient;
    Location mlocation;

    Unbinder unbinder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        unbinder = ButterKnife.bind(this);

        //adbrix facebook , google 계정 확인을 위한 코드
        SharedPrefHelper.getInstance(SignUpActivity.this).setSharedPreferences(SharedPrefHelper.SNS_TYPE, getIntent().getStringExtra("type"));

        AdbrixUtil.setFirstTimeExperience(this, SharedPrefHelper.SIGNUP);

        tvStart.setActivated(false);


        try {
            setSpinner();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        tvMale.setOnClickListener(v -> {
            isMale = true;
            tvMale.setBackgroundResource(R.drawable.a_gender_full);
            tvFemale.setBackgroundResource(R.drawable.a_gender_empty);
        });

        tvFemale.setOnClickListener(v -> {
            isMale = false;
            tvFemale.setBackgroundResource(R.drawable.a_gender_full);
            tvMale.setBackgroundResource(R.drawable.a_gender_empty);
        });

        locationAgreement.setAlpha(0.6f);

        locationAgreement.setOnClickListener(v -> {
            if (isAgreeLocationTerms) {
//                locationAgreement.setSelected(isAgreeLocationTerms);
//                locationAgreement.setBackgroundResource(R.drawable.background_google);
                isAgreeLocationTerms = false;
                v.post(() -> v.setAlpha(0.6f));
            } else {
//                locationAgreement.setSelected(isAgreeLocationTerms);
//                locationAgreement.setBackgroundResource(R.drawable.background_spinner);
                isAgreeLocationTerms = true;
                v.post(() -> v.setAlpha(1f));
                getLocation();
            }
        });

//        setBirthdayDialog(birthday);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    int pastPosition = -1;

    private void setSpinner() throws NoSuchFieldException, IllegalAccessException {
        setSpinnerHeight(200);

        int year = Calendar.getInstance().get(Calendar.YEAR);

        ArrayList<String> birthList = new ArrayList<>();

        for (int i = year - 12 + 1; i > year - 44 + 1; i--) {
            birthList.add(String.valueOf(i));
        }

        CustomSpinnerAdapter spinnerAdapter = new CustomSpinnerAdapter(this, birthList);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (pastPosition == -1) {
                    ((TextView) parent.findViewById(R.id.spinnerText)).setTextColor(getResources().getColor(R.color.color_spinner_inactive));
                } else {
                    ((TextView) parent.findViewById(R.id.spinnerText)).setTextColor(getResources().getColor(R.color.main_color));
                }

                pastPosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinner.setAdapter(spinnerAdapter);
    }

    private void setSpinnerHeight(int spinnerHeight) throws NoSuchFieldException, IllegalAccessException {
        Field popup = Spinner.class.getDeclaredField("mPopup");
        popup.setAccessible(true);

        ListPopupWindow window = (ListPopupWindow) popup.get(spinner);
        window.setHeight(Utils.dpToPx(spinnerHeight));
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
                    locationAgreement.setAlpha(1.f);
                    isAgreeLocationTerms = false;
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

    String name;
    String profile;
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

        //TODO: 회원가입 ㄱㄱ 이름, 프로필 사진 등록
        name = getIntent().getStringExtra("name");
        profile = getIntent().getStringExtra("profile");
        type = getIntent().getStringExtra("type");
        token = getIntent().getStringExtra("token");
        gender = (isMale ? "M" : "F");
        birth = spinner.getSelectedItem().toString();
        lat = String.valueOf(mlocation.getLatitude());
        lng = String.valueOf(mlocation.getLongitude());

        Call<User> response = NetRetrofit.getInstance(this).getService().register(token,
                type,
                name,
                gender,
                birth,
                lat,
                lng
        );


        response.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    if (response.code() == 200) {
                        String token = response.headers().get("HelloVoiceAuth");
                        Log.d(TAG, "token: " + token);
                        SharedPrefHelper.getInstance(SignUpActivity.this).setSharedPreferences(SharedPrefHelper.ACCESS_TOKEN, token);
                        SharedPrefHelper.getInstance(SignUpActivity.this).setSharedPreferences(SharedPrefHelper.SNS_TYPE, getIntent().getStringExtra("type"));
                        Gson gson = new Gson();
                        SharedPrefHelper.getInstance(SignUpActivity.this).setSharedPreferences(SharedPrefHelper.USER_INFO, gson.toJson(response.body()));
                        moveToProfile(response.body());
                    }

                } else {
                    try {
                        Result result = Utils.parseError(response);
                        if ("이미 가입한 유저입니다.".equals(result.getMessage())) {
                            String token = response.headers().get("HelloVoiceAuth");
                            Log.d(TAG, "token: " + token);
                            SharedPrefHelper.getInstance(SignUpActivity.this).setSharedPreferences(SharedPrefHelper.ACCESS_TOKEN, token);
                            SharedPrefHelper.getInstance(SignUpActivity.this).setSharedPreferences(SharedPrefHelper.SNS_TYPE, getIntent().getStringExtra("type"));
                            Gson gson = new Gson();
                            SharedPrefHelper.getInstance(SignUpActivity.this).setSharedPreferences(SharedPrefHelper.USER_INFO, gson.toJson(response.body()));
                            moveToProfile(response.body());
                        } else {
                            Utils.toastError(getApplicationContext(), response);
                        }
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
        intent.putExtra("profile", profile);
        startActivity(intent);
    }

    private boolean isCompleteForm() {
        if (isAgreeLocationTerms &&
                mlocation != null) {

            return true;
        } else {
            if (!isAgreeLocationTerms)
                Toast.makeText(this, "disagree", Toast.LENGTH_SHORT).show();
            if (mlocation == null)
                Toast.makeText(this, "location null", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    /**
     * 스피너 커스텀을 위한 클래스
     * 누르기전 layout = spinner_custom_layout
     * 드롭다운 layout = spinner_dropdown_layout
     */
    public class CustomSpinnerAdapter extends BaseAdapter {
        Context context;
        List<String> data;
        LayoutInflater inflater;
        TextView selectTv;

        public CustomSpinnerAdapter(Context context, List<String> data) {
            this.context = context;
            this.data = data;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            if (data != null)
                return data.size();
            else
                return 0;
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.spinner_custom_layout, parent, false);
            }

            if (data != null) {
                String text = data.get(position);
                ((TextView) convertView.findViewById(R.id.spinnerText)).setText(text);
            }
            return convertView;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.spinner_dropdown_layout, parent, false);
            }
            //데이터세팅
            String text = data.get(position);
            ((TextView) convertView.findViewById(R.id.spinnerText)).setText(text);

            return convertView;
        }
    }
}
