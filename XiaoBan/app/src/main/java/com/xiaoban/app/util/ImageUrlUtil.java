package com.xiaoban.app.util;

import com.xiaoban.app.base.Constants;

public final class ImageUrlUtil {

    private ImageUrlUtil() {}

    public static String resolve(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path;
        }
        String base = Constants.BASE_URL;
        if (base.endsWith("/") && path.startsWith("/")) {
            return base.substring(0, base.length() - 1) + path;
        }
        if (!base.endsWith("/") && !path.startsWith("/")) {
            return base + "/" + path;
        }
        return base + path;
    }
}
