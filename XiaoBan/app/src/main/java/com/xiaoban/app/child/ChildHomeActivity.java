package com.xiaoban.app.child;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;

import androidx.cardview.widget.CardView;

import com.xiaoban.app.R;
import com.xiaoban.app.auth.LoginActivity;
import com.xiaoban.app.base.BaseActivity;
import com.xiaoban.app.util.SharedPrefUtil;

import java.util.Calendar;

/**
 * 子女端首页
 * 显示父母设备状态、今日摘要、待确认对话、快捷操作
 */
public class ChildHomeActivity extends BaseActivity {

    // 顶部栏
    private TextView tvDeviceStatus;
    private ImageView btnSettings;

    // 状态卡片
    private TextView tvChatCount;
    private TextView tvMood;
    private TextView tvLastActive;

    // 待确认卡片
    private CardView cardPending;
    private TextView tvPendingTime;
    private TextView tvPendingContent;

    // 今日摘要卡片
    private CardView cardDaily;
    private TextView tvReportDate;
    private TextView tvDailySummary;

    // 快捷操作卡片
    private CardView cardReminder;
    private CardView cardMessage;
    private CardView cardHistory;
    private CardView cardBind;

    // 底部导航
    private LinearLayout navHome;
    private LinearLayout navChat;
    private LinearLayout navRemind;
    private LinearLayout navSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_home);

        initViews();
        loadDeviceStatus();
        loadTodayStats();
        loadPendingConfirm();
        loadDailySummary();
        setupClickListeners();
    }

    /**
     * 初始化视图组件
     */
    private void initViews() {
        // 顶部栏
        tvDeviceStatus = findViewById(R.id.tv_device_status);
        btnSettings = findViewById(R.id.btn_settings);

        // 状态卡片
        tvChatCount = findViewById(R.id.tv_chat_count);
        tvMood = findViewById(R.id.tv_mood);
        tvLastActive = findViewById(R.id.tv_last_active);

        // 待确认卡片
        cardPending = findViewById(R.id.card_pending);
        tvPendingTime = findViewById(R.id.tv_pending_time);
        tvPendingContent = findViewById(R.id.tv_pending_content);

        // 今日摘要卡片
        cardDaily = findViewById(R.id.card_daily);
        tvReportDate = findViewById(R.id.tv_report_date);
        tvDailySummary = findViewById(R.id.tv_daily_summary);

        // 快捷操作卡片
        cardReminder = findViewById(R.id.card_reminder);
        cardMessage = findViewById(R.id.card_message);
        cardHistory = findViewById(R.id.card_history);
        cardBind = findViewById(R.id.card_bind);

        // 底部导航
        navHome = findViewById(R.id.nav_home);
        navChat = findViewById(R.id.nav_chat);
        navRemind = findViewById(R.id.nav_remind);
        navSettings = findViewById(R.id.nav_settings);
    }

    /**
     * 加载设备状态
     */
    private void loadDeviceStatus() {
        // TODO: 从API获取设备状态
        // 模拟数据
        boolean isOnline = true;
        String deviceName = "妈妈的设备";

        String statusText = deviceName + " · " + (isOnline ? "在线" : "离线");
        tvDeviceStatus.setText(statusText);
    }

    /**
     * 加载今日统计数据
     */
    private void loadTodayStats() {
        // TODO: 从API获取今日统计
        // 模拟数据
        int chatCount = 8;
        String mood = "😊"; // 情绪：😊开心 😐一般 😢难过 😰焦虑
        String lastActiveTime = "10:18";

        tvChatCount.setText(String.valueOf(chatCount));
        tvMood.setText(mood);
        tvLastActive.setText(lastActiveTime);
    }

    /**
     * 加载待确认对话
     */
    private void loadPendingConfirm() {
        // TODO: 从API获取待确认对话列表
        // 模拟数据
        boolean hasPending = true;

        if (hasPending) {
            cardPending.setVisibility(View.VISIBLE);

            // 设置待确认对话内容
            String pendingTime = "今天 10:20";
            String pendingContent = "妈妈问了降压药的服用时间，AI已回答但建议您确认用药信息。";

            tvPendingTime.setText(pendingTime);
            tvPendingContent.setText(pendingContent);
        } else {
            cardPending.setVisibility(View.GONE);
        }
    }

    /**
     * 加载今日摘要
     */
    private void loadDailySummary() {
        // TODO: 从API获取今日摘要
        // 模拟数据
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        String dateText = month + "月" + day + "日";
        String summaryText = "妈妈今天问了2次关于头晕的问题（建议关注）；主动聊天3次，情绪状态良好；提及膝盖疼，建议近期安排就医。";

        tvReportDate.setText(dateText);
        tvDailySummary.setText(summaryText);
    }

    /**
     * 设置点击事件监听器
     */
    private void setupClickListeners() {
        // 设置按钮
        btnSettings.setOnClickListener(v -> showLogoutDialog());

        // 待确认卡片点击
        cardPending.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChildCorrectActivity.class);
            startActivity(intent);
        });

        // 今日摘要卡片点击
        cardDaily.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChildDailyReportActivity.class);
            startActivity(intent);
        });

        // 快捷操作 - 远程提醒
        cardReminder.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChildReminderActivity.class);
            startActivity(intent);
        });

        // 快捷操作 - 发消息
        cardMessage.setOnClickListener(v -> {
            // TODO: 跳转到发消息页面
        });

        // 快捷操作 - 历史日报
        cardHistory.setOnClickListener(v -> {
            // TODO: 跳转到历史日报列表
        });

        // 快捷操作 - 设备管理
        cardBind.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChildBindActivity.class);
            startActivity(intent);
        });

        // 底部导航 - 首页（当前页）
        navHome.setOnClickListener(v -> {
            // 已在首页，不做操作
        });

        // 底部导航 - 对话
        navChat.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChildCorrectActivity.class);
            startActivity(intent);
        });

        // 底部导航 - 提醒
        navRemind.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChildReminderActivity.class);
            startActivity(intent);
        });

        // 底部导航 - 设置
        navSettings.setOnClickListener(v -> showLogoutDialog());
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
        // 页面恢复时刷新数据
        loadDeviceStatus();
        loadTodayStats();
        loadPendingConfirm();
        loadDailySummary();
    }
}
