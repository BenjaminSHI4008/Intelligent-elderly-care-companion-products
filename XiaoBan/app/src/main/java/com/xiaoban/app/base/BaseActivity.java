package com.xiaoban.app.base;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.xiaoban.app.util.SharedPrefUtil;
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    protected Long getCurrentUserId() {
        return SharedPrefUtil.getLong(this, Constants.SP_USER_ID, 0L);
    }

    protected String getCurrentRole() {
        return SharedPrefUtil.getString(this, Constants.SP_ROLE, "");
    }
}
