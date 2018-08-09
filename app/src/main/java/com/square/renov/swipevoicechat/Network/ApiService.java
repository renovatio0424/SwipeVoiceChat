package com.square.renov.swipevoicechat.Network;

import android.support.annotation.Nullable;

import com.square.renov.swipevoicechat.Model.Filter;
import com.square.renov.swipevoicechat.Model.PointLog;
import com.square.renov.swipevoicechat.Model.Result;
import com.square.renov.swipevoicechat.Model.SystemCheck;
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

    @DELETE("user/withdraw")
    Call<Result> withdrawUser(@Query("reason") String reason);


    @GET("voice")
    Call<ArrayList<VoiceCard>> getRandomVoiceCard();

    @GET("voice/filtered")
    Call<ArrayList<VoiceCard>> getFilteredRandomVoiceCard();

    /**
     * ChatId가 있으면 챗방으로
     * 없으면 새이야기
     */
    @FormUrlEncoded
    @POST("voice")
    Call<VoiceCard> sendChatVoice(@Field("chatId") int chatId, @Field("url") String url, @Field("seconds") int seconds);

    @FormUrlEncoded
    @POST("voice")
    Call<VoiceCard> sendNewVoice(@Field("url") String url, @Field("seconds") int seconds);

    @GET("voice/chat/list")
    Call<ArrayList<VoiceChatRoom>> loadVoiceChatRoomList(@Query("limit") int limit, @Query("offset") int offset);

    @GET("voice/chat/{chatId}/list")
    Call<ArrayList<VoiceChat>> loadVoiceChatList(@Path("chatId") int chatId, @Query("limit") int limit, @Query("offset") int offset);

    /**
     * 첫 답장하기 (답장하면서 채팅방 생성)
     * @param voiceId 답장할 새이야기 id (VoiceCard)
     * @param VoiceUrl 음성 파일 경로*/
    @FormUrlEncoded
    @POST("voice/chat/start")
    Call<VoiceChatRoom> makeVoiceChatRoom(@Field("voiceId") int voiceId, @Field("url") String VoiceUrl, @Field("seconds") int seconds);

    @FormUrlEncoded
    @POST("voice/chat/leave")
    Call<VoiceChatRoom> leaveVoiceChatRoom(@Field("chatId") int chatId);
    /**
     * 보이스 넘기기 / 보이스 신고하기
     * @param id       신고 / 넘길 voiceCard id
     * @param passType Pass -> 보이스 넘기기 / Report -> 신고하기
     * @param reason   신고하기 -> 신고 사유
     */
    @FormUrlEncoded
    @POST("voice/pass")
    Call<VoiceCard> passVoice(@Field("id") int id, @Field("type") String passType, @Field("reason") @Nullable String reason);

    /**
     * @param type image / voice
     * @param size file size*/
    @GET("upload")
    Call<Map> getUploadMetaData(@Query("type") String type, @Query("size") int size);


    @GET("cash/log")
    Call<ArrayList<PointLog>> loadLunaLogList(@Query("limit") int limit, @Query("offset") int offset);

    @GET("voice/count")
    Call<Integer> getNewVoiceCount();

    @FormUrlEncoded
    @POST("cash/charge/android")
    Call<JsonObject> payInAppProduct(@Field("originalJson") String originalJson, @Field("signature") String signature);

    @GET("user/code")
    Call<String> getMyInviteCode();

    @GET("user/code/valid")
    Call<Result> checkInviteCode(@Query("code") String code);

    @GET("setting/android")
    Call<SystemCheck> checkSystem();
}
