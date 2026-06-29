package com.xiaoban.app.base;

public class Constants {

    // 后端API地址
    // Android 模拟器使用 10.0.2.2 访问宿主机(电脑)的 localhost
    // 真机调试时改为电脑的局域网 IP，例如 http://192.168.1.100:8080/
    public static final String BASE_URL = "http://10.0.2.2:8080/";

    // SharedPreferences keys
    public static final String SP_NAME = "xiaoban_sp";
    public static final String SP_TOKEN = "token";
    public static final String SP_USER_ID = "user_id";
    public static final String SP_ROLE = "role";
    public static final String SP_NICKNAME = "nickname";
    public static final String SP_PHONE = "phone";

    // 科大讯飞 AppID（请在讯飞开放平台申请：https://www.xfyun.cn/）
    public static final String IFLYTEK_APP_ID = "your_iflytek_appid";

    // 极光推送（请在极光推送控制台申请：https://www.jiguang.cn/）
    public static final String JPUSH_APP_KEY = "your_jpush_appkey";

    // 推送类型
    public static final String PUSH_TYPE_HEALTH = "health_alert";
    public static final String PUSH_TYPE_URGENT = "urgent_alert";
    public static final String PUSH_TYPE_REPORT = "daily_report";
    public static final String PUSH_TYPE_CORRECTION = "correction";
    public static final String PUSH_TYPE_FAMILY_MSG = "family_msg";
    public static final String PUSH_TYPE_REMINDER = "reminder";
    public static final String PUSH_TYPE_BIND_SUCCESS = "bind_success";
}
