package com.xiaoban.app.network;

import android.content.Context;

import androidx.annotation.NonNull;

import com.xiaoban.app.base.Constants;
import com.xiaoban.app.util.SharedPrefUtil;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class TokenInterceptor implements Interceptor {

    private final Context context;

    public TokenInterceptor(Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request original = chain.request();

        String token = SharedPrefUtil.getString(context, Constants.SP_TOKEN, "");
        if (token.isEmpty()) {
            return chain.proceed(original);
        }

        Request request = original.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();
        return chain.proceed(request);
    }
}
