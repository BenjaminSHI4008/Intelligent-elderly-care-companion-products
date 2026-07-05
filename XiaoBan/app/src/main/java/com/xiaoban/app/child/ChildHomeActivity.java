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
import com.xiaoban.app.model.DashboardData;
import com.xiaoban.app.network.ApiCallback;
import com.xiaoban.app.network.ApiClient;
import com.xiaoban.app.network.ChildRepository;
import com.xiaoban.app.util.BindNotificationHelper;
import com.xiaoban.app.util.ElderSelectionStore;
import com.xiaoban.app.util.SharedPrefUtil;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * 子女端首页。所有状态均来自真实 /api/child/dashboard 聚合，
 * 无绑定→空态引导绑定；网络失败→错误态占位 + 点击状态卡重试；禁止假数据。
 */
public class ChildHomeActivity extends BaseActivity {

    private static final String PLACEHOLDER = "—";

    private TextView tvDeviceStatus;
    private TextView tvBindCount;
    private ImageView btnSettings;

    private TextView tvChatCount;
    private TextView tvMood;
    private TextView tvLastActive;
    private TextView tvReminderCount;

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

    private ChildRepository repository;
    private long selectedElderId = ElderSelectionStore.NO_ELDER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_home);

        repository = ChildRepository.from(this);
        initViews();
        setupClickListeners();
        resetReportDateLabel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBoundEldersThenDashboard();
    }

    private void initViews() {
        tvDeviceStatus = findViewById(R.id.tv_device_status);
        tvBindCount = findViewById(R.id.tv_bind_count);
        btnSettings = findViewById(R.id.btn_settings);

        tvChatCount = findViewById(R.id.tv_chat_count);
        tvMood = findViewById(R.id.tv_mood);
        tvLastActive = findViewById(R.id.tv_last_active);
        tvReminderCount = findViewById(R.id.tv_reminder_count);

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

    private void loadBoundEldersThenDashboard() {
        ApiClient.getInstance(this).getApi().getBindRelations()
                .enqueue(new ApiCallback<List<BindingRelationItem>>() {
                    @Override
                    public void onSuccess(List<BindingRelationItem> data) {
                        if (isFinishing()) return;
                        runOnUiThread(() -> {
                            updateBoundElderUi(data);
                            selectedElderId = ElderSelectionStore.resolve(ChildHomeActivity.this, data);
                            if (selectedElderId == ElderSelectionStore.NO_ELDER) {
                                showNoElderState();
                            } else {
                                loadDashboard(selectedElderId);
                            }
                        });
                    }

                    @Override
                    public void onNetworkError(String message) {
                        if (isFinishing()) return;
                        runOnUiThread(() -> {
                            tvDeviceStatus.setText("网络异常 · 点击重试");
                            tvBindCount.setText("重试");
                            showErrorState();
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

    private void loadDashboard(long elderId) {
        repository.loadDashboard(elderId, null, new ApiCallback<DashboardData>() {
            @Override
            public void onSuccess(DashboardData data) {
                if (isFinishing()) return;
                runOnUiThread(() -> renderDashboard(data));
            }

            @Override
            public void onNetworkError(String message) {
                if (isFinishing()) return;
                runOnUiThread(() -> {
                    showErrorState();
                    showToast("首页数据加载失败，点击今日摘要重试");
                });
            }

            @Override
            public void onBusinessError(int code, String message) {
                if (isFinishing()) return;
                runOnUiThread(() -> {
                    showErrorState();
                    showToast(message);
                });
            }
        });
    }

    private void renderDashboard(DashboardData data) {
        if (data == null) {
            showErrorState();
            return;
        }

        tvChatCount.setText(data.getTodayChatCount() != null
                ? String.valueOf(data.getTodayChatCount()) : PLACEHOLDER);
        tvMood.setText(moodEmoji(data.getMoodScore()));
        tvLastActive.setText(extractTime(data.getLastActiveAt()));

        int reminderCount = data.getActiveReminderCount() != null ? data.getActiveReminderCount() : 0;
        tvReminderCount.setText(reminderCount + "个提醒已设置");

        renderPending(data);
        renderSummary(data);
    }

    private void renderPending(DashboardData data) {
        int pending = data.getPendingCount() != null ? data.getPendingCount() : 0;
        if (pending > 0) {
            cardPending.setVisibility(View.VISIBLE);
            tvPendingTime.setText(extractTime(data.getLastActiveAt()));
            tvPendingContent.setText("有 " + pending + " 条对话待您确认，点击查看");
        } else {
            cardPending.setVisibility(View.GONE);
        }
    }

    private void renderSummary(DashboardData data) {
        String summary = data.getSummaryText();
        if (summary != null && !summary.trim().isEmpty()) {
            tvDailySummary.setText(summary);
        } else {
            tvDailySummary.setText("今日暂无日报摘要（日报每天生成）");
        }
    }

    private void showNoElderState() {
        tvChatCount.setText(PLACEHOLDER);
        tvMood.setText(PLACEHOLDER);
        tvLastActive.setText(PLACEHOLDER);
        tvReminderCount.setText(PLACEHOLDER);
        cardPending.setVisibility(View.GONE);
        tvDailySummary.setText("绑定老人后，可在此查看智能日报摘要");
    }

    private void showErrorState() {
        tvChatCount.setText(PLACEHOLDER);
        tvMood.setText(PLACEHOLDER);
        tvLastActive.setText(PLACEHOLDER);
        tvReminderCount.setText(PLACEHOLDER);
        cardPending.setVisibility(View.GONE);
        tvDailySummary.setText("数据加载失败，点击重试");
    }

    private String moodEmoji(Integer moodScore) {
        if (moodScore == null) {
            return PLACEHOLDER;
        }
        switch (moodScore) {
            case 5: return "😄";
            case 4: return "😊";
            case 3: return "🙂";
            case 2: return "😕";
            case 1: return "😟";
            default: return PLACEHOLDER;
        }
    }

    /** Backend serializes LocalDateTime as "yyyy-MM-dd HH:mm:ss"; show HH:mm. */
    private String extractTime(String dateTime) {
        if (dateTime == null || dateTime.isEmpty()) {
            return PLACEHOLDER;
        }
        String[] parts = dateTime.split("[ T]");
        if (parts.length >= 2 && parts[1].length() >= 5) {
            return parts[1].substring(0, 5);
        }
        return PLACEHOLDER;
    }

    private void resetReportDateLabel() {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        tvReportDate.setText(String.format(Locale.CHINA, "%d月%d日", month, day));
    }

    private void setupClickListeners() {
        btnSettings.setOnClickListener(v -> showLogoutDialog());
        tvDeviceStatus.setOnClickListener(v -> openDeviceManagement());

        cardPending.setOnClickListener(v ->
                startActivity(new Intent(this, ChildCorrectActivity.class)));

        // Daily summary card doubles as retry trigger when data failed to load.
        cardDaily.setOnClickListener(v -> {
            if (selectedElderId != ElderSelectionStore.NO_ELDER) {
                startActivity(new Intent(this, ChildDailyReportActivity.class));
            } else {
                openDeviceManagement();
            }
        });

        cardReminder.setOnClickListener(v ->
                startActivity(new Intent(this, ChildReminderActivity.class)));

        cardMessage.setOnClickListener(v ->
                startActivity(new Intent(this, ChildMessageListActivity.class)));

        cardHistory.setOnClickListener(v ->
                startActivity(new Intent(this, ChildDailyReportActivity.class)));

        cardBind.setOnClickListener(v -> openDeviceManagement());

        navHome.setOnClickListener(v -> loadBoundEldersThenDashboard());

        navChat.setOnClickListener(v ->
                startActivity(new Intent(this, ChildCorrectActivity.class)));

        navRemind.setOnClickListener(v ->
                startActivity(new Intent(this, ChildReminderActivity.class)));

        navSettings.setOnClickListener(v ->
                startActivity(new Intent(this, ElderProfileActivity.class)));
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
}
