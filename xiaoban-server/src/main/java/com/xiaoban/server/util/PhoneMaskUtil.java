package com.xiaoban.server.util;

public final class PhoneMaskUtil {

    private PhoneMaskUtil() {}

    public static String mask(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone != null ? phone : "";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
