package com.square.renov.swipevoicechat.Util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.square.renov.swipevoicechat.Activity.ShopActivity;
import com.square.renov.swipevoicechat.Model.PointLog;
import com.square.renov.swipevoicechat.Model.Profile;
import com.square.renov.swipevoicechat.Model.Result;
import com.square.renov.swipevoicechat.Model.User;
import com.square.renov.swipevoicechat.Model.VoiceCard;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.square.renov.swipevoicechat.Model.VoiceChat;
import com.square.renov.swipevoicechat.Model.VoiceChatRoom;
import com.square.renov.swipevoicechat.Network.NetRetrofit;
import com.square.renov.swipevoicechat.R;

import org.json.JSONArray;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Utils {
    private static final String TAG = "Utils";

    public static List<Profile> loadProfiles(Context context) {
        try {
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            JSONArray array = new JSONArray(loadJSONFromAsset(context, "profiles.json"));
            List<Profile> profileList = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                Profile profile = gson.fromJson(array.getString(i), Profile.class);
                profileList.add(profile);
            }
            return profileList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<VoiceCard> loadCards(Context context) {
        try {
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            JSONArray array = new JSONArray(loadJSONFromAsset(context, "voicecards.json"));
            ArrayList<VoiceCard> voiceCards = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                VoiceCard voiceCard = gson.fromJson(array.getString(i), VoiceCard.class);
                voiceCards.add(voiceCard);
            }
            return voiceCards;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<VoiceChatRoom> loadRooms(Context context) {
        try {
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            JSONArray array = new JSONArray(loadJSONFromAsset(context, "voicechatrooms.json"));
            ArrayList<VoiceChatRoom> voiceChatRooms = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                VoiceChatRoom voiceChatRoom = gson.fromJson(array.getString(i), VoiceChatRoom.class);
                voiceChatRooms.add(voiceChatRoom);
            }
            return voiceChatRooms;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<VoiceChat> loadChats(Context context) {
        try {
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            JSONArray array = new JSONArray(loadJSONFromAsset(context, "voicechats.json"));
            List<VoiceChat> voiceChats = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                VoiceChat voiceChat = gson.fromJson(array.getString(i), VoiceChat.class);
                voiceChats.add(voiceChat);
            }
            return voiceChats;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<PointLog> loadPointLogs(Context context) {
        try {
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            JSONArray array = new JSONArray(loadJSONFromAsset(context, "pointlogs.json"));
            ArrayList<PointLog> pointLogs = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                PointLog pointLog = gson.fromJson(array.getString(i), PointLog.class);
                pointLogs.add(pointLog);
            }
            return pointLogs;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String loadJSONFromAsset(Context context, String jsonFileName) {
        String json = null;
        InputStream is = null;
        try {
            AssetManager manager = context.getAssets();
            Log.d(TAG, "path " + jsonFileName);
            is = manager.open(jsonFileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public static String getPlayTimeFormat(int playTime) {
        return String.format("%02d", (playTime / 1000 / 60) % 60) + ":" + String.format("%02d", (playTime / 1000) % 60);
    }

    public static String setChatTime(long time) {
        long currentTime = System.currentTimeMillis();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        String result = "";
        if (currentTime - time < 24 * 60 * 60 * 1000) {
            int AM_PM = calendar.get(Calendar.AM_PM);
            result = (AM_PM == Calendar.AM ? "오전 " : "오후 ") + (calendar.get(Calendar.HOUR) == 0 ? 12 : calendar.get(Calendar.HOUR)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE));
        } else if (24 * 60 * 60 * 1000 <= currentTime - time) {
            result = calendar.get(Calendar.YEAR) + ". " + (calendar.get(Calendar.MONTH) + 1) + ". " + calendar.get(Calendar.DATE);
        }


        return result;
    }


    public static Result parseError(Response response) throws IOException {
        Gson gson = new Gson();
        return gson.fromJson(response.errorBody().string(), Result.class);
    }

    public static void toastError(Context context, Response response) throws IOException {
        Gson gson = new Gson();
        Result result = gson.fromJson(response.errorBody().string(), Result.class);
        Toast.makeText(context, result.getMessage(), Toast.LENGTH_SHORT).show();
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static String setRecordTimer(long time) {
        String result =
//                String.format("%02d", (time/1000/60)%60) + ":" +
                String.format("%02d", (time / 1000) % 60) + ":" + String.format("%02d", (time % 1000) / 10);
        Log.d("set record timer", result);
        return result;
    }

    public static String getCardCreatedAt(long createdAt) {
        String result = "";
        long currentTime = System.currentTimeMillis();
        long diffTime = currentTime - createdAt;
        if (0 < diffTime && diffTime <= 5 * 60 * 1000) {
            result = "<b>1</b> min";
        } else if (5 * 60 * 1000 < diffTime && diffTime <= 30 * 60 * 1000) {
            result = "<b>5</b> min";
        } else if (30 * 60 * 1000 < diffTime && diffTime <= 60 * 60 * 1000) {
            result = "<b>30 min";
        } else if (60 * 60 * 1000 < diffTime && diffTime <= 3 * 60 * 60 * 1000) {
            result = "<b>1</b> hour";
        } else if (3 * 60 * 60 * 1000 < diffTime && diffTime <= 12 * 60 * 60 * 1000) {
            result = "<b>3</b> hour";
        } else {
            result = "<b>12</b> hour";
        }

        return result;
    }

    public static Point getDisplaySize(Context context) {
        Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public static SpannableStringBuilder setNameAndAge(String name, int ageFromBirth) {
        SpannableStringBuilder str = new SpannableStringBuilder(name + "  " + ageFromBirth);
        str.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return str;
    }

    public static byte[] getSampleWave() {
        return new byte[]{
                (byte) 11, (byte) 21, (byte) 31, (byte) 41, (byte) 11,
                (byte) 21, (byte) 31, (byte) 41, (byte) 21, (byte) 41,
                (byte) 21, (byte) 41, (byte) 21, (byte) 31, (byte) 41,
                (byte) 21, (byte) 41, (byte) 21, (byte) 41, (byte) 31,
                (byte) 21, (byte) 21, (byte) 41, (byte) 21, (byte) 41,
                (byte) 31, (byte) 41, (byte) 31, (byte) 41, (byte) 31,
                (byte) 31, (byte) 21, (byte) 31, (byte) 41, (byte) 31,
                (byte) 31, (byte) 21, (byte) 31, (byte) 21, (byte) 31,
                (byte) 31, (byte) 31, (byte) 41, (byte) 31, (byte) 21,
                (byte) 41, (byte) 31, (byte) 31, (byte) 21, (byte) 31,
                (byte) 41, (byte) 31, (byte) 41, (byte) 31, (byte) 21,
                (byte) 41, (byte) 31, (byte) 41, (byte) 31, (byte) 21
        };
    }

    public static boolean needToDataUpdate(Context context, String updateDataName) {
        long lastUpdateTime = SharedPrefHelper.getInstance(context).getSharedPreferences(updateDataName, 0L);
        long currentTime = System.currentTimeMillis();

        Log.d("need to data update]", "last update time : " + lastUpdateTime + " / current time: " + currentTime);
        long timeDiff = currentTime - lastUpdateTime;

        if (timeDiff > 24 * 60 * 60 * 1000)
            return true;
        else
            return false;
    }

    public static boolean haveEnoughReplyLuna(Context context, int needLuna) {
        User me = SharedPrefHelper.getInstance(context).getUserInfo();
        if (me.getLuna() >= needLuna) {
            return true;
        }

        MaterialDialog reportDialog = new MaterialDialog.Builder(context)
                .customView(R.layout.dialog_code, false)
                .show();

        DialogUtils.initDialogView(reportDialog, context);

        TextView tvTitle = (TextView) reportDialog.findViewById(R.id.tv_title);
        TextView tvContent = (TextView) reportDialog.findViewById(R.id.tv_content);
        TextView tvConfirm = (TextView) reportDialog.findViewById(R.id.tv_send_code);
        TextView tvCancel = (TextView) reportDialog.findViewById(R.id.tv_cancel);
        EditText etInviteCode = (EditText) reportDialog.findViewById(R.id.et_code);

        etInviteCode.setVisibility(View.GONE);

        tvTitle.setText(needLuna - me.getLuna() + "루나가 부족합니다.");
        tvContent.setText("루나를 충전하러 가시겠어요?");
        tvConfirm.setText("충전하기");

        tvConfirm.setBackgroundResource(R.drawable.button_text_background);
        tvConfirm.setTextColor(context.getResources().getColorStateList(R.color.button_text_color));
        tvConfirm.setEnabled(true);
        tvConfirm.setOnClickListener(v -> {
            Toast.makeText(context, "send code", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(context, ShopActivity.class);
            context.startActivity(intent);
        });

        tvCancel.setText("취소");
        tvCancel.setOnClickListener(v -> {
            reportDialog.dismiss();
        });

        return false;
    }

    public static String setLunaCount(int luna) {
        String result = "";
        if(luna > 999){
            return result = "999 +";
        }
        return result = String.valueOf(luna);
    }

    public static void refreshMyInfo(Context context){
        Call<User> request = NetRetrofit.getInstance(context).getService().checkCurrentUserInfo();
        request.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    Gson gson = new Gson();
                    String stringUserInfo = gson.toJson(response.body());
                    SharedPrefHelper.getInstance(context).setSharedPreferences(SharedPrefHelper.USER_INFO, stringUserInfo);
                } else {
                    try {
                        Utils.toastError(context, response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public static void getHashKey(Context context){
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageInfo == null)
            Log.e("KeyHash", "KeyHash:null");

        for (Signature signature : packageInfo.signatures) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            } catch (NoSuchAlgorithmException e) {
                Log.e("KeyHash", "Unable to get MessageDigest. signature=" + signature, e);
            }
        }
    }
}
