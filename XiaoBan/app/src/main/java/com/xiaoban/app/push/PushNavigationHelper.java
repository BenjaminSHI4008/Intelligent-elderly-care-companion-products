package com.xiaoban.app.push;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.xiaoban.app.auth.LoginActivity;
import com.xiaoban.app.base.Constants;
import com.xiaoban.app.elder.ElderMessageActivity;
import com.xiaoban.app.util.SharedPrefUtil;

import org.json.JSONObject;

/**
 * Opens the appropriate screen when the user taps a push notification.
 */
public final class PushNavigationHelper {

    private PushNavigationHelper() {}

    public static void openFromNotification(Context context, String title, String extrasJson) {
        if (context == null) {
            return;
        }

        String token = SharedPrefUtil.getString(context, Constants.SP_TOKEN, "");
        if (TextUtils.isEmpty(token)) {
            Intent loginIntent = new Intent(context, LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(loginIntent);
            return;
        }

        String type = extractType(extrasJson);
        if (Constants.PUSH_TYPE_FAMILY_MSG.equals(type) || isFamilyMessageNotification(title)) {
            openFamilyMessage(context);
        }
    }

    private static void openFamilyMessage(Context context) {
        String role = SharedPrefUtil.getString(context, Constants.SP_ROLE, "");
        Intent intent;
        if ("child".equals(role)) {
            intent = new Intent(context, com.xiaoban.app.child.ChildHomeActivity.class);
        } else {
            intent = new Intent(context, ElderMessageActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
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

    private static boolean isFamilyMessageNotification(String title) {
        return "小伴消息".equals(title);
    }
}
