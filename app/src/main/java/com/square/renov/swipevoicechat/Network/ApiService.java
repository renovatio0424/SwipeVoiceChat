package com.square.renov.swipevoicechat.Network;

import android.support.annotation.Nullable;

import com.square.renov.swipevoicechat.Model.Filter;
import com.square.renov.swipevoicechat.Model.Result;
import com.square.renov.swipevoicechat.Model.User;
import com.square.renov.swipevoicechat.Model.VoiceCard;
import com.square.renov.swipevoicechat.Model.VoiceChat;
import com.square.renov.swipevoicechat.Model.VoiceChatRoom;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @GET("users/{user}/repos")
    Call<ArrayList<JsonObject>> getListRepos(@Path("user") String id);

    @FormUrlEncoded
    @POST("user/register")
    Call<User> register(@Field("token") String token,
                        @Field("type") String type,
                        @Field("name") String name,
                        @Field("gender") String gender,
                        @Field("birth") String birth,
                        @Field("lat") String lat,
                        @Field("lng") String lng);

    @FormUrlEncoded
    @POST("user/login")
    Call<User> login(@Field("token") String token, @Field("type") String SnsType);

    @GET("user/logout")
    Call<Result> logout();

    @GET("user/filter")
    Call<Filter> checkFilter();

    @Headers("Content-Type: application/json")
    @POST("user/filter")
    Call<Filter> updateFilter(@Body Filter filter);

    @GET("user")
    Call<User> checkCurrentUserInfo();

    @Headers("Content-Type: application/json")
    @PUT("user")
    Call<User> updateUserInfo(@Body User updateUserInfo);






    @GET("voice")
    Call<List<VoiceCard>> getRandomVoiceCard();

    /**
     * ChatId가 있으면 챗방으로
     * 없으면 새이야기
     * */
    @FormUrlEncoded
    @POST("voice")
    Call<VoiceCard> sendChatVoice(@Field("chatId") int chatId, @Field("url") String url);

    @FormUrlEncoded
    @POST("voice")
    Call<VoiceCard> sendNewVoice(@Field("url") String url);

    @GET("voice/chat/list")
    Call<ArrayList<VoiceChatRoom>> loadVoiceChatRoomList();

    @GET("voice/chat/{chatId}/list")
    Call<ArrayList<VoiceChat>> loadVoiceChatList(@Path("chatId") String chatId);

    @FormUrlEncoded
    @POST("voice/chat/start")
    Call<VoiceChatRoom> makeVoiceChatRoom(@Field("voiceId") int voiceId);

    @FormUrlEncoded
    @POST("voice/pass")
    Call<VoiceCard> passVoice(@Field("id") int id, @Field("type") String passType, @Field("reason") @Nullable String reason);

    @GET("upload")
    Call<Map> getUploadMetaData(@Query("type") String type, @Query("size") int size);


}
