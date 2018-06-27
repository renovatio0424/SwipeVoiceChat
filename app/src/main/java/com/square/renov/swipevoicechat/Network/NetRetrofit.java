package com.square.renov.swipevoicechat.Network;

import android.content.Context;
import android.util.Log;

import com.square.renov.swipevoicechat.Util.SharedPrefHelper;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetRetrofit {
    private static final String TAG = NetRetrofit.class.getSimpleName();
    private static NetRetrofit ourInstance;
    private static String DEVELOP_SERVER_DOMAIN = "http://13.125.253.85/api/";
    private Context context;

    private NetRetrofit(Context context) {
        this.context = context;
    }

    public static synchronized NetRetrofit getInstance(Context context) {
        if (ourInstance == null)
            ourInstance = new NetRetrofit(context);
        return ourInstance;
    }

    OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    String accessToken = SharedPrefHelper.getInstance(context).getSharedPreferences(SharedPrefHelper.ACCESS_TOKEN, "");

                    Request original = chain.request()
                            .newBuilder()
                            .addHeader("HelloVoiceAuth", accessToken)
                            .build();

                    Log.e(TAG, "request header: " + original.headers());
                    return chain.proceed(original);
                }
            })
            .build();

    Retrofit retrofit = new Retrofit.Builder()
            .client(client)
            .baseUrl(DEVELOP_SERVER_DOMAIN)
            .addConverterFactory(GsonConverterFactory.create()) // 파싱등록
            .build();

    ApiService service = retrofit.create(ApiService.class);

    public ApiService getService() {
        return service;
    }
}
