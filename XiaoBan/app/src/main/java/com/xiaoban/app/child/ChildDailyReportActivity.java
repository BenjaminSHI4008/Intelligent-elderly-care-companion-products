package com.xiaoban.app.child;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xiaoban.app.R;
import com.xiaoban.app.adapter.HealthConcernAdapter;
import com.xiaoban.app.adapter.TopicAdapter;
import com.xiaoban.app.base.BaseActivity;
import com.xiaoban.app.model.HealthConcern;
import com.xiaoban.app.model.Topic;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChildDailyReportActivity extends BaseActivity {

    private TextView tvReportDate;
    private TextView tvTotalConversations;
    private RecyclerView rvTopics;
    private RecyclerView rvHealthConcerns;
    private ImageView ivBack;

    private TopicAdapter topicAdapter;
    private HealthConcernAdapter healthConcernAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_daily_report);

        initViews();
        setupRecyclerViews();
        loadReportData();
    }

    private void initViews() {
        ivBack = findViewById(R.id.btn_back);
        tvReportDate = findViewById(R.id.tv_report_date);
        tvTotalConversations = findViewById(R.id.tv_chat_summary);
        rvTopics = findViewById(R.id.rv_topics);
        rvHealthConcerns = findViewById(R.id.rv_health_concerns);

        ivBack.setOnClickListener(v -> finish());

        String currentDate = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA)
                .format(new Date());
        tvReportDate.setText(currentDate);
    }

    private void setupRecyclerViews() {
        topicAdapter = new TopicAdapter();
        rvTopics.setLayoutManager(new LinearLayoutManager(this));
        rvTopics.setAdapter(topicAdapter);

        healthConcernAdapter = new HealthConcernAdapter();
        rvHealthConcerns.setLayoutManager(new LinearLayoutManager(this));
        rvHealthConcerns.setAdapter(healthConcernAdapter);
    }

    private void loadReportData() {
        loadTestData();
    }

    private void loadTestData() {
        tvTotalConversations.setText("今日共 12 次对话");

        List<Topic> topics = new ArrayList<>();
        topics.add(new Topic("💊", "用药咨询", "询问降压药服用时间", 3));
        topics.add(new Topic("🏥", "健康状况", "提及腰部疼痛", 2));
        topics.add(new Topic("🍽️", "饮食记录", "询问晚餐建议", 2));
        topics.add(new Topic("😊", "情感交流", "分享邻居趣事", 3));
        topics.add(new Topic("📺", "娱乐活动", "讨论电视节目", 2));
        topicAdapter.setTopics(topics);

        List<HealthConcern> concerns = new ArrayList<>();
        concerns.add(new HealthConcern(
                "high",
                "连续3天提及腰痛",
                "建议：尽快带父亲就医检查，可能需要拍片确认"));
        concerns.add(new HealthConcern(
                "medium",
                "降压药服用时间混乱",
                "建议：设置定时提醒，确保按时服药"));
        concerns.add(new HealthConcern(
                "low",
                "睡眠时间略有减少",
                "建议：关注后续变化，必要时调整作息"));
        healthConcernAdapter.setConcerns(concerns);
    }
}
