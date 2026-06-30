package com.xiaoban.app.child;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.xiaoban.app.R;
import com.xiaoban.app.auth.LoginActivity;
import com.xiaoban.app.base.BaseActivity;
import com.xiaoban.app.elder.ElderProfileActivity;
import com.xiaoban.app.model.BindingRelationItem;
import com.xiaoban.app.network.ApiCallback;
import com.xiaoban.app.network.ApiClient;
import com.xiaoban.app.util.BindNotificationHelper;
import com.xiaoban.app.util.SharedPrefUtil;

import java.util.Calendar;
import java.util.List;

/**
 * 子女端首页
 */
public class ChildHomeActivity extends BaseActivity {

    private TextView tvDeviceStatus;
    private TextView tvBindCount;
    private ImageView btnSettings;

    private TextView tvChatCount;
    private TextView tvMood;
    private TextView tvLastActive;

    private CardView cardPending;
    private TextView tvPendingTime;
    private TextView tvPendingContent;

    private CardView cardDaily;
    private TextView tvReportDate;
    private TextView tvDailySummary;

    private CardView cardReminder;
    private CardView cardMessage;
    private CardView cardHistory;
    private CardView cardBind;

    private LinearLayout navHome;
    private LinearLayout navChat;
    private LinearLayout navRemind;
    private LinearLayout navSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_home);

        initViews();
        loadBoundElders();
        loadTodayStats();
        loadPendingConfirm();
        loadDailySummary();
        setupClickListeners();
    }

    private void initViews() {
        tvDeviceStatus = findViewById(R.id.tv_device_status);
        tvBindCount = findViewById(R.id.tv_bind_count);
        btnSettings = findViewById(R.id.btn_settings);

        tvChatCount = findViewById(R.id.tv_chat_count);
        tvMood = findViewById(R.id.tv_mood);
        tvLastActive = findViewById(R.id.tv_last_active);

        cardPending = findViewById(R.id.card_pending);
        tvPendingTime = findViewById(R.id.tv_pending_time);
        tvPendingContent = findViewById(R.id.tv_pending_content);

        cardDaily = findViewById(R.id.card_daily);
        tvReportDate = findViewById(R.id.tv_report_date);
        tvDailySummary = findViewById(R.id.tv_daily_summary);

        cardReminder = findViewById(R.id.card_reminder);
        cardMessage = findViewById(R.id.card_message);
        cardHistory = findViewById(R.id.card_history);
        cardBind = findViewById(R.id.card_bind);

        navHome = findViewById(R.id.nav_home);
        navChat = findViewById(R.id.nav_chat);
        navRemind = findViewById(R.id.nav_remind);
        navSettings = findViewById(R.id.nav_settings);
    }

    private void loadBoundElders() {
        ApiClient.getInstance(this).getApi().getBindRelations()
                .enqueue(new ApiCallback<List<BindingRelationItem>>() {
                    @Override
                    public void onSuccess(List<BindingRelationItem> data) {
                        if (isFinishing()) return;
                        runOnUiThread(() -> updateBoundElderUi(data));
                    }

                    @Override
                    public void onNetworkError(String message) {
                        if (isFinishing()) return;
                        runOnUiThread(() -> {
                            tvDeviceStatus.setText("暂未绑定老人 · 点击添加");
                            tvBindCount.setText("去绑定");
                        });
                    }
                });
    }

    private void updateBoundElderUi(List<BindingRelationItem> relations) {
        int count = BindNotificationHelper.countActiveRelations(relations);
        String elderNames = BindNotificationHelper.formatElderNamesForChild(relations);

        if (count == 0 || elderNames == null) {
            tvDeviceStatus.setText("暂未绑定老人 · 点击添加");
            tvBindCount.setText("去绑定");
            return;
        }

        if (count == 1) {
            tvDeviceStatus.setText(elderNames + " · 已绑定");
        } else {
            tvDeviceStatus.setText(elderNames + " · 已绑定" + count + "位");
        }
        tvBindCount.setText("已绑定 " + count + " 位");
    }

    private void loadTodayStats() {
        // TODO: 接入日报/对话统计 API
        tvChatCount.setText("-");
        tvMood.setText("-");
        tvLastActive.setText("-");
    }

    private void loadPendingConfirm() {
        // TODO: 接入待确认对话 API
        cardPending.setVisibility(View.GONE);
    }

    private void loadDailySummary() {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        tvReportDate.setText(month + "月" + day + "日");
        tvDailySummary.setText("绑定老人后，可在此查看智能日报摘要");
    }

    private void setupClickListeners() {
        btnSettings.setOnClickListener(v -> showLogoutDialog());

        cardPending.setOnClickListener(v ->
                startActivity(new Intent(this, ChildCorrectActivity.class)));

        cardDaily.setOnClickListener(v ->
                startActivity(new Intent(this, ChildDailyReportActivity.class)));

        cardReminder.setOnClickListener(v ->
                startActivity(new Intent(this, ChildReminderActivity.class)));

        cardMessage.setOnClickListener(v ->
                startActivity(new Intent(this, ChildSendMessageActivity.class)));

        cardHistory.setOnClickListener(v ->
                startActivity(new Intent(this, ChildDailyReportActivity.class)));

        cardBind.setOnClickListener(v ->
                startActivity(new Intent(this, ChildBindActivity.class)));

        navHome.setOnClickListener(v -> { });

        navChat.setOnClickListener(v ->
                startActivity(new Intent(this, ChildCorrectActivity.class)));

        navRemind.setOnClickListener(v ->
                startActivity(new Intent(this, ChildReminderActivity.class)));

        navSettings.setOnClickListener(v ->
                startActivity(new Intent(this, ElderProfileActivity.class)));
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("设置")
                .setMessage("确定要退出登录吗？")
                .setNegativeButton("取消", null)
                .setPositiveButton("退出登录", (dialog, which) -> logout())
                .show();
    }

    private void logout() {
        SharedPrefUtil.clear(this);
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBoundElders();
    }
}
