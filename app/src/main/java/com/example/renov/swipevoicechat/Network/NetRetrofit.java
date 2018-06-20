package com.example.renov.swipevoicechat.Network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetRetrofit {
    private static NetRetrofit ourInstance;
    private static String DEVELOP_SERVER_DOMAIN = "http://13.125.253.85/api/";

    private NetRetrofit() {

    }

    public static synchronized NetRetrofit getInstance() {
        if(ourInstance == null)
            ourInstance = new NetRetrofit();
        return ourInstance;
    }

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(DEVELOP_SERVER_DOMAIN)
            .addConverterFactory(GsonConverterFactory.create()) // 파싱등록
            .build();

    RetrofitService service = retrofit.create(RetrofitService.class);

    public RetrofitService getService() {
        return service;
    }
}
