package com.xiaoban.app.push;

import android.content.Context;

import cn.jpush.android.api.NotificationMessage;
import cn.jpush.android.service.JPushMessageReceiver;

/**
 * JPush 5.x message receiver — triggers elder TTS when notifications arrive.
 */
public class XiaoBanJPushReceiver extends JPushMessageReceiver {

    @Override
    public void onNotifyMessageArrived(Context context, NotificationMessage message) {
        if (message == null) {
            return;
        }
        PushNotificationHandler.handleNotificationArrived(
                context,
                message.notificationTitle,
                message.notificationContent,
                message.notificationExtras
        );
    }
}
