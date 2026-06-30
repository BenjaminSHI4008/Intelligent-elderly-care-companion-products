package com.xiaoban.app.base;

import com.xiaoban.app.BuildConfig;

public class Constants {

    // 后端 API 地址在 app/build.gradle 的 buildConfigField 中配置
    // 模拟器：http://10.0.2.2:8080/  |  真机：改为电脑 WLAN IP
    public static final String BASE_URL = BuildConfig.BASE_URL;

    // SharedPreferences keys
    public static final String SP_NAME = "xiaoban_sp";
    public static final String SP_TOKEN = "token";
    public static final String SP_USER_ID = "user_id";
    public static final String SP_ROLE = "role";
    public static final String SP_NICKNAME = "nickname";
    public static final String SP_PHONE = "phone";
    public static final String SP_GENDER = "gender";
    public static final String SP_BIRTHDAY = "birthday";
    public static final String SP_EMERGENCY_CONTACT = "emergency_contact";

# 外部 SDK 配置对照项目根目录 config.yaml -> client-sdk-reference
    // 科大讯飞 AppID — 见 config.yaml client-sdk-reference.iflytek-voice
    public static final String IFLYTEK_APP_ID = "0a9e1284";

    // 极光推送 AppKey — 须与 config.yaml external-services.jpush.app-key 一致
    public static final String JPUSH_APP_KEY = "8bc0c3218b934e9a4f38b32a";

    // 高德 Android 定位 Key — 见 config.yaml client-sdk-reference.amap-location
    public static final String AMAP_ANDROID_KEY = "e03e26e0ba4e73a79bb0a8cedf723405";

    // 推送类型
    public static final String PUSH_TYPE_HEALTH = "health_alert";
    public static final String PUSH_TYPE_URGENT = "urgent_alert";
    public static final String PUSH_TYPE_REPORT = "daily_report";
    public static final String PUSH_TYPE_CORRECTION = "correction";
    public static final String PUSH_TYPE_FAMILY_MSG = "family_msg";
    public static final String PUSH_TYPE_REMINDER = "reminder";
    public static final String PUSH_TYPE_BIND_SUCCESS = "bind_success";
}
