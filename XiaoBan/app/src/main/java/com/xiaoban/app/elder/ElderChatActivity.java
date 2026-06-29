package com.xiaoban.app.elder;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xiaoban.app.R;
import com.xiaoban.app.base.BaseActivity;
import com.xiaoban.app.elder.adapter.ChatAdapter;
import com.xiaoban.app.model.ChatResponse;
import com.xiaoban.app.network.ApiCallback;
import com.xiaoban.app.network.ApiClient;
import com.xiaoban.app.voice.VoiceManager;
import com.xiaoban.app.voice.VoiceRecognizer;
import com.xiaoban.app.widget.VoiceButton;

import java.util.HashMap;
import java.util.Map;

public class ElderChatActivity extends BaseActivity {

    private RecyclerView rvChat;
    private ChatAdapter chatAdapter;
    private VoiceButton voiceButton;
    private View thinkingIndicator;
    private TextView tvThinkingDots;
    private View btnBack;

    private String sessionId;
    private Handler handler = new Handler(Looper.getMainLooper());
    private ValueAnimator thinkingAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elder_chat);

        sessionId = getIntent().getStringExtra("sessionId");
        String userQuestion = getIntent().getStringExtra("userQuestion");
        String aiAnswer = getIntent().getStringExtra("aiAnswer");
        String category = getIntent().getStringExtra("category");

        initViews();
        setupRecyclerView();
        setupVoiceButton();

        if (userQuestion != null && aiAnswer != null) {
            chatAdapter.addElderMessage(userQuestion);
            chatAdapter.addAiMessage(aiAnswer, category);
            handler.postDelayed(() -> {
                VoiceManager.getInstance().getSynthesizer().speak(aiAnswer, null);
            }, 500);
        }
    }

    private void initViews() {
        rvChat = findViewById(R.id.rv_chat);
        voiceButton = findViewById(R.id.voice_button);
        thinkingIndicator = findViewById(R.id.thinking_indicator);
        tvThinkingDots = findViewById(R.id.tv_thinking_dots);
        btnBack = findViewById(R.id.btn_back);

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
                    scrollToBottom();
                    handler.postDelayed(() -> {
                        VoiceManager.getInstance().getSynthesizer().speak(data.getAnswer(), null);
                    }, 300);
                });
            }

            @Override
            public void onBusinessError(int code, String message) {
                if (isFinishing()) return;
                runOnUiThread(() -> {
                    showThinking(false);
                    showToast(message);
                });
            }

            @Override
            public void onNetworkError(String errorMsg) {
                if (isFinishing()) return;
                runOnUiThread(() -> {
                    showThinking(false);
                    showToast("对话失败：" + errorMsg);
                });
            }
        });
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
