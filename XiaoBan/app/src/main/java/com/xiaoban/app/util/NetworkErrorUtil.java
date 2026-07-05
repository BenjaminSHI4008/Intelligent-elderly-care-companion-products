package com.xiaoban.app.util;

import com.xiaoban.app.BuildConfig;
import com.xiaoban.app.base.Constants;

/**
 * Maps low-level network failures to user-facing hints.
 * Debug builds include BASE_URL to speed up local troubleshooting.
 */
public final class NetworkErrorUtil {

    private NetworkErrorUtil() {
    }

    public static String getChatFailureMessage() {
        if (BuildConfig.DEBUG) {
            return "无法连接服务器 " + Constants.BASE_URL + "，请确认后端已启动且 IP 正确";
        }
        return "对话失败，请检查网络";
    }

    public static String getGenericFailureMessage() {
        if (BuildConfig.DEBUG) {
            return "网络异常，无法连接 " + Constants.BASE_URL;
        }
        return "网络异常，请检查网络连接";
    }
}
