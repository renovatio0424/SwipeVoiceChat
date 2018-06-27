package com.square.renov.swipevoicechat.Network;

import android.content.Context;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FileUploadRetrofit {
    private static final String TAG = NetRetrofit.class.getSimpleName();
    private static FileUploadRetrofit ourInstance;
    private static String FILE_UPLOAD_SERVER_DOMAIN = "https://hellovoicebucket.s3.amazonaws.com/";
    private Context context;

    private FileUploadRetrofit(Context context) {
        this.context = context;
    }

    public static synchronized FileUploadRetrofit getInstance(Context context) {
        if (ourInstance == null)
            ourInstance = new FileUploadRetrofit(context);
        return ourInstance;
    }

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(FILE_UPLOAD_SERVER_DOMAIN)
            .addConverterFactory(GsonConverterFactory.create()) // 파싱등록
            .build();

    UploadService service = retrofit.create(UploadService.class);

    public UploadService getService() {
        return service;
    }
}
