package com.square.renov.swipevoicechat.Util;

import android.content.Context;
import android.util.Log;

import com.igaworks.adbrix.IgawAdbrix;
import com.square.renov.swipevoicechat.R;

public class AdbrixUtil {
    private static final String TAG = AdbrixUtil.class.getSimpleName();

    /**
     * adbrix FTE 연동 함수
     * 참고
     * http://help.igaworks.com/hc/ko/3_3/Content/Article/adbrix_aos
     */
    public static void setFirstTimeExperience(Context context, int SIGN_UP_STEP) {
        if (!SharedPrefHelper.getInstance(context).hadExperiencedSignUpStep(SIGN_UP_STEP)) {
            Log.e(TAG,"step : " + SIGN_UP_STEP);
            SharedPrefHelper.getInstance(context).setSharedPreferences(SharedPrefHelper.SIGN_UP_STEP, SIGN_UP_STEP);
            String message = "";
            String type = SharedPrefHelper.getInstance(context).getSharedPreferences(SharedPrefHelper.SNS_TYPE, null);
            switch (SIGN_UP_STEP) {
                case SharedPrefHelper.TUTORIAL:
                    message = context.getString(R.string.tutorial);
                    break;
                case SharedPrefHelper.LOGIN:
                    message = context.getString(R.string.login);
                    break;
                case SharedPrefHelper.SIGNUP:
                    if("FACEBOOK".equals(type))
                        message = context.getString(R.string.facebook_sign_up);
                    else if ("GOOGLE".equals(type))
                        message = context.getString(R.string.google_sign_up);
                    break;
                case SharedPrefHelper.PROFILE:
                    if("FACEBOOK".equals(type))
                        message = context.getString(R.string.facebook_profile_regist);
                    else if ("GOOGLE".equals(type))
                        message = context.getString(R.string.google_profile_regist);
                    break;
                case SharedPrefHelper.MAIN:
                    if("FACEBOOK".equals(type))
                        message = context.getString(R.string.facebook_main);
                    else if ("GOOGLE".equals(type))
                        message = context.getString(R.string.google_main);
                    break;
                default:
                    message = "error";
                    break;
            }
            Log.e(TAG, "fte message: " + message);
            IgawAdbrix.firstTimeExperience(message);
        } else {
            int step = SharedPrefHelper.getInstance(context).getSharedPreferences(SharedPrefHelper.SIGN_UP_STEP, -1);
            Log.e(TAG, "Registed: " + step);
        }
    }

}
