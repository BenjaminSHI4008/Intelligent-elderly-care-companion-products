package com.xiaoban.app.util;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class ImageUploadHelper {

    private ImageUploadHelper() {}

    public static File copyToCache(Context context, Uri uri) throws IOException {
        File output = new File(context.getCacheDir(), "upload_" + System.currentTimeMillis() + ".jpg");
        try (InputStream input = context.getContentResolver().openInputStream(uri);
             FileOutputStream out = new FileOutputStream(output)) {
            if (input == null) {
                throw new IOException("无法读取图片");
            }
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }
        return output;
    }
}
