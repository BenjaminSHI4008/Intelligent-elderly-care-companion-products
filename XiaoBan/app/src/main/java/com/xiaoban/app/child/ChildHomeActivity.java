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
import com.xiaoban.app.model.ChildDashboard;
import com.xiaoban.app.network.ApiCallback;
import com.xiaoban.app.network.ApiClient;
import com.xiaoban.app.util.BindNotificationHelper;
import com.xiaoban.app.util.ChildElderSelection;
import com.xiaoban.app.util.SharedPrefUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
    private TextView tvReminderCount;
    private CardView cardMessage;
    private TextView tvMessageCount;
    private CardView cardHistory;
    private CardView cardBind;

    private LinearLayout navHome;
    private LinearLayout navChat;
    private LinearLayout navRemind;
    private LinearLayout navSettings;

    private long currentElderId;
    private String currentElderName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_home);

        initViews();
        setupClickListeners();
        showLoadingState();
        loadBoundElders();
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
        tvReminderCount = findViewById(R.id.tv_reminder_count);
        cardMessage = findViewById(R.id.card_message);
        tvMessageCount = findViewById(R.id.tv_message_count);
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
                            showUnboundState("绑定关系加载失败，稍后重试");
                            showToast("绑定关系加载失败");
                        });
                    }

                    @Override
                    public void onBusinessError(int code, String message) {
                        if (isFinishing()) return;
                        runOnUiThread(() -> {
                            showUnboundState("绑定关系加载失败，稍后重试");
                            showToast(message);
                        });
                    }
                });
    }

    private void updateBoundElderUi(List<BindingRelationItem> relations) {
        int count = BindNotificationHelper.countActiveRelations(relations);
        BindingRelationItem selected = ChildElderSelection.resolve(this, relations);
        if (count == 0 || selected == null) {
            showUnboundState("暂未绑定老人 · 点击添加");
            return;
        }

        currentElderId = selected.getElderId();
        currentElderName = selected.getDisplayNameForChild();
        if (currentElderName == null || currentElderName.isEmpty()) {
            currentElderName = "老人";
        }

        String suffix = count > 1 ? " · 已绑定" + count + "位" : " · 已绑定";
        tvDeviceStatus.setText(currentElderName + suffix);
        tvBindCount.setText("已绑定 " + count + " 位");
        loadDashboard();
    }

    private void loadDashboard() {
        if (currentElderId <= 0) {
            showUnboundState("暂未绑定老人 · 点击添加");
            return;
        }

        String today = formatDateParam(new Date());
        ApiClient.getInstance(this).getApi().getChildDashboard(currentElderId, today)
                .enqueue(new ApiCallback<ChildDashboard>() {
                    @Override
                    public void onSuccess(ChildDashboard data) {
                        if (isFinishing()) return;
                        runOnUiThread(() -> updateDashboardUi(data));
                    }

                    @Override
                    public void onNetworkError(String message) {
                        if (isFinishing()) return;
                        runOnUiThread(() -> {
                            tvDailySummary.setText("真实数据加载失败，请稍后重试");
                            showToast("首页数据加载失败");
                        });
                    }

                    @Override
                    public void onBusinessError(int code, String message) {
                        if (isFinishing()) return;
                        runOnUiThread(() -> {
                            tvDailySummary.setText("真实数据加载失败，请稍后重试");
                            showToast(message);
                        });
                    }
                });
    }

    private void updateDashboardUi(ChildDashboard dashboard) {
        tvReportDate.setText(formatMonthDay(new Date()));
        if (dashboard == null) {
            tvDailySummary.setText("暂无真实数据");
            return;
        }

        ChildDashboard.TodayStatus status = dashboard.getTodayStatus();
        if (status != null) {
            tvChatCount.setText(String.valueOf(status.getChatCount()));
            tvMood.setText(status.getMoodLabel() == null ? "🙂" : status.getMoodLabel());
            tvLastActive.setText(formatTime(status.getLastActiveAt()));
            updatePendingCard(status.getPendingCount());
        } else {
            tvChatCount.setText("-");
            tvMood.setText("🙂");
            tvLastActive.setText("-");
            cardPending.setVisibility(View.GONE);
        }

        ChildDashboard.DailySummary daily = dashboard.getDailySummary();
        if (daily != null && daily.isGenerated() && daily.getSummaryText() != null
                && !daily.getSummaryText().isEmpty()) {
            tvDailySummary.setText(daily.getSummaryText());
        } else {
            tvDailySummary.setText("暂无今日日报，老人端产生对话并生成日报后会显示。");
        }

        ChildDashboard.ReminderSummary reminder = dashboard.getReminderSummary();
        int activeReminders = reminder != null ? reminder.getActiveCount() : 0;
        tvReminderCount.setText(activeReminders + "个提醒已设置");

        ChildDashboard.MessageSummary message = dashboard.getMessageSummary();
        int unread = message != null ? message.getUnreadCount() : 0;
        tvMessageCount.setText(unread > 0 ? unread + "条未读消息" : "暂无未读消息");
    }

    private void updatePendingCard(int pendingCount) {
        if (pendingCount <= 0) {
            cardPending.setVisibility(View.GONE);
            return;
        }
        cardPending.setVisibility(View.VISIBLE);
        tvPendingTime.setText("今天");
        tvPendingContent.setText("有 " + pendingCount + " 条老人端真实对话需要确认");
    }

    private void showLoadingState() {
        tvDeviceStatus.setText("正在加载绑定关系...");
        tvBindCount.setText("-");
        tvChatCount.setText("-");
        tvMood.setText("🙂");
        tvLastActive.setText("-");
        tvReportDate.setText(formatMonthDay(new Date()));
        tvDailySummary.setText("正在加载真实数据...");
        tvReminderCount.setText("-");
        tvMessageCount.setText("-");
        cardPending.setVisibility(View.GONE);
    }

    private void showUnboundState(String deviceStatus) {
        currentElderId = 0L;
        currentElderName = "";
        tvDeviceStatus.setText(deviceStatus);
        tvBindCount.setText("去绑定");
        tvChatCount.setText("-");
        tvMood.setText("🙂");
        tvLastActive.setText("-");
        tvReportDate.setText(formatMonthDay(new Date()));
        tvDailySummary.setText("绑定老人后，可在此查看真实日报摘要");
        tvReminderCount.setText("绑定后查看");
        tvMessageCount.setText("绑定后查看");
        cardPending.setVisibility(View.GONE);
    }

    private void setupClickListeners() {
        btnSettings.setOnClickListener(v -> showLogoutDialog());
        tvDeviceStatus.setOnClickListener(v -> openDeviceManagement());

        cardPending.setOnClickListener(v ->
                startActivity(new Intent(this, ChildCorrectActivity.class)));

        cardDaily.setOnClickListener(v -> openDailyReport());
        cardHistory.setOnClickListener(v -> openDailyReport());
        cardReminder.setOnClickListener(v -> openReminder());

        cardMessage.setOnClickListener(v ->
                startActivity(new Intent(this, ChildMessageListActivity.class)));

        cardBind.setOnClickListener(v -> openDeviceManagement());

        navHome.setOnClickListener(v -> { });

        navChat.setOnClickListener(v ->
                startActivity(new Intent(this, ChildCorrectActivity.class)));

        navRemind.setOnClickListener(v -> openReminder());

        navSettings.setOnClickListener(v ->
                startActivity(new Intent(this, ElderProfileActivity.class)));
    }

    private void openDailyReport() {
        if (currentElderId <= 0) {
            showToast("请先绑定老人");
            return;
        }
        Intent intent = new Intent(this, ChildDailyReportActivity.class);
        intent.putExtra(ChildDailyReportActivity.EXTRA_ELDER_ID, currentElderId);
        intent.putExtra(ChildDailyReportActivity.EXTRA_ELDER_NAME, currentElderName);
        intent.putExtra(ChildDailyReportActivity.EXTRA_DATE, formatDateParam(new Date()));
        startActivity(intent);
    }

    private void openReminder() {
        if (currentElderId <= 0) {
            showToast("请先绑定老人");
            return;
        }
        Intent intent = new Intent(this, ChildReminderActivity.class);
        intent.putExtra(ChildReminderActivity.EXTRA_ELDER_ID, currentElderId);
        intent.putExtra(ChildReminderActivity.EXTRA_ELDER_NAME, currentElderName);
        startActivity(intent);
    }

    private void openDeviceManagement() {
        startActivity(new Intent(this, ChildBindActivity.class));
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

    private String formatDateParam(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(date);
    }

    private String formatMonthDay(Date date) {
        return new SimpleDateFormat("M月d日", Locale.CHINA).format(date);
    }

    private String formatTime(String isoTime) {
        if (isoTime == null || isoTime.length() < 16) {
            return "-";
        }
        int tIndex = isoTime.indexOf('T');
        if (tIndex >= 0 && isoTime.length() >= tIndex + 6) {
            return isoTime.substring(tIndex + 1, tIndex + 6);
        }
        return isoTime.length() >= 16 ? isoTime.substring(11, 16) : "-";
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBoundElders();
    }
}
