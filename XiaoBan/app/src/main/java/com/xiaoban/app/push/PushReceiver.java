package com.xiaoban.app.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import cn.jpush.android.api.JPushInterface;

/**
 * Legacy JPush broadcast receiver (fallback for custom messages).
 */
public class PushReceiver extends BroadcastReceiver {

    private static final String TAG = "PushReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        Bundle bundle = intent.getExtras();

        if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(action) && bundle != null) {
            String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
            String content = bundle.getString(JPushInterface.EXTRA_MESSAGE);
            PushNotificationHandler.handleNotificationArrived(context, null, content, extras);
        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(action) && bundle != null) {
            String title = bundle.getString(JPushInterface.EXTRA_NOTIFICATION_TITLE);
            String content = bundle.getString(JPushInterface.EXTRA_ALERT);
            String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
            PushNotificationHandler.handleNotificationArrived(context, title, content, extras);
        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(action) && bundle != null) {
            String title = bundle.getString(JPushInterface.EXTRA_NOTIFICATION_TITLE);
            String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
            PushNavigationHelper.openFromNotification(context, title, extras);
        }
    }
}
