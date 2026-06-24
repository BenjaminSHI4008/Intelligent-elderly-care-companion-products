package com.xiaoban.app.elder;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.xiaoban.app.R;
import com.xiaoban.app.auth.LoginActivity;
import com.xiaoban.app.base.BaseActivity;
import com.xiaoban.app.base.Constants;
import com.xiaoban.app.model.User;
import com.xiaoban.app.network.ApiCallback;
import com.xiaoban.app.network.ApiClient;
import com.xiaoban.app.util.SharedPrefUtil;

import java.util.HashMap;
import java.util.Map;

public class ElderProfileActivity extends BaseActivity {

    private TextView tvAvatar;
    private TextView tvNickname;
    private TextView tvRole;
    private TextView tvPhone;
    private TextView tvGender;
    private TextView tvBirthday;
    private TextView tvUserId;
    private TextView tvEditNicknameHint;
    private TextView tvEditAccount;
    private View btnBack;
    private View btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elder_profile);

        initViews();
        setupListeners();
        showCachedProfile();
        loadProfile();
    }

    private void initViews() {
        tvAvatar = findViewById(R.id.tv_avatar);
        tvNickname = findViewById(R.id.tv_nickname);
        tvRole = findViewById(R.id.tv_role);
        tvPhone = findViewById(R.id.tv_phone);
        tvGender = findViewById(R.id.tv_gender);
        tvBirthday = findViewById(R.id.tv_birthday);
        tvUserId = findViewById(R.id.tv_user_id);
        tvEditNicknameHint = findViewById(R.id.tv_edit_nickname_hint);
        tvEditAccount = findViewById(R.id.tv_edit_account);
        btnBack = findViewById(R.id.btn_back);
        btnLogout = findViewById(R.id.btn_logout);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnLogout.setOnClickListener(v -> logout());
        tvNickname.setOnClickListener(v -> showNicknameDialog());
        tvEditNicknameHint.setOnClickListener(v -> showNicknameDialog());
        tvEditAccount.setOnClickListener(v -> {
            startActivity(new Intent(this, ElderProfileEditActivity.class));
        });
    }

    private void showCachedProfile() {
        String nickname = SharedPrefUtil.getString(this, Constants.SP_NICKNAME, "");
        String phone = SharedPrefUtil.getString(this, Constants.SP_PHONE, "");
        String role = SharedPrefUtil.getString(this, Constants.SP_ROLE, "");
        long userId = SharedPrefUtil.getLong(this, Constants.SP_USER_ID, 0);

        updateProfile(nickname, phone, role, userId);
        updateAccountInfo();
    }

    private void loadProfile() {
        ApiClient.getInstance(this).getApi().getProfile().enqueue(new ApiCallback<User>() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    SharedPrefUtil.putString(ElderProfileActivity.this, Constants.SP_PHONE, user.getPhone());
                    SharedPrefUtil.putString(ElderProfileActivity.this, Constants.SP_NICKNAME, user.getNickname());
                    SharedPrefUtil.putString(ElderProfileActivity.this, Constants.SP_GENDER, user.getGender());
                    SharedPrefUtil.putString(ElderProfileActivity.this, Constants.SP_BIRTHDAY, user.getBirthday());
                    updateProfile(user.getNickname(), user.getPhone(), user.getRole(), user.getUserId());
                    updateAccountInfo();
                });
            }
        });
    }

    private void updateProfile(String nickname, String phone, String role, long userId) {
        String displayName = nickname == null || nickname.isEmpty() ? "小伴用户" : nickname;
        tvNickname.setText(displayName);
        tvAvatar.setText(displayName.substring(0, 1));
        tvRole.setText("elder".equals(role) ? "老人端" : "子女端");
        tvPhone.setText("手机号：" + (phone == null || phone.isEmpty() ? "未获取" : phone));
        tvUserId.setText("用户ID：" + (userId > 0 ? String.valueOf(userId) : "--"));
    }

    private void updateAccountInfo() {
        tvGender.setText("性别：" + SharedPrefUtil.getString(this, Constants.SP_GENDER, ""));
        tvBirthday.setText("生日：" + SharedPrefUtil.getString(this, Constants.SP_BIRTHDAY, ""));
    }

    private void showNicknameDialog() {
        EditText editText = new EditText(this);
        editText.setHint("请输入新的名字");
        editText.setText(tvNickname.getText());
        editText.setSingleLine(true);
        editText.setSelectAllOnFocus(true);
        editText.setPadding(32, 28, 32, 28);

        new AlertDialog.Builder(this)
                .setTitle("修改名字")
                .setView(editText)
                .setPositiveButton("保存", (dialog, which) -> {
                    String nickname = editText.getText().toString().trim();
                    if (nickname.isEmpty()) {
                        showToast("名字不能为空");
                        return;
                    }
                    saveNickname(nickname);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void saveNickname(String nickname) {
        Map<String, String> body = new HashMap<>();
        body.put("nickname", nickname);

        ApiClient.getInstance(this).getApi().updateProfile(body).enqueue(new ApiCallback<User>() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    SharedPrefUtil.putString(ElderProfileActivity.this, Constants.SP_NICKNAME, user.getNickname());
                    SharedPrefUtil.putString(ElderProfileActivity.this, Constants.SP_PHONE, user.getPhone());
                    SharedPrefUtil.putString(ElderProfileActivity.this, Constants.SP_GENDER, user.getGender());
                    SharedPrefUtil.putString(ElderProfileActivity.this, Constants.SP_BIRTHDAY, user.getBirthday());
                    updateProfile(user.getNickname(), user.getPhone(), user.getRole(), user.getUserId());
                    updateAccountInfo();
                    showToast("名字已修改");
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        showCachedProfile();
    }

    private void logout() {
        SharedPrefUtil.clear(this);
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
