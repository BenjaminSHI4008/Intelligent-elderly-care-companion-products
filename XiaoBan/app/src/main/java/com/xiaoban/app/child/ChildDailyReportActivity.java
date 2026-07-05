package com.xiaoban.app.child;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

<<<<<<< HEAD
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
=======
>>>>>>> ae5245dd4789e2f7f705836e323616db126ae8e9
import com.xiaoban.app.R;
import com.xiaoban.app.adapter.HealthConcernAdapter;
import com.xiaoban.app.adapter.TopicAdapter;
import com.xiaoban.app.base.BaseActivity;
<<<<<<< HEAD
import com.xiaoban.app.model.DailyReport;
import com.xiaoban.app.model.HealthConcern;
import com.xiaoban.app.model.Topic;
import com.xiaoban.app.network.ApiCallback;
import com.xiaoban.app.network.ChildRepository;
import com.xiaoban.app.util.ElderSelectionStore;
=======
import com.xiaoban.app.model.HealthConcern;
import com.xiaoban.app.model.Topic;
>>>>>>> ae5245dd4789e2f7f705836e323616db126ae8e9

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
<<<<<<< HEAD
import java.util.Map;

/**
 * 子女端智能日报。数据来自真实 /api/report/daily（后端已按 active 绑定校验）。
 * 无数据→空态；网络失败→错误态。禁止本地拼接测试日报。
 */
public class ChildDailyReportActivity extends BaseActivity {

    private static final Gson GSON = new Gson();

    private static final Map<String, String> TOPIC_ICONS = Map.of(
            "健康", "🏥",
            "生活", "🍽️",
            "情感", "😊",
            "其他", "📺");

=======

public class ChildDailyReportActivity extends BaseActivity {

>>>>>>> ae5245dd4789e2f7f705836e323616db126ae8e9
    private TextView tvReportDate;
    private TextView tvTotalConversations;
    private RecyclerView rvTopics;
    private RecyclerView rvHealthConcerns;
    private ImageView ivBack;

    private TopicAdapter topicAdapter;
    private HealthConcernAdapter healthConcernAdapter;

<<<<<<< HEAD
    private ChildRepository repository;
    private long elderId = ElderSelectionStore.NO_ELDER;

=======
>>>>>>> ae5245dd4789e2f7f705836e323616db126ae8e9
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_daily_report);

<<<<<<< HEAD
        repository = ChildRepository.from(this);
        initViews();
        setupRecyclerViews();
        elderId = ElderSelectionStore.getSelected(this);
=======
        initViews();
        setupRecyclerViews();
>>>>>>> ae5245dd4789e2f7f705836e323616db126ae8e9
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
<<<<<<< HEAD
        if (elderId == ElderSelectionStore.NO_ELDER) {
            tvTotalConversations.setText("请先在首页绑定并选择老人");
            return;
        }
        repository.loadDailyReport(elderId, null, new ApiCallback<List<DailyReport>>() {
            @Override
            public void onSuccess(List<DailyReport> data) {
                if (isFinishing()) return;
                runOnUiThread(() -> renderReport(data));
            }

            @Override
            public void onNetworkError(String message) {
                if (isFinishing()) return;
                runOnUiThread(() -> tvTotalConversations.setText("日报加载失败，请返回重试"));
            }

            @Override
            public void onBusinessError(int code, String message) {
                if (isFinishing()) return;
                runOnUiThread(() -> tvTotalConversations.setText(message));
            }
        });
    }

    private void renderReport(List<DailyReport> reports) {
        if (reports == null || reports.isEmpty()) {
            tvTotalConversations.setText("今日暂无日报（日报每天定时生成）");
            topicAdapter.setTopics(new ArrayList<>());
            healthConcernAdapter.setConcerns(new ArrayList<>());
            return;
        }

        DailyReport report = reports.get(0);
        tvTotalConversations.setText("今日共 " + report.getTotalConversations() + " 次对话");
        topicAdapter.setTopics(parseTopics(report.getTopicDistribution()));
        healthConcernAdapter.setConcerns(parseHealthConcerns(report.getHealthFlags()));
    }

    private List<Topic> parseTopics(String topicDistributionJson) {
        List<Topic> topics = new ArrayList<>();
        if (topicDistributionJson == null || topicDistributionJson.isEmpty()) {
            return topics;
        }
        try {
            JsonElement root = GSON.fromJson(topicDistributionJson, JsonElement.class);
            if (!root.isJsonObject()) {
                return topics;
            }
            JsonObject obj = root.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                String name = entry.getKey();
                int percent = entry.getValue().getAsInt();
                String icon = TOPIC_ICONS.getOrDefault(name, "💬");
                topics.add(new Topic(icon, name, "占比 " + percent + "%", percent));
            }
        } catch (RuntimeException e) {
            // Malformed AI JSON: show nothing rather than fake topics.
        }
        return topics;
    }

    private List<HealthConcern> parseHealthConcerns(String healthFlagsJson) {
        List<HealthConcern> concerns = new ArrayList<>();
        if (healthFlagsJson == null || healthFlagsJson.isEmpty()) {
            return concerns;
        }
        try {
            JsonElement root = GSON.fromJson(healthFlagsJson, JsonElement.class);
            if (!root.isJsonArray()) {
                return concerns;
            }
            JsonArray arr = root.getAsJsonArray();
            for (JsonElement el : arr) {
                String flag = el.getAsString();
                if (flag != null && !flag.trim().isEmpty()) {
                    concerns.add(new HealthConcern("medium", flag, "建议关注后续变化，必要时就医"));
                }
            }
        } catch (RuntimeException e) {
            // Malformed AI JSON: show nothing rather than fake concerns.
        }
        return concerns;
=======
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
>>>>>>> ae5245dd4789e2f7f705836e323616db126ae8e9
    }
}
