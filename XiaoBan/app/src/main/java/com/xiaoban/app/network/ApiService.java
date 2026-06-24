package com.xiaoban.app.network;

import com.xiaoban.app.model.ApiResponse;
import com.xiaoban.app.model.ChatResponse;
import com.xiaoban.app.model.Conversation;
import com.xiaoban.app.model.DailyReport;
import com.xiaoban.app.model.Message;
import com.xiaoban.app.model.Reminder;
import com.xiaoban.app.model.User;
import com.xiaoban.app.model.WeatherInfo;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    // === 认证 ===
    @POST("api/auth/register")
    Call<ApiResponse<User>> register(@Body Map<String, String> body);

    @POST("api/auth/login")
    Call<ApiResponse<User>> login(@Body Map<String, String> body);

    @GET("api/auth/profile")
    Call<ApiResponse<User>> getProfile();

    @PUT("api/auth/profile")
    Call<ApiResponse<User>> updateProfile(@Body Map<String, String> body);

    // === 对话 ===
    @POST("api/voice/chat")
    Call<ApiResponse<ChatResponse>> chat(@Body Map<String, String> body);

    // === 子女端对话管理 ===
    @GET("api/conversations/pending")
    Call<ApiResponse<List<Conversation>>> getPending();

    @GET("api/conversations/history")
    Call<ApiResponse<List<Conversation>>> getHistory(@Query("elderId") long elderId, @Query("date") String date);

    @POST("api/conversations/confirm")
    Call<ApiResponse<Void>> confirm(@Body Map<String, Object> body);

    @POST("api/conversations/correct")
    Call<ApiResponse<Void>> correct(@Body Map<String, Object> body);

    // === 家庭消息 ===
    @GET("api/messages/received")
    Call<ApiResponse<List<Message>>> getMessages(@Query("page") int page, @Query("size") int size);

    @POST("api/messages/send")
    Call<ApiResponse<Void>> sendMessage(@Body Map<String, Object> body);

    @PUT("api/messages/{id}/read")
    Call<ApiResponse<Void>> markRead(@Path("id") long id);

    // === 提醒 ===
    @POST("api/reminder/create")
    Call<ApiResponse<Void>> createReminder(@Body Map<String, Object> body);

    @GET("api/reminder/list")
    Call<ApiResponse<List<Reminder>>> getReminders(@Query("elderId") long elderId);

    @GET("api/reminder/today")
    Call<ApiResponse<List<Reminder>>> getTodayReminders();

    @PUT("api/reminder/{id}/toggle")
    Call<ApiResponse<Void>> toggleReminder(@Path("id") long id);

    @DELETE("api/reminder/{id}")
    Call<ApiResponse<Void>> deleteReminder(@Path("id") long id);

    // === 绑定 ===
    @POST("api/bind/generate-code")
    Call<ApiResponse<Map<String, String>>> generateCode();

    @POST("api/bind/verify-code")
    Call<ApiResponse<Map<String, Object>>> verifyCode(@Body Map<String, String> body);

    @POST("api/bind/bluetooth")
    Call<ApiResponse<Void>> bindBluetooth(@Body Map<String, Long> body);

    @GET("api/bind/relations")
    Call<ApiResponse<List<Map<String, Object>>>> getBindRelations();

    @DELETE("api/bind/{relationId}")
    Call<ApiResponse<Void>> unbind(@Path("relationId") long relationId);

    // === 日报 ===
    @GET("api/report/daily")
    Call<ApiResponse<List<DailyReport>>> getDailyReport(@Query("elderId") long elderId, @Query("date") String date);

    // === 天气 ===
    @GET("api/weather/current")
    Call<ApiResponse<WeatherInfo>> getCurrentWeather(@Query("city") String city);
}
