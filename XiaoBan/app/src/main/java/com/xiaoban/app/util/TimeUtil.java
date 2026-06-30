package com.xiaoban.app.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeUtil {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.CHINA);
    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

    public static String today() {
        return DATE_FORMAT.format(new Date());
    }

    public static String formatTime(Date date) {
        return TIME_FORMAT.format(date);
    }

    public static String formatDateTime(Date date) {
        return DATETIME_FORMAT.format(date);
    }

    public static String getTodayDate() {
        Calendar calendar = Calendar.getInstance();
        String[] weekDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return month + "月" + day + "日 " + weekDays[dayOfWeek - 1];
    }

    public static String formatMessageTime(Date date) {
        if (date == null) return "";

        Calendar now = Calendar.getInstance();
        Calendar msgTime = Calendar.getInstance();
        msgTime.setTime(date);

        if (isSameDay(now, msgTime)) {
            int hour = msgTime.get(Calendar.HOUR_OF_DAY);
            String period = hour < 12 ? "上午" : "下午";
            return "今天" + period + " " + TIME_FORMAT.format(date);
        } else if (isYesterday(now, msgTime)) {
            int hour = msgTime.get(Calendar.HOUR_OF_DAY);
            String period = hour < 12 ? "上午" : "下午";
            return "昨天" + period + " " + TIME_FORMAT.format(date);
        } else {
            return DATE_FORMAT.format(date) + " " + TIME_FORMAT.format(date);
        }
    }

    private static boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private static boolean isYesterday(Calendar today, Calendar date) {
        Calendar yesterday = (Calendar) today.clone();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        return isSameDay(yesterday, date);
    }
}
