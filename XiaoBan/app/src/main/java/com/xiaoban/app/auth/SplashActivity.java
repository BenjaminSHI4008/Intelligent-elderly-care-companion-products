package com.xiaoban.app.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.xiaoban.app.base.BaseActivity;
import com.xiaoban.app.base.Constants;
import com.xiaoban.app.child.ChildHomeActivity;
import com.xiaoban.app.elder.ElderHomeActivity;
import com.xiaoban.app.util.SharedPrefUtil;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Handler().postDelayed(() -> {
            String token = SharedPrefUtil.getString(this, Constants.SP_TOKEN, "");
            String role = SharedPrefUtil.getString(this, Constants.SP_ROLE, "");

            Intent intent;
            if (!token.isEmpty() && !role.isEmpty()) {
                if ("elder".equals(role)) {
                    intent = new Intent(this, ElderHomeActivity.class);
                } else {
                    intent = new Intent(this, ChildHomeActivity.class);
                }
            } else {
                intent = new Intent(this, LoginActivity.class);
            }
            startActivity(intent);
            finish();
        }, 1500);
    }
}
