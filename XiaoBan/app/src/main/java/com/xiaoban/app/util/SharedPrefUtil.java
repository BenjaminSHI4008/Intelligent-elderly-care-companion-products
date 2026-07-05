package com.xiaoban.app.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.xiaoban.app.base.Constants;

public class SharedPrefUtil {

    private static SharedPreferences getSP(Context context) {
        return context.getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE);
    }

    public static void putString(Context context, String key, String value) {
        getSP(context).edit().putString(key, value).apply();
    }

    public static String getString(Context context, String key, String defValue) {
        return getSP(context).getString(key, defValue);
    }

    public static void putLong(Context context, String key, long value) {
        getSP(context).edit().putLong(key, value).apply();
    }

    public static long getLong(Context context, String key, long defValue) {
        return getSP(context).getLong(key, defValue);
    }

    public static void clear(Context context) {
        getSP(context).edit().clear().apply();
    }
}
