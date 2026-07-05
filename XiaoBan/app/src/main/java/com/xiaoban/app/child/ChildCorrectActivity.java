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
import com.xiaoban.app.network.ApiClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChildCorrectActivity extends BaseActivity {

    private ImageView ivBack;
    private RecyclerView rvCorrections;
    private LinearLayout emptyView;
    private CorrectionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_correct);

        initViews();
        setupRecyclerView();
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
        ApiClient.getInstance(this).getApi().getPending()
                .enqueue(new ApiCallback<List<Conversation>>() {
                    @Override
                    public void onSuccess(List<Conversation> data) {
                        runOnUiThread(() -> {
                            List<Correction> corrections = convertConversations(data);
                            adapter.setCorrections(corrections);
                            updateEmptyView(corrections.isEmpty());
                        });
                    }

                    @Override
                    public void onNetworkError(String message) {
                        runOnUiThread(() -> {
                            showToast("待确认对话加载失败");
                            updateEmptyView(adapter.getItemCount() == 0);
                        });
                    }

                    @Override
                    public void onBusinessError(int code, String message) {
                        runOnUiThread(() -> {
                            showToast(message);
                            updateEmptyView(adapter.getItemCount() == 0);
                        });
                    }
                });
    }

    private List<Correction> convertConversations(List<Conversation> conversations) {
        List<Correction> result = new ArrayList<>();
        if (conversations == null) {
            return result;
        }
        for (Conversation conversation : conversations) {
            Correction correction = new Correction(
                    String.valueOf(conversation.getId()),
                    conversation.getUserQuestion(),
                    conversation.getAiAnswer(),
                    formatTime(conversation.getCreatedAt()),
                    conversation.getConfirmStatus());
            correction.setCorrectedAnswer(conversation.getChildCorrection());
            result.add(correction);
        }
        return result;
    }

    private void confirmCorrection(Correction correction) {
        Map<String, Object> body = new HashMap<>();
        body.put("conversationId", Long.parseLong(correction.getId()));
        ApiClient.getInstance(this).getApi().confirm(body)
                .enqueue(new ApiCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        runOnUiThread(() -> {
                            correction.setStatus("confirmed");
                            adapter.notifyDataSetChanged();
                            Toast.makeText(ChildCorrectActivity.this, "已确认", Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }

    private void correctAnswer(Correction correction, String correctedAnswer) {
        Map<String, Object> body = new HashMap<>();
        body.put("conversationId", Long.parseLong(correction.getId()));
        body.put("correction", correctedAnswer);
        ApiClient.getInstance(this).getApi().correct(body)
                .enqueue(new ApiCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        runOnUiThread(() -> {
                            correction.setStatus("corrected");
                            correction.setCorrectedAnswer(correctedAnswer);
                            adapter.notifyDataSetChanged();
                            Toast.makeText(ChildCorrectActivity.this,
                                    "已提交纠正，将通知老人", Toast.LENGTH_SHORT).show();
                        });
                    }
                });
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

    private String formatTime(String isoTime) {
        if (isoTime == null || isoTime.length() < 16) {
            return "";
        }
        int tIndex = isoTime.indexOf('T');
        if (tIndex >= 0 && isoTime.length() >= tIndex + 6) {
            return isoTime.substring(0, 10) + " " + isoTime.substring(tIndex + 1, tIndex + 6);
        }
        return isoTime;
    }
}
