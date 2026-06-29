package com.xiaoban.app.push;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.xiaoban.app.base.Constants;
import com.xiaoban.app.util.BindNotificationHelper;
import com.xiaoban.app.voice.VoiceManager;

import org.json.JSONObject;

/**
 * Centralized push notification handling for elder-side TTS and future deep links.
 */
public final class PushNotificationHandler {

    private static final String TAG = "PushNotificationHandler";

    private PushNotificationHandler() {}

    public static void handleNotificationArrived(Context context, String title, String content, String extrasJson) {
        String type = extractType(extrasJson);

        if (Constants.PUSH_TYPE_BIND_SUCCESS.equals(type)
                || isBindSuccessNotification(title, content)) {
            String speech = TextUtils.isEmpty(content) ? "您的家人已成功与您绑定" : content;
            VoiceManager.getInstance().getSynthesizer().speak(speech, null);
            return;
        }

        if (TextUtils.isEmpty(extrasJson)) {
            return;
        }

        try {
            JSONObject json = new JSONObject(extrasJson);
            if (TextUtils.isEmpty(type)) {
                type = json.optString("type", "");
            }
            dispatchByType(type, json);
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse push extras", e);
        }
    }

    private static void dispatchByType(String type, JSONObject json) {
        switch (type) {
            case Constants.PUSH_TYPE_CORRECTION:
                String correction = json.optString("content", "");
                VoiceManager.getInstance().getSynthesizer().speak(
                        "您家人说，" + correction, null);
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
            default:
                break;
        }
    }

    private static String extractType(String extrasJson) {
        if (TextUtils.isEmpty(extrasJson)) {
            return "";
        }
        try {
            return new JSONObject(extrasJson).optString("type", "");
        } catch (Exception e) {
            return "";
        }
    }

    private static boolean isBindSuccessNotification(String title, String content) {
        return title != null && title.contains("绑定成功");
    }
}
