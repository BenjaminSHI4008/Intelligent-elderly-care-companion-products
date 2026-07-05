package com.xiaoban.app.base;

import android.app.Application;

import cn.jpush.android.api.JPushInterface;
import com.iflytek.cloud.SpeechUtility;
import com.xiaoban.app.util.SharedPrefUtil;
import com.xiaoban.app.voice.VoiceManager;

public class BaseApplication extends Application {

    private static BaseApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // 讯飞SDK初始化 — 必须在Application的onCreate中执行
        SpeechUtility.createUtility(this, "appid=" + Constants.IFLYTEK_APP_ID);
        VoiceManager.getInstance().init(this);

        // 初始化极光推送
        JPushInterface.setDebugMode(true);
        JPushInterface.init(this);

        // 设置推送别名为用户ID
        long userId = SharedPrefUtil.getLong(this, Constants.SP_USER_ID, 0);
        if (userId > 0) {
            JPushInterface.setAlias(this, 0, String.valueOf(userId));
        }
    }

    public static BaseApplication getInstance() {
        return instance;
    }
}
