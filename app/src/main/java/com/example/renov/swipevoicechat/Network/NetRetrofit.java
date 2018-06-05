package com.example.renov.swipevoicechat.Network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetRetrofit {
    private static NetRetrofit ourInstance = new NetRetrofit();
    private static String SERVER_DOMAIN = "https://api.github.com/";

    public static NetRetrofit getInstance() {
        return ourInstance;
    }
    private NetRetrofit() {
    }

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(SERVER_DOMAIN)
            .addConverterFactory(GsonConverterFactory.create()) // 파싱등록
            .build();

    RetrofitService service = retrofit.create(RetrofitService.class);

    public RetrofitService getService() {
        return service;
    }
}
