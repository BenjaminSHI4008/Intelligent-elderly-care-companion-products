package com.xiaoban.app.util;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.xiaoban.app.auth.LoginActivity;

/**
 * Clears stale local session when backend rejects the JWT.
 */
public final class SessionHelper {

    private SessionHelper() {
    }

    public static boolean isUnauthorized(int code) {
        return code == 401 || code == 4010;
    }

    public static void forceRelogin(Context context, String message) {
        SharedPrefUtil.clear(context);
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        Toast.makeText(context,
                message == null || message.isEmpty() ? "登录已失效，请重新登录" : message,
                Toast.LENGTH_LONG).show();
    }
}
