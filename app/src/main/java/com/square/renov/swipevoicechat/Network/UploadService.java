package com.square.renov.swipevoicechat.Network;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.HeaderMap;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface UploadService {

    @Multipart
    @PUT("{key}")
    Call<String> upload(@Path("key") String key,
                        @HeaderMap Map<String, String> headers,
                        @Part MultipartBody.Part file);
}
