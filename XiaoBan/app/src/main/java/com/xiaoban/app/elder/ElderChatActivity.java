package com.xiaoban.app.elder;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xiaoban.app.R;
import com.xiaoban.app.base.BaseActivity;
import com.xiaoban.app.elder.adapter.ChatAdapter;
import com.xiaoban.app.elder.adapter.ChatHistoryAdapter;
import com.xiaoban.app.model.ChatResponse;
import com.xiaoban.app.model.ChatHistorySession;
import com.xiaoban.app.network.ApiCallback;
import com.xiaoban.app.network.ApiClient;
import com.xiaoban.app.util.ChatHistoryStore;
import com.xiaoban.app.voice.VoiceManager;
import com.xiaoban.app.voice.VoiceRecognizer;
import com.xiaoban.app.widget.VoiceButton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ElderChatActivity extends BaseActivity {

    private RecyclerView rvChat;
    private ChatAdapter chatAdapter;
    private VoiceButton voiceButton;
    private View thinkingIndicator;
    private TextView tvThinkingDots;
    private View btnBack;
    private View btnHistory;
    private View btnCloseHistory;
    private View historyScrim;
    private View historyPanel;
    private TextView tvHistoryEmpty;
    private RecyclerView rvHistorySessions;
    private ChatHistoryAdapter historyAdapter;

    private String sessionId;
    private Handler handler = new Handler(Looper.getMainLooper());
    private ValueAnimator thinkingAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elder_chat);

        sessionId = getIntent().getStringExtra("sessionId");
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }
        String userQuestion = getIntent().getStringExtra("userQuestion");
        String aiAnswer = getIntent().getStringExtra("aiAnswer");
        String category = getIntent().getStringExtra("category");
        boolean openHistoryPanel = getIntent().getBooleanExtra("openHistoryPanel", false);

        initViews();
        setupRecyclerView();
        setupVoiceButton();
        setupHistoryPanel();
        setupBackHandler();

        if (userQuestion != null && aiAnswer != null) {
            chatAdapter.addElderMessage(userQuestion);
            chatAdapter.addAiMessage(aiAnswer, category);
            ChatHistoryStore.addExchange(this, sessionId, userQuestion, aiAnswer, category);
            refreshHistorySessions();
            handler.postDelayed(() -> {
                VoiceManager.getInstance().getSynthesizer().speak(aiAnswer, null);
            }, 500);
        }

        if (openHistoryPanel) {
            handler.postDelayed(this::showHistoryPanel, 200);
        }
    }

    private void initViews() {
        rvChat = findViewById(R.id.rv_chat);
        voiceButton = findViewById(R.id.voice_button);
        thinkingIndicator = findViewById(R.id.thinking_indicator);
        tvThinkingDots = findViewById(R.id.tv_thinking_dots);
        btnBack = findViewById(R.id.btn_back);
        btnHistory = findViewById(R.id.btn_history);
        btnCloseHistory = findViewById(R.id.btn_close_history);
        historyScrim = findViewById(R.id.history_scrim);
        historyPanel = findViewById(R.id.history_panel);
        tvHistoryEmpty = findViewById(R.id.tv_history_empty);
        rvHistorySessions = findViewById(R.id.rv_history_sessions);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter();
        chatAdapter.setReplayListener(text -> {
            VoiceManager.getInstance().getSynthesizer().speak(text, null);
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvChat.setLayoutManager(layoutManager);
        rvChat.setAdapter(chatAdapter);
    }

    private void setupVoiceButton() {
        voiceButton.setVoiceCallback(new VoiceRecognizer.Callback() {
            @Override
            public void onResult(String text) {
                runOnUiThread(() -> {
                    chatAdapter.addElderMessage(text);
                    scrollToBottom();
                    sendChat(text);
                });
            }

            @Override
            public void onPartialResult(String text) {
            }

            @Override
            public void onError(String errorMsg) {
                runOnUiThread(() -> showToast("识别失败：" + errorMsg));
            }
        });
    }

    private void setupBackHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (historyPanel != null && historyPanel.getVisibility() == View.VISIBLE) {
                    hideHistoryPanel();
                    return;
                }
                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
            }
        });
    }

    private void setupHistoryPanel() {
        historyAdapter = new ChatHistoryAdapter();
        historyAdapter.setOnSessionClickListener(session -> {
            loadHistorySession(session);
            hideHistoryPanel();
        });
        rvHistorySessions.setLayoutManager(new LinearLayoutManager(this));
        rvHistorySessions.setAdapter(historyAdapter);

        btnHistory.setOnClickListener(v -> showHistoryPanel());
        btnCloseHistory.setOnClickListener(v -> hideHistoryPanel());
        historyScrim.setOnClickListener(v -> hideHistoryPanel());

        refreshHistorySessions();
    }

    private void sendChat(String text) {
        showThinking(true);

        Map<String, String> body = new HashMap<>();
        body.put("text", text);
        body.put("sessionId", sessionId);

        ApiClient.getInstance(this).getApi().chat(body).enqueue(new ApiCallback<ChatResponse>() {
            @Override
            public void onSuccess(ChatResponse data) {
                runOnUiThread(() -> {
                    showThinking(false);
                    chatAdapter.addAiMessage(data.getAnswer(), data.getCategory());
                    ChatHistoryStore.addExchange(ElderChatActivity.this, sessionId,
                            text, data.getAnswer(), data.getCategory());
                    refreshHistorySessions();
                    scrollToBottom();
                    handler.postDelayed(() -> {
                        VoiceManager.getInstance().getSynthesizer().speak(data.getAnswer(), null);
                    }, 300);
                });
            }

            public void onNetworkError(String errorMsg) {
                runOnUiThread(() -> {
                    showThinking(false);
                    showToast("对话失败：" + errorMsg);
                });
            }
        });
    }

    private void refreshHistorySessions() {
        if (historyAdapter == null) {
            return;
        }
        List<ChatHistorySession> sessions = ChatHistoryStore.getSessions(this);
        historyAdapter.setSessions(sessions);
        tvHistoryEmpty.setVisibility(sessions.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void loadHistorySession(ChatHistorySession session) {
        if (session == null) {
            return;
        }

        sessionId = session.getSessionId();
        chatAdapter.clearMessages();
        for (ChatHistorySession.Message message : session.getMessages()) {
            chatAdapter.addElderMessage(message.getUserQuestion());
            chatAdapter.addAiMessage(message.getAiAnswer(), message.getCategory());
        }
        scrollToBottom();
    }

    private void showHistoryPanel() {
        if (historyPanel.getVisibility() == View.VISIBLE) {
            return;
        }
        refreshHistorySessions();
        historyScrim.setVisibility(View.VISIBLE);
        historyPanel.setTranslationX(dpToPx(288));
        historyPanel.setVisibility(View.VISIBLE);
        historyPanel.post(() -> {
            historyPanel.setTranslationX(historyPanel.getWidth());
            historyPanel.animate().translationX(0f).setDuration(180).start();
        });
    }

    private void hideHistoryPanel() {
        if (historyPanel.getVisibility() != View.VISIBLE) {
            return;
        }
        historyPanel.animate()
                .translationX(historyPanel.getWidth())
                .setDuration(160)
                .withEndAction(() -> {
                    historyPanel.setVisibility(View.GONE);
                    historyScrim.setVisibility(View.GONE);
                    historyPanel.setTranslationX(0f);
                })
                .start();
    }

    private float dpToPx(int dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private void showThinking(boolean show) {
        if (show) {
            thinkingIndicator.setVisibility(View.VISIBLE);
            startThinkingAnimation();
        } else {
            thinkingIndicator.setVisibility(View.GONE);
            stopThinkingAnimation();
        }
    }

    private void startThinkingAnimation() {
        if (thinkingAnimator != null) {
            thinkingAnimator.cancel();
        }

        thinkingAnimator = ValueAnimator.ofInt(0, 3);
        thinkingAnimator.setDuration(1200);
        thinkingAnimator.setRepeatCount(ValueAnimator.INFINITE);
        thinkingAnimator.addUpdateListener(animation -> {
            int dotCount = (int) animation.getAnimatedValue();
            StringBuilder dots = new StringBuilder();
            for (int i = 0; i < dotCount; i++) {
                dots.append(".");
            }
            tvThinkingDots.setText(dots.toString());
        });
        thinkingAnimator.start();
    }

    private void stopThinkingAnimation() {
        if (thinkingAnimator != null) {
            thinkingAnimator.cancel();
            thinkingAnimator = null;
        }
    }

    private void scrollToBottom() {
        handler.post(() -> {
            if (chatAdapter.getItemCount() > 0) {
                rvChat.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
            }
        });
    }

    public void handleCorrectionPush(String senderName, String correctionText) {
        runOnUiThread(() -> {
            chatAdapter.addCorrectionMessage(senderName, correctionText);
            scrollToBottom();
            VoiceManager.getInstance().getSynthesizer().speak(
                "您" + senderName + "说，" + correctionText, null
            );
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopThinkingAnimation();
        VoiceManager.getInstance().getSynthesizer().stop();
    }
}
