package com.xiaoban.app.base;

public class Constants {

    // 后端API地址（请替换为实际服务器地址）
    public static final String BASE_URL = "http://192.168.43.31:8080/";

    // SharedPreferences keys
    public static final String SP_NAME = "xiaoban_sp";
    public static final String SP_TOKEN = "token";
    public static final String SP_USER_ID = "user_id";
    public static final String SP_ROLE = "role";
    public static final String SP_NICKNAME = "nickname";
    public static final String SP_PHONE = "phone";
    public static final String SP_GENDER = "gender";
    public static final String SP_BIRTHDAY = "birthday";
    public static final String SP_CHILD_PHONE = "child_phone";
    public static final String SP_EMERGENCY_CONTACT = "emergency_contact";

    // 科大讯飞 AppID（请在讯飞开放平台申请：https://www.xfyun.cn/）
    public static final String IFLYTEK_APP_ID = "0a9e1284";

    // 极光推送（请在极光推送控制台申请：https://www.jiguang.cn/）
    public static final String JPUSH_APP_KEY = "8bc0c3218b934e9a4f38b32a";

    // 推送类型
    public static final String PUSH_TYPE_HEALTH = "health_alert";
    public static final String PUSH_TYPE_URGENT = "urgent_alert";
    public static final String PUSH_TYPE_REPORT = "daily_report";
    public static final String PUSH_TYPE_CORRECTION = "correction";
    public static final String PUSH_TYPE_FAMILY_MSG = "family_msg";
    public static final String PUSH_TYPE_REMINDER = "reminder";
}
