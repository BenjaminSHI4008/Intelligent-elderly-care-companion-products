package com.xiaoban.app.network;

import android.widget.Toast;

import com.xiaoban.app.base.BaseApplication;
import com.xiaoban.app.model.ApiResponse;

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
            onBusinessError(response.code(), "请求失败");
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
}
