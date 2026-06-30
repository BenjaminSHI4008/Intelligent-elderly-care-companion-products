package com.xiaoban.app.base;

import android.os.Build;

import com.xiaoban.app.BuildConfig;

public class Constants {

    // 后端 API 地址：debug/release 在 app/build.gradle 的 buildConfigField 中配置
    // 模拟器访问宿主机 localhost 请用 http://10.0.2.2:8080/
    // 真机请用电脑 WLAN IP，例如 http://192.168.43.31:8080/
    public static final String BASE_URL = isEmulator()
            ? "http://10.0.2.2:8080/"
            : BuildConfig.BASE_URL;

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

    // 科大讯飞 AppID（讯飞开放平台：https://www.xfyun.cn/）
    public static final String IFLYTEK_APP_ID = "0a9e1284";

    // 极光推送 AppKey（极光控制台：https://www.jiguang.cn/）
    public static final String JPUSH_APP_KEY = "8bc0c3218b934e9a4f38b32a";

    // 高德 Android 定位 SDK Key（与 AndroidManifest 中 com.amap.api.v2.apikey 一致）
    public static final String AMAP_ANDROID_KEY = "e03e26e0ba4e73a79bb0a8cedf723405";

    // 推送类型
    public static final String PUSH_TYPE_HEALTH = "health_alert";
    public static final String PUSH_TYPE_URGENT = "urgent_alert";
    public static final String PUSH_TYPE_REPORT = "daily_report";
    public static final String PUSH_TYPE_CORRECTION = "correction";
    public static final String PUSH_TYPE_FAMILY_MSG = "family_msg";
    public static final String PUSH_TYPE_REMINDER = "reminder";
    public static final String PUSH_TYPE_BIND_SUCCESS = "bind_success";

    private static boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.contains("emulator")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"));
    }
}
