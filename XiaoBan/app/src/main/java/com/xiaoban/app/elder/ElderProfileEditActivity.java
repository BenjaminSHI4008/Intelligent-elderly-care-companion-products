package com.xiaoban.app.elder;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.xiaoban.app.R;
import com.xiaoban.app.base.BaseActivity;
import com.xiaoban.app.base.Constants;
import com.xiaoban.app.model.User;
import com.xiaoban.app.network.ApiCallback;
import com.xiaoban.app.network.ApiClient;
import com.xiaoban.app.util.SharedPrefUtil;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class ElderProfileEditActivity extends BaseActivity {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    private TextView tvAvatar;
    private TextView tvGenderValue;
    private TextView tvBirthdayValue;
    private TextView tvPhoneValue;
    private TextView tvPasswordValue;
    private View rowGender;
    private View rowBirthday;
    private View rowPhone;
    private View rowPassword;
    private View btnBack;
    private View btnSave;
    private String newPassword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elder_profile_edit);

        initViews();
        loadCachedValues();
        setupListeners();
    }

    private void initViews() {
        tvAvatar = findViewById(R.id.tv_avatar);
        tvGenderValue = findViewById(R.id.tv_gender_value);
        tvBirthdayValue = findViewById(R.id.tv_birthday_value);
        tvPhoneValue = findViewById(R.id.tv_phone_value);
        tvPasswordValue = findViewById(R.id.tv_password_value);
        rowGender = findViewById(R.id.row_gender);
        rowBirthday = findViewById(R.id.row_birthday);
        rowPhone = findViewById(R.id.row_phone);
        rowPassword = findViewById(R.id.row_password);
        btnBack = findViewById(R.id.btn_back);
        btnSave = findViewById(R.id.btn_save);
    }

    private void loadCachedValues() {
        String nickname = SharedPrefUtil.getString(this, Constants.SP_NICKNAME, "");
        String displayName = nickname == null || nickname.isEmpty() ? "小" : nickname;
        tvAvatar.setText(displayName.substring(0, 1));
        tvGenderValue.setText(SharedPrefUtil.getString(this, Constants.SP_GENDER, ""));
        setBirthdayValue(SharedPrefUtil.getString(this, Constants.SP_BIRTHDAY, ""));
        tvPhoneValue.setText(SharedPrefUtil.getString(this, Constants.SP_PHONE, ""));
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        rowGender.setOnClickListener(v -> showGenderDialog());
        rowBirthday.setOnClickListener(v -> showBirthdayDialog());
        rowPhone.setOnClickListener(v -> showPhoneDialog());
        rowPassword.setOnClickListener(v -> showPasswordDialog());
        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void showGenderDialog() {
        String[] genders = {"男", "女"};
        new AlertDialog.Builder(this)
                .setTitle("选择性别")
                .setItems(genders, (dialog, which) -> tvGenderValue.setText(genders[which]))
                .show();
    }

    private void showBirthdayDialog() {
        Calendar calendar = Calendar.getInstance();
        String birthday = tvBirthdayValue.getText().toString();
        if (!birthday.isEmpty() && birthday.contains("-")) {
            String[] parts = birthday.split("-");
            if (parts.length == 3) {
                calendar.set(parseInt(parts[0], 1950), parseInt(parts[1], 1) - 1, parseInt(parts[2], 1));
            }
        } else {
            calendar.set(1950, Calendar.JANUARY, 1);
        }

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String value = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    setBirthdayValue(value);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        DatePicker datePicker = dialog.getDatePicker();
        Calendar min = Calendar.getInstance();
        min.set(1850, Calendar.JANUARY, 1);
        Calendar max = Calendar.getInstance();
        max.set(2026, Calendar.DECEMBER, 31);
        datePicker.setMinDate(min.getTimeInMillis());
        datePicker.setMaxDate(max.getTimeInMillis());

        dialog.setTitle("请准确选择生日");
        dialog.show();
    }

    private void showPhoneDialog() {
        EditText editText = new EditText(this);
        editText.setHint("请输入手机号");
        editText.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        editText.setSingleLine(true);
        editText.setText(tvPhoneValue.getText());
        editText.setSelection(editText.getText().length());
        editText.setPadding(32, 28, 32, 28);

        new AlertDialog.Builder(this)
                .setTitle("修改手机号")
                .setView(editText)
                .setPositiveButton("保存", (dialog, which) -> tvPhoneValue.setText(editText.getText().toString().trim()))
                .setNegativeButton("取消", null)
                .show();
    }

    private void showPasswordDialog() {
        EditText editText = new EditText(this);
        editText.setHint("请输入新密码");
        editText.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        editText.setSingleLine(true);
        editText.setPadding(32, 28, 32, 28);

        new AlertDialog.Builder(this)
                .setTitle("修改密码")
                .setMessage("保存后，下次登录需使用新密码。")
                .setView(editText)
                .setPositiveButton("保存", (dialog, which) -> {
                    String password = editText.getText().toString().trim();
                    if (password.isEmpty()) {
                        showToast("密码不能为空");
                        return;
                    }
                    newPassword = password;
                    tvPasswordValue.setText("已修改，待保存");
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void setBirthdayValue(String value) {
        tvBirthdayValue.setText(value == null || value.isEmpty() ? "填写生日完善信息" : value);
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private void saveProfile() {
        String birthday = tvBirthdayValue.getText().toString();
        if ("填写生日完善信息".equals(birthday)) {
            birthday = "";
        }

        String phone = tvPhoneValue.getText().toString().trim();
        if (phone.isEmpty() || !PHONE_PATTERN.matcher(phone).matches()) {
            showToast("请输入11位有效手机号");
            return;
        }

        Map<String, String> body = new HashMap<>();
        body.put("phone", phone);
        body.put("gender", tvGenderValue.getText().toString().trim());
        body.put("birthday", birthday);
        if (!newPassword.isEmpty()) {
            body.put("password", newPassword);
        }

        ApiClient.getInstance(this).getApi().updateProfile(body).enqueue(new ApiCallback<User>() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    SharedPrefUtil.putString(ElderProfileEditActivity.this, Constants.SP_PHONE, user.getPhone());
                    SharedPrefUtil.putString(ElderProfileEditActivity.this, Constants.SP_GENDER, user.getGender());
                    SharedPrefUtil.putString(ElderProfileEditActivity.this, Constants.SP_BIRTHDAY, user.getBirthday());
                    showToast("已保存");
                    finish();
                });
            }
        });
    }
}
