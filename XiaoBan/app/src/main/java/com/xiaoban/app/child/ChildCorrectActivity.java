package com.xiaoban.app.child;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xiaoban.app.R;
import com.xiaoban.app.adapter.CorrectionAdapter;
import com.xiaoban.app.base.BaseActivity;
import com.xiaoban.app.model.Conversation;
import com.xiaoban.app.model.Correction;
import com.xiaoban.app.network.ApiCallback;
import com.xiaoban.app.network.ChildRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * 子女端待确认对话。数据来自真实 /api/conversations/pending（后端已按 active 绑定 +
 * 隐私 + pending 过滤），确认/纠正调真实接口。禁止本地 mock 列表。
 */
public class ChildCorrectActivity extends BaseActivity {

    private ImageView ivBack;
    private RecyclerView rvCorrections;
    private LinearLayout emptyView;
    private CorrectionAdapter adapter;

    private ChildRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_correct);

        repository = ChildRepository.from(this);
        initViews();
        setupRecyclerView();
        loadCorrections();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCorrections();
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        rvCorrections = findViewById(R.id.rv_corrections);
        emptyView = findViewById(R.id.empty_view);

        ivBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new CorrectionAdapter();
        rvCorrections.setLayoutManager(new LinearLayoutManager(this));
        rvCorrections.setAdapter(adapter);

        adapter.setOnActionListener(new CorrectionAdapter.OnCorrectionActionListener() {
            @Override
            public void onConfirm(Correction correction) {
                confirmCorrection(correction);
            }

            @Override
            public void onCorrect(Correction correction, String correctedAnswer) {
                correctAnswer(correction, correctedAnswer);
            }
        });
    }

    private void loadCorrections() {
        repository.loadPending(new ApiCallback<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> data) {
                if (isFinishing()) return;
                runOnUiThread(() -> renderPending(data));
            }

            @Override
            public void onNetworkError(String message) {
                if (isFinishing()) return;
                runOnUiThread(() -> {
                    showToast("待确认列表加载失败，请下拉或返回重试");
                    updateEmptyView(true);
                });
            }

            @Override
            public void onBusinessError(int code, String message) {
                if (isFinishing()) return;
                runOnUiThread(() -> {
                    showToast(message);
                    updateEmptyView(true);
                });
            }
        });
    }

    private void renderPending(List<Conversation> conversations) {
        List<Correction> corrections = new ArrayList<>();
        if (conversations != null) {
            for (Conversation c : conversations) {
                corrections.add(new Correction(
                        String.valueOf(c.getId()),
                        c.getUserQuestion(),
                        c.getAiAnswer(),
                        formatTime(c.getCreatedAt()),
                        c.getConfirmStatus() != null ? c.getConfirmStatus() : "pending"));
            }
        }
        adapter.setCorrections(corrections);
        updateEmptyView(corrections.isEmpty());
    }

    private void confirmCorrection(Correction correction) {
        long conversationId = parseId(correction.getId());
        if (conversationId <= 0) {
            return;
        }
        repository.confirmConversation(conversationId, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                if (isFinishing()) return;
                runOnUiThread(() -> {
                    Toast.makeText(ChildCorrectActivity.this, "已确认", Toast.LENGTH_SHORT).show();
                    loadCorrections();
                });
            }

            @Override
            public void onNetworkError(String message) {
                if (isFinishing()) return;
                runOnUiThread(() -> showToast("确认失败，请检查网络"));
            }

            @Override
            public void onBusinessError(int code, String message) {
                if (isFinishing()) return;
                runOnUiThread(() -> showToast(message));
            }
        });
    }

    private void correctAnswer(Correction correction, String correctedAnswer) {
        long conversationId = parseId(correction.getId());
        if (conversationId <= 0 || correctedAnswer == null || correctedAnswer.trim().isEmpty()) {
            showToast("请输入纠正内容");
            return;
        }
        repository.correctConversation(conversationId, correctedAnswer, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                if (isFinishing()) return;
                runOnUiThread(() -> {
                    Toast.makeText(ChildCorrectActivity.this,
                            "已提交纠正，将通知老人", Toast.LENGTH_SHORT).show();
                    loadCorrections();
                });
            }

            @Override
            public void onNetworkError(String message) {
                if (isFinishing()) return;
                runOnUiThread(() -> showToast("提交失败，请检查网络"));
            }

            @Override
            public void onBusinessError(int code, String message) {
                if (isFinishing()) return;
                runOnUiThread(() -> showToast(message));
            }
        });
    }

    private long parseId(String id) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            return -1L;
        }
    }

    /** Backend serializes createdAt as "yyyy-MM-dd HH:mm:ss". */
    private String formatTime(String createdAt) {
        if (createdAt == null || createdAt.isEmpty()) {
            return "";
        }
        String[] parts = createdAt.split("[ T]");
        if (parts.length >= 2 && parts[1].length() >= 5) {
            return parts[0].length() >= 10
                    ? parts[0].substring(5) + " " + parts[1].substring(0, 5)
                    : parts[1].substring(0, 5);
        }
        return createdAt;
    }

    private void updateEmptyView(boolean isEmpty) {
        if (isEmpty) {
            rvCorrections.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            rvCorrections.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }
}
