package com.xiaoban.app.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import cn.jpush.android.api.JPushInterface;
import com.xiaoban.app.base.Constants;
import com.xiaoban.app.voice.VoiceManager;

import org.json.JSONObject;

public class PushReceiver extends BroadcastReceiver {

    private static final String TAG = "PushReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Bundle bundle = intent.getExtras();

        if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(action)) {
            String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
            handlePush(context, extras);
        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(action)) {
            Log.i(TAG, "收到通知");
        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(action)) {
            String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
            handleNotificationClick(context, extras);
        }
    }

    private void handlePush(Context context, String extras) {
        try {
            JSONObject json = new JSONObject(extras);
            String type = json.optString("type", "");

            switch (type) {
                case Constants.PUSH_TYPE_CORRECTION:
                    String correction = json.optString("content", "");
                    VoiceManager.getInstance().getSynthesizer().speak(
                            "您女儿说，" + correction, null);
                    break;
                case Constants.PUSH_TYPE_FAMILY_MSG:
                    VoiceManager.getInstance().getSynthesizer().speak(
                            "您的家人给您发了一条消息", null);
                    break;
                case Constants.PUSH_TYPE_REMINDER:
                    String content = json.optString("content", "");
                    VoiceManager.getInstance().getSynthesizer().speak(
                            "小伴提醒您，该" + content + "了", null);
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "处理推送失败", e);
        }
    }

    private void handleNotificationClick(Context context, String extras) {
        try {
            JSONObject json = new JSONObject(extras);
            String type = json.optString("type", "");

            // 点击通知跳转对应页面（具体实现在Phase 4/5完善）
            Log.i(TAG, "通知点击: type=" + type);
        } catch (Exception e) {
            Log.e(TAG, "处理通知点击失败", e);
        }
    }
}
