package com.xiaoban.app.child;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xiaoban.app.R;
import com.xiaoban.app.adapter.HealthConcernAdapter;
import com.xiaoban.app.adapter.TopicAdapter;
import com.xiaoban.app.base.BaseActivity;
import com.xiaoban.app.model.DailyReport;
import com.xiaoban.app.model.HealthConcern;
import com.xiaoban.app.model.Topic;
import com.xiaoban.app.network.ApiCallback;
import com.xiaoban.app.network.ApiClient;
import com.xiaoban.app.util.ChildElderSelection;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class ChildDailyReportActivity extends BaseActivity {

    public static final String EXTRA_ELDER_ID = "extra_elder_id";
    public static final String EXTRA_ELDER_NAME = "extra_elder_name";
    public static final String EXTRA_DATE = "extra_date";

    private TextView tvReportDate;
    private TextView tvHealthScore;
    private TextView tvMoodScore;
    private TextView tvActiveScore;
    private TextView tvChatSummary;
    private TextView btnPrevDay;
    private TextView btnNextDay;
    private RecyclerView rvTopics;
    private RecyclerView rvHealthConcerns;
    private CardView cardHealthConcerns;
    private ImageView ivBack;

    private TopicAdapter topicAdapter;
    private HealthConcernAdapter healthConcernAdapter;
    private long elderId;
    private LocalDate selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_daily_report);

        elderId = getIntent().getLongExtra(EXTRA_ELDER_ID, 0L);
        if (elderId <= 0) {
            elderId = ChildElderSelection.getSelectedElderId(this);
        }
        String date = getIntent().getStringExtra(EXTRA_DATE);
        selectedDate = date == null || date.isEmpty() ? LocalDate.now() : LocalDate.parse(date);

        initViews();
        setupRecyclerViews();
        setupDateActions();
        loadReportData();
    }

    private void initViews() {
        ivBack = findViewById(R.id.btn_back);
        tvReportDate = findViewById(R.id.tv_report_date);
        tvHealthScore = findViewById(R.id.tv_health_score);
        tvMoodScore = findViewById(R.id.tv_mood_score);
        tvActiveScore = findViewById(R.id.tv_active_score);
        tvChatSummary = findViewById(R.id.tv_chat_summary);
        btnPrevDay = findViewById(R.id.btn_prev_day);
        btnNextDay = findViewById(R.id.btn_next_day);
        rvTopics = findViewById(R.id.rv_topics);
        rvHealthConcerns = findViewById(R.id.rv_health_concerns);
        cardHealthConcerns = findViewById(R.id.card_health_concerns);

        ivBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerViews() {
        topicAdapter = new TopicAdapter();
        rvTopics.setLayoutManager(new LinearLayoutManager(this));
        rvTopics.setAdapter(topicAdapter);

        healthConcernAdapter = new HealthConcernAdapter();
        rvHealthConcerns.setLayoutManager(new LinearLayoutManager(this));
        rvHealthConcerns.setAdapter(healthConcernAdapter);
    }

    private void setupDateActions() {
        btnPrevDay.setOnClickListener(v -> {
            selectedDate = selectedDate.minusDays(1);
            loadReportData();
        });
        btnNextDay.setOnClickListener(v -> {
            selectedDate = selectedDate.plusDays(1);
            loadReportData();
        });
    }

    private void loadReportData() {
        updateDateLabel();
        if (elderId <= 0) {
            showEmptyReport("请先绑定老人");
            return;
        }

        String date = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        ApiClient.getInstance(this).getApi().getDailyReport(elderId, date)
                .enqueue(new ApiCallback<List<DailyReport>>() {
                    @Override
                    public void onSuccess(List<DailyReport> data) {
                        runOnUiThread(() -> {
                            if (data == null || data.isEmpty()) {
                                showEmptyReport("当天暂无真实日报");
                            } else {
                                showReport(data.get(0));
                            }
                        });
                    }

                    @Override
                    public void onNetworkError(String message) {
                        runOnUiThread(() -> showEmptyReport("日报加载失败，请稍后重试"));
                    }

                    @Override
                    public void onBusinessError(int code, String message) {
                        runOnUiThread(() -> {
                            showEmptyReport("日报加载失败，请稍后重试");
                            showToast(message);
                        });
                    }
                });
    }

    private void showReport(DailyReport report) {
        int total = report.getTotalConversations();
        tvHealthScore.setText(total > 0 ? String.valueOf(Math.min(100, 60 + total * 5)) : "-");
        tvMoodScore.setText(formatMood(report.getMoodScore()));
        tvActiveScore.setText(String.valueOf(total));
        tvChatSummary.setText(report.getSummaryText() == null || report.getSummaryText().isEmpty()
                ? "日报暂无摘要内容"
                : report.getSummaryText());

        topicAdapter.setTopics(parseTopics(report.getTopicDistribution(), total));
        List<HealthConcern> concerns = parseHealthConcerns(report.getHealthFlags());
        healthConcernAdapter.setConcerns(concerns);
        cardHealthConcerns.setVisibility(concerns.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void showEmptyReport(String message) {
        tvHealthScore.setText("-");
        tvMoodScore.setText("🙂");
        tvActiveScore.setText("0");
        tvChatSummary.setText(message);
        topicAdapter.setTopics(new ArrayList<>());
        healthConcernAdapter.setConcerns(new ArrayList<>());
        cardHealthConcerns.setVisibility(View.GONE);
    }

    private List<Topic> parseTopics(String raw, int totalConversations) {
        List<Topic> topics = new ArrayList<>();
        if (raw == null || raw.isEmpty() || "null".equals(raw)) {
            return topics;
        }
        try {
            JSONObject json = new JSONObject(raw);
            Iterator<String> keys = json.keys();
            List<TopicDraft> drafts = new ArrayList<>();
            int totalWeight = 0;
            while (keys.hasNext()) {
                String name = keys.next();
                int weight = Math.max(0, json.optInt(name, 0));
                drafts.add(new TopicDraft(name, weight));
                totalWeight += weight;
            }

            allocateTopicCounts(drafts, Math.max(0, totalConversations), totalWeight);
            allocateTopicPercents(drafts);

            for (TopicDraft draft : drafts) {
                topics.add(new Topic(resolveTopicIcon(draft.name), draft.name,
                        "占比 " + draft.displayPercent + "%", draft.count));
            }
        } catch (Exception ignored) {
        }
        return topics;
    }

    private void allocateTopicCounts(List<TopicDraft> drafts, int totalConversations, int totalWeight) {
        if (drafts.isEmpty() || totalConversations <= 0 || totalWeight <= 0) {
            return;
        }

        int assigned = 0;
        for (TopicDraft draft : drafts) {
            double exact = totalConversations * draft.weight / (double) totalWeight;
            draft.count = (int) Math.floor(exact);
            draft.countRemainder = exact - draft.count;
            assigned += draft.count;
        }

        int remaining = totalConversations - assigned;
        while (remaining > 0) {
            TopicDraft target = null;
            for (TopicDraft draft : drafts) {
                if (target == null
                        || draft.countRemainder > target.countRemainder
                        || (draft.countRemainder == target.countRemainder && draft.weight > target.weight)) {
                    target = draft;
                }
            }
            if (target == null) {
                break;
            }
            target.count++;
            target.countRemainder = -1;
            remaining--;
        }
    }

    private void allocateTopicPercents(List<TopicDraft> drafts) {
        int totalCount = 0;
        for (TopicDraft draft : drafts) {
            totalCount += draft.count;
        }
        if (totalCount <= 0) {
            return;
        }

        int assigned = 0;
        for (TopicDraft draft : drafts) {
            if (draft.count <= 0) {
                draft.displayPercent = 0;
                draft.percentRemainder = 0;
                continue;
            }
            double exact = draft.count * 100.0 / totalCount;
            draft.displayPercent = (int) Math.floor(exact);
            draft.percentRemainder = exact - draft.displayPercent;
            assigned += draft.displayPercent;
        }

        int remaining = 100 - assigned;
        while (remaining > 0) {
            TopicDraft target = null;
            for (TopicDraft draft : drafts) {
                if (draft.count <= 0) {
                    continue;
                }
                if (target == null
                        || draft.percentRemainder > target.percentRemainder
                        || (draft.percentRemainder == target.percentRemainder && draft.count > target.count)) {
                    target = draft;
                }
            }
            if (target == null) {
                break;
            }
            target.displayPercent++;
            target.percentRemainder = -1;
            remaining--;
        }
    }

    private List<HealthConcern> parseHealthConcerns(String raw) {
        List<HealthConcern> concerns = new ArrayList<>();
        if (raw == null || raw.isEmpty() || "null".equals(raw)) {
            return concerns;
        }
        try {
            JSONArray array = new JSONArray(raw);
            for (int i = 0; i < array.length(); i++) {
                String flag = array.optString(i, "");
                if (!flag.isEmpty()) {
                    concerns.add(new HealthConcern("medium", flag, "来自老人端真实对话生成的日报"));
                }
            }
        } catch (Exception ignored) {
        }
        return concerns;
    }

    private void updateDateLabel() {
        tvReportDate.setText(selectedDate.format(
                DateTimeFormatter.ofPattern("yyyy年MM月dd日", Locale.CHINA)));
    }

    private String formatMood(int moodScore) {
        if (moodScore <= 0) {
            return "🙂";
        }
        if (moodScore <= 2) {
            return "😟";
        }
        if (moodScore == 3) {
            return "🙂";
        }
        if (moodScore == 4) {
            return "😊";
        }
        return "😄";
    }

    private String resolveTopicIcon(String topicName) {
        if (topicName == null) {
            return "📌";
        }
        if (topicName.contains("健康")) {
            return "🏥";
        }
        if (topicName.contains("生活")) {
            return "🏠";
        }
        if (topicName.contains("情感")) {
            return "😊";
        }
        return "📌";
    }

    private static class TopicDraft {
        final String name;
        final int weight;
        int count;
        int displayPercent;
        double countRemainder;
        double percentRemainder;

        TopicDraft(String name, int weight) {
            this.name = name;
            this.weight = weight;
        }
    }
}
