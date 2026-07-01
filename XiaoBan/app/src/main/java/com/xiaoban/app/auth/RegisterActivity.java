package com.xiaoban.app.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;

import com.xiaoban.app.R;
import com.xiaoban.app.base.BaseActivity;
import com.xiaoban.app.base.Constants;
import com.xiaoban.app.child.ChildHomeActivity;
import com.xiaoban.app.elder.ElderHomeActivity;
import com.xiaoban.app.model.User;
import com.xiaoban.app.network.ApiCallback;
import com.xiaoban.app.network.ApiClient;
import com.xiaoban.app.util.SharedPrefUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import cn.jpush.android.api.JPushInterface;

public class RegisterActivity extends BaseActivity {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    private EditText etPhone, etPassword, etNickname;
    private RadioGroup rgRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        etNickname = findViewById(R.id.et_nickname);
        rgRole = findViewById(R.id.rg_role);
        ImageView btnBack = findViewById(R.id.btn_back);
        Button btnRegister = findViewById(R.id.btn_register);

        btnBack.setOnClickListener(v -> finish());
        btnRegister.setOnClickListener(v -> doRegister());
    }

    private void doRegister() {
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String nickname = etNickname.getText().toString().trim();
        String role = rgRole.getCheckedRadioButtonId() == R.id.rb_elder ? "elder" : "child";

        if (phone.isEmpty() || password.isEmpty()) {
            showToast("请输入手机号和密码");
            return;
        }

        if (!PHONE_PATTERN.matcher(phone).matches()) {
            etPhone.requestFocus();
            etPhone.setError("请输入11位有效手机号");
            showToast("手机号必须为11位数字，且以1开头、第二位为3-9");
            return;
        }

        showToast("正在注册...");
        Map<String, String> body = new HashMap<>();
        body.put("phone", phone);
        body.put("password", password);
        body.put("role", role);
        body.put("nickname", nickname);

        ApiClient.getInstance(this).getApi().register(body).enqueue(new ApiCallback<User>() {
            @Override
            public void onSuccess(User user) {
                SharedPrefUtil.putString(RegisterActivity.this, Constants.SP_TOKEN, user.getToken());
                SharedPrefUtil.putLong(RegisterActivity.this, Constants.SP_USER_ID, user.getUserId());
                SharedPrefUtil.putString(RegisterActivity.this, Constants.SP_ROLE, user.getRole());
                SharedPrefUtil.putString(RegisterActivity.this, Constants.SP_NICKNAME, user.getNickname());
                SharedPrefUtil.putString(RegisterActivity.this, Constants.SP_PHONE, phone);

                JPushInterface.setAlias(RegisterActivity.this, 0, String.valueOf(user.getUserId()));

                Intent intent;
                if ("elder".equals(user.getRole())) {
                    intent = new Intent(RegisterActivity.this, ElderHomeActivity.class);
                } else {
                    intent = new Intent(RegisterActivity.this, ChildHomeActivity.class);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }
}
