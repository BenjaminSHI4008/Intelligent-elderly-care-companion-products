package com.xiaoban.app.network;

import android.widget.Toast;

import com.google.gson.Gson;
import com.xiaoban.app.base.BaseApplication;
import com.xiaoban.app.model.ApiResponse;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class ApiCallback<T> implements Callback<ApiResponse<T>> {

    @Override
    public void onResponse(Call<ApiResponse<T>> call, Response<ApiResponse<T>> response) {
        if (response.isSuccessful() && response.body() != null) {
            ApiResponse<T> body = response.body();
            if (body.isSuccess()) {
                onSuccess(body.getData());
            } else {
                onBusinessError(body.getCode(), body.getMessage());
            }
        } else {
            ApiResponse<?> errorBody = parseErrorBody(response);
            if (errorBody != null) {
                onBusinessError(errorBody.getCode(), errorBody.getMessage());
            } else {
                onBusinessError(response.code(), "请求失败");
            }
        }
    }

    @Override
    public void onFailure(Call<ApiResponse<T>> call, Throwable t) {
        onNetworkError(t.getMessage());
    }

    public abstract void onSuccess(T data);

    public void onBusinessError(int code, String message) {
        Toast.makeText(BaseApplication.getInstance(), message, Toast.LENGTH_SHORT).show();
    }

    public void onNetworkError(String message) {
        Toast.makeText(BaseApplication.getInstance(), "网络异常，请检查网络连接", Toast.LENGTH_SHORT).show();
    }

    private ApiResponse<?> parseErrorBody(Response<ApiResponse<T>> response) {
        if (response.errorBody() == null) {
            return null;
        }

        try {
            return new Gson().fromJson(response.errorBody().string(), ApiResponse.class);
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }
}
