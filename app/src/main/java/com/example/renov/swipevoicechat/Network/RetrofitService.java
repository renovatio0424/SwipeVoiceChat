package com.example.renov.swipevoicechat.Network;

import android.support.annotation.Nullable;

import com.example.renov.swipevoicechat.Model.Filter;
import com.example.renov.swipevoicechat.Model.User;
import com.example.renov.swipevoicechat.Model.VoiceCard;
import com.example.renov.swipevoicechat.Model.VoiceChat;
import com.example.renov.swipevoicechat.Model.VoiceChatRoom;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface RetrofitService {
    @GET("users/{user}/repos")
    Call<ArrayList<JsonObject>> getListRepos(@Path("user") String id);

    @FormUrlEncoded
    @POST("/user/register")
    Call<User> register(@Field("token") String token,
                        @Field("type") String type,
                        @Field("gender") String gender,
                        @Field("birth") int birth);

    @FormUrlEncoded
    @POST("/user/login")
    Call<User> login(@Field("token") String token, @Field("type") String SnsType);

    @GET("/user/logout")
    void logout();

    @GET("/user/filter")
    Call<Filter> checkFilter();

    @POST("/user/filter")
    void updateFilter(@Body Filter filter);

    @GET("/user")
    Call<User> checkCurrentUserInfo();

    @PUT("/user")
    Call<User> updateUserInfo(@Field("update") User updateUserInfo);





    @GET("/voice")
    Call<VoiceCard> getRandomVoiceCard();

    @FormUrlEncoded
    @POST("/voice")
    Call<VoiceCard> sendVoice(@Field("chatId") int chatId, @Field("url") String url);

    @GET("/voice/chat/list")
    Call<VoiceChatRoom> loadVoiceChatRoomList();

    @GET("/voice/chat/{chatId}/list")
    Call<ArrayList<VoiceChat>> loadVoiceChatList(@Path("chatId") String chatId);

    @FormUrlEncoded
    @POST("/voice/chat/start")
    Call<ArrayList<VoiceChatRoom>> startVoiceChat(@Field("voiceId") int voiceId);

    @FormUrlEncoded
    @POST("/voice/pass")
    Call<VoiceCard> passVoice(@Field("id") int id, @Field("type") String passType, @Field("reason") @Nullable String reason);


//    @GET("/upload")
}
