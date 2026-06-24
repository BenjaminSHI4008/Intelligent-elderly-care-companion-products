package com.xiaoban.app.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.xiaoban.app.R;
import com.xiaoban.app.base.BaseActivity;
import com.xiaoban.app.base.Constants;
import com.xiaoban.app.child.ChildHomeActivity;
import com.xiaoban.app.elder.ElderHomeActivity;
import com.xiaoban.app.model.ApiResponse;
import com.xiaoban.app.model.User;
import com.xiaoban.app.network.ApiCallback;
import com.xiaoban.app.network.ApiClient;
import com.xiaoban.app.util.SharedPrefUtil;

import java.util.HashMap;
import java.util.Map;

import cn.jpush.android.api.JPushInterface;

public class LoginActivity extends BaseActivity {

    private EditText etPhone, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        Button btnLogin = findViewById(R.id.btn_login);
        TextView tvRegister = findViewById(R.id.tv_register);

        btnLogin.setOnClickListener(v -> doLogin());
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void doLogin() {
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (phone.isEmpty() || password.isEmpty()) {
            showToast("请输入手机号和密码");
            return;
        }

        Map<String, String> body = new HashMap<>();
        body.put("phone", phone);
        body.put("password", password);

        ApiClient.getInstance(this).getApi().login(body).enqueue(new ApiCallback<User>() {
            @Override
            public void onSuccess(User user) {
                SharedPrefUtil.putString(LoginActivity.this, Constants.SP_TOKEN, user.getToken());
                SharedPrefUtil.putLong(LoginActivity.this, Constants.SP_USER_ID, user.getUserId());
                SharedPrefUtil.putString(LoginActivity.this, Constants.SP_ROLE, user.getRole());
                SharedPrefUtil.putString(LoginActivity.this, Constants.SP_NICKNAME, user.getNickname());
                SharedPrefUtil.putString(LoginActivity.this, Constants.SP_PHONE, phone);

                JPushInterface.setAlias(LoginActivity.this, 0, String.valueOf(user.getUserId()));

                Intent intent;
                if ("elder".equals(user.getRole())) {
                    intent = new Intent(LoginActivity.this, ElderHomeActivity.class);
                } else {
                    intent = new Intent(LoginActivity.this, ChildHomeActivity.class);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }
}
