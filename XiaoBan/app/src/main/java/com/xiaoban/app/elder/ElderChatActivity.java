package com.xiaoban.app.elder;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xiaoban.app.R;
import com.xiaoban.app.base.BaseActivity;
import com.xiaoban.app.base.Constants;
import com.xiaoban.app.elder.adapter.ChatAdapter;
import com.xiaoban.app.elder.adapter.ChatHistoryAdapter;
import com.xiaoban.app.model.ChatResponse;
import com.xiaoban.app.model.ChatHistorySession;
import com.xiaoban.app.network.ApiCallback;
import com.xiaoban.app.network.ApiClient;
import com.xiaoban.app.util.ChatHistoryStore;
import com.xiaoban.app.util.SharedPrefUtil;
import com.xiaoban.app.voice.VoiceManager;
import com.xiaoban.app.voice.VoiceRecognizer;
import com.xiaoban.app.widget.VoiceButton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ElderChatActivity extends BaseActivity {

    private static final String INPUT_MODE_VOICE = "voice";
    private static final String INPUT_MODE_TEXT = "text";
    private static final long EMPTY_STATE_CAROUSEL_INTERVAL_MS = 5000L;
    private static final long EMPTY_STATE_SLIDE_DURATION_MS = 260L;

    private RecyclerView rvChat;
    private ChatAdapter chatAdapter;
    private VoiceButton voiceButton;
    private View cardInputSwitch;
    private TextView tvInputSwitch;
    private View panelVoiceInput;
    private View panelTextInput;
    private EditText etTextMessage;
    private View btnSendText;
    private View thinkingIndicator;
    private TextView tvThinkingDots;
    private View btnBack;
    private View btnNewChat;
    private View btnHistory;
    private View btnCloseHistory;
    private View historyScrim;
    private View historyPanel;
    private View emptyChatState;
    private View emptyChatContent;
    private View dotEmptyNewChat;
    private View dotEmptyHistory;
    private ImageView ivEmptyChatIcon;
    private TextView tvEmptyChatTitle;
    private TextView tvEmptyChatDesc;
    private TextView tvHistoryEmpty;
    private RecyclerView rvHistorySessions;
    private ChatHistoryAdapter historyAdapter;

    private String sessionId;
    private String inputMode = INPUT_MODE_VOICE;
    private boolean isWaitingForAiResponse = false;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean emptyStateShowingHistoryIntro = false;
    private boolean emptyStateCarouselActive = false;
    private float emptyStateDragStartX = 0f;
    private boolean emptyStateDragging = false;
    private boolean emptyStateDragMoved = false;
    private final Runnable emptyStateCarouselRunnable = new Runnable() {
        @Override
        public void run() {
            if (!emptyStateCarouselActive || emptyChatState.getVisibility() != View.VISIBLE) {
                return;
            }
            slideToEmptyStatePage(!emptyStateShowingHistoryIntro);
            handler.postDelayed(this, EMPTY_STATE_CAROUSEL_INTERVAL_MS);
        }
    };
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
        setupInputMode(openHistoryPanel);
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

        updateEmptyState();

        if (openHistoryPanel) {
            handler.postDelayed(this::showHistoryPanel, 200);
        }
    }

    private void initViews() {
        rvChat = findViewById(R.id.rv_chat);
        voiceButton = findViewById(R.id.voice_button);
        cardInputSwitch = findViewById(R.id.card_input_switch);
        tvInputSwitch = findViewById(R.id.tv_input_switch);
        panelVoiceInput = findViewById(R.id.panel_voice_input);
        panelTextInput = findViewById(R.id.panel_text_input);
        etTextMessage = findViewById(R.id.et_text_message);
        btnSendText = findViewById(R.id.btn_send_text);
        thinkingIndicator = findViewById(R.id.thinking_indicator);
        tvThinkingDots = findViewById(R.id.tv_thinking_dots);
        btnBack = findViewById(R.id.btn_back);
        btnNewChat = findViewById(R.id.btn_new_chat);
        btnHistory = findViewById(R.id.btn_history);
        btnCloseHistory = findViewById(R.id.btn_close_history);
        historyScrim = findViewById(R.id.history_scrim);
        historyPanel = findViewById(R.id.history_panel);
        emptyChatState = findViewById(R.id.empty_chat_state);
        emptyChatContent = findViewById(R.id.empty_chat_content);
        dotEmptyNewChat = findViewById(R.id.dot_empty_new_chat);
        dotEmptyHistory = findViewById(R.id.dot_empty_history);
        ivEmptyChatIcon = findViewById(R.id.iv_empty_chat_icon);
        tvEmptyChatTitle = findViewById(R.id.tv_empty_chat_title);
        tvEmptyChatDesc = findViewById(R.id.tv_empty_chat_desc);
        tvHistoryEmpty = findViewById(R.id.tv_history_empty);
        rvHistorySessions = findViewById(R.id.rv_history_sessions);

        btnBack.setOnClickListener(v -> finish());
        btnNewChat.setOnClickListener(v -> startNewChat());
        setupEmptyStateDrag();
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
                    if (isWaitingForAiResponse) {
                        showToast("小伴正在回复，请稍等");
                        return;
                    }
                    String question = text == null ? "" : text.trim();
                    if (!hasMeaningfulText(question)) {
                        showToast("没有识别到语音内容");
                        return;
                    }
                    chatAdapter.addElderMessage(question);
                    updateEmptyState();
                    scrollToBottom();
                    sendChat(question);
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

    private void setupInputMode(boolean suppressKeyboard) {
        cardInputSwitch.setOnClickListener(v -> {
            String nextMode = INPUT_MODE_TEXT.equals(inputMode) ? INPUT_MODE_VOICE : INPUT_MODE_TEXT;
            applyInputMode(nextMode, true, true);
        });

        btnSendText.setOnClickListener(v -> sendTextMessage());
        etTextMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendTextMessage();
                return true;
            }
            return false;
        });

        String savedMode = SharedPrefUtil.getString(this,
                Constants.SP_ELDER_CHAT_INPUT_MODE, INPUT_MODE_VOICE);
        if (!INPUT_MODE_TEXT.equals(savedMode)) {
            savedMode = INPUT_MODE_VOICE;
        }
        applyInputMode(savedMode, false, !suppressKeyboard);
    }

    private void applyInputMode(String mode, boolean persist, boolean allowKeyboard) {
        boolean textMode = INPUT_MODE_TEXT.equals(mode);
        inputMode = textMode ? INPUT_MODE_TEXT : INPUT_MODE_VOICE;

        panelVoiceInput.setVisibility(textMode ? View.GONE : View.VISIBLE);
        panelTextInput.setVisibility(textMode ? View.VISIBLE : View.GONE);
        tvInputSwitch.setText(textMode ? "🎤 语音输入" : "✍ 手写/键盘输入");

        if (persist) {
            SharedPrefUtil.putString(this, Constants.SP_ELDER_CHAT_INPUT_MODE, inputMode);
        }

        if (textMode && allowKeyboard) {
            etTextMessage.requestFocus();
            showKeyboard();
        } else if (!textMode) {
            etTextMessage.clearFocus();
            hideKeyboard();
        }
    }

    private void sendTextMessage() {
        if (isWaitingForAiResponse) {
            showToast("小伴正在回复，请稍等");
            return;
        }

        String question = etTextMessage.getText() == null
                ? ""
                : etTextMessage.getText().toString().trim();
        if (!hasMeaningfulText(question)) {
            showToast("请输入要问小伴的话");
            return;
        }

        etTextMessage.setText("");
        chatAdapter.addElderMessage(question);
        updateEmptyState();
        scrollToBottom();
        sendChat(question);
    }

    private void setupBackHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (historyPanel != null && historyPanel.getVisibility() == View.VISIBLE) {
                    hideHistoryPanel();
                    return;
                }
                if (INPUT_MODE_TEXT.equals(inputMode) && etTextMessage.hasFocus()) {
                    etTextMessage.clearFocus();
                    hideKeyboard();
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
            if (isWaitingForAiResponse) {
                showToast("小伴正在回复，请稍等");
                return;
            }
            loadHistorySession(session);
            hideHistoryPanel();
        });
        historyAdapter.setOnSessionDeleteListener(this::showDeleteHistoryConfirmDialog);
        rvHistorySessions.setLayoutManager(new LinearLayoutManager(this));
        rvHistorySessions.setAdapter(historyAdapter);

        btnHistory.setOnClickListener(v -> showHistoryPanel());
        btnCloseHistory.setOnClickListener(v -> hideHistoryPanel());
        historyScrim.setOnClickListener(v -> hideHistoryPanel());

        refreshHistorySessions();
    }

    private void showDeleteHistoryConfirmDialog(ChatHistorySession session) {
        if (isWaitingForAiResponse) {
            showToast("小伴正在回复，请稍等");
            return;
        }
        if (session == null) {
            return;
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("确认删除")
                .setMessage("确定删除这条聊天记录吗？删除后不能恢复。")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", (d, which) -> deleteHistorySession(session))
                .create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(getColor(R.color.urgent_red)));
        dialog.show();
    }

    private void deleteHistorySession(ChatHistorySession session) {
        if (isWaitingForAiResponse) {
            showToast("小伴正在回复，请稍等");
            return;
        }
        if (session == null) {
            return;
        }

        boolean deleted = ChatHistoryStore.deleteSession(this, session.getSessionId());
        if (!deleted) {
            showToast("删除失败，请稍后再试");
            return;
        }

        if (session.getSessionId().equals(sessionId)) {
            sessionId = UUID.randomUUID().toString();
            chatAdapter.clearMessages();
            etTextMessage.setText("");
            etTextMessage.clearFocus();
            hideKeyboard();
            VoiceManager.getInstance().getSynthesizer().stop();
            updateEmptyState();
        }

        refreshHistorySessions();
        showToast("已删除聊天记录");
    }

    private void startNewChat() {
        if (isWaitingForAiResponse) {
            showToast("小伴正在回复，请稍等");
            return;
        }

        sessionId = UUID.randomUUID().toString();
        chatAdapter.clearMessages();
        etTextMessage.setText("");
        etTextMessage.clearFocus();
        hideKeyboard();
        hideHistoryPanel();
        VoiceManager.getInstance().getSynthesizer().stop();
        updateEmptyState();
        showToast("已开始新的聊天");
    }

    private void sendChat(String text) {
        String question = text == null ? "" : text.trim();
        if (!hasMeaningfulText(question)) {
            showToast("请输入要问小伴的话");
            return;
        }

        isWaitingForAiResponse = true;
        showThinking(true);

        Map<String, String> body = new HashMap<>();
        body.put("text", question);
        body.put("sessionId", sessionId);

        ApiClient.getInstance(this).getApi().chat(body).enqueue(new ApiCallback<ChatResponse>() {
            @Override
            public void onSuccess(ChatResponse data) {
                runOnUiThread(() -> {
                    isWaitingForAiResponse = false;
                    showThinking(false);
                    chatAdapter.addAiMessage(data.getAnswer(), data.getCategory());
                    ChatHistoryStore.addExchange(ElderChatActivity.this, sessionId,
                            question, data.getAnswer(), data.getCategory());
                    refreshHistorySessions();
                    updateEmptyState();
                    scrollToBottom();
                    handler.postDelayed(() -> {
                        VoiceManager.getInstance().getSynthesizer().speak(data.getAnswer(), null);
                    }, 300);
                });
            }

            @Override
            public void onBusinessError(int code, String message) {
                runOnUiThread(() -> {
                    isWaitingForAiResponse = false;
                    showThinking(false);
                    showToast(message == null || message.isEmpty() ? "对话失败，请稍后再试" : message);
                });
            }

            @Override
            public void onNetworkError(String errorMsg) {
                runOnUiThread(() -> {
                    isWaitingForAiResponse = false;
                    showThinking(false);
                    showToast("对话失败，请检查网络");
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
        updateEmptyState();
        scrollToBottom();
    }

    private void updateEmptyState() {
        if (emptyChatState == null || chatAdapter == null) {
            return;
        }
        boolean shouldShow = chatAdapter.getItemCount() == 0;
        emptyChatState.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
        if (shouldShow) {
            startEmptyStateCarousel();
        } else {
            stopEmptyStateCarousel();
        }
    }

    private void startEmptyStateCarousel() {
        handler.removeCallbacks(emptyStateCarouselRunnable);
        emptyStateCarouselActive = true;
        emptyStateShowingHistoryIntro = false;
        applyEmptyStateContent();
        resetEmptyStateContentPosition();
        scheduleEmptyStateCarousel();
    }

    private void stopEmptyStateCarousel() {
        emptyStateCarouselActive = false;
        handler.removeCallbacks(emptyStateCarouselRunnable);
        if (emptyChatContent != null) {
            emptyChatContent.animate().cancel();
            resetEmptyStateContentPosition();
        }
        emptyStateShowingHistoryIntro = false;
    }

    private void slideToEmptyStatePage(boolean showHistoryIntro) {
        slideToEmptyStatePage(showHistoryIntro, -1);
    }

    private void slideToEmptyStatePage(boolean showHistoryIntro, int direction) {
        if (emptyChatContent == null || emptyChatContent.getWidth() == 0) {
            emptyStateShowingHistoryIntro = showHistoryIntro;
            applyEmptyStateContent();
            resetEmptyStateContentPosition();
            return;
        }

        float width = emptyChatContent.getWidth();
        int slideDirection = direction < 0 ? -1 : 1;
        DecelerateInterpolator interpolator = new DecelerateInterpolator();
        emptyChatContent.animate().cancel();
        emptyChatContent.animate()
                .translationX(slideDirection * width)
                .alpha(0f)
                .setDuration(EMPTY_STATE_SLIDE_DURATION_MS)
                .setInterpolator(interpolator)
                .withEndAction(() -> {
                    if (!emptyStateCarouselActive || emptyChatState.getVisibility() != View.VISIBLE) {
                        return;
                    }
                    emptyStateShowingHistoryIntro = showHistoryIntro;
                    applyEmptyStateContent();
                    emptyChatContent.setTranslationX(-slideDirection * width);
                    emptyChatContent.setAlpha(0f);
                    emptyChatContent.animate()
                            .translationX(0f)
                            .alpha(1f)
                            .setDuration(EMPTY_STATE_SLIDE_DURATION_MS)
                            .setInterpolator(interpolator)
                            .start();
                })
                .start();
    }

    private void setupEmptyStateDrag() {
        if (emptyChatState == null || emptyChatContent == null) {
            return;
        }

        emptyChatState.setOnTouchListener((v, event) -> {
            if (emptyChatState.getVisibility() != View.VISIBLE || chatAdapter.getItemCount() > 0) {
                return false;
            }

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    emptyStateDragStartX = event.getRawX();
                    emptyStateDragging = true;
                    emptyStateDragMoved = false;
                    handler.removeCallbacks(emptyStateCarouselRunnable);
                    emptyChatContent.animate().cancel();
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (!emptyStateDragging) {
                        return true;
                    }
                    float dx = event.getRawX() - emptyStateDragStartX;
                    if (Math.abs(dx) > dpToPx(6)) {
                        emptyStateDragMoved = true;
                    }
                    applyEmptyStateDragOffset(dx);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (!emptyStateDragging) {
                        return true;
                    }
                    float finalDx = event.getRawX() - emptyStateDragStartX;
                    finishEmptyStateDrag(finalDx);
                    emptyStateDragging = false;
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    return true;
                default:
                    return true;
            }
        });
    }

    private void applyEmptyStateDragOffset(float dx) {
        if (emptyChatContent == null) {
            return;
        }
        float width = Math.max(1f, emptyChatContent.getWidth());
        float clampedDx = Math.max(-width, Math.min(width, dx));
        float dragProgress = Math.min(1f, Math.abs(clampedDx) / width);
        emptyChatContent.setTranslationX(clampedDx);
        emptyChatContent.setAlpha(1f - dragProgress * 0.45f);
    }

    private void finishEmptyStateDrag(float dx) {
        if (emptyChatContent == null) {
            return;
        }

        float threshold = Math.max(dpToPx(48), emptyChatContent.getWidth() * 0.18f);
        if (emptyStateDragMoved && Math.abs(dx) >= threshold) {
            int direction = dx < 0 ? -1 : 1;
            slideToEmptyStatePage(!emptyStateShowingHistoryIntro, direction);
        } else {
            emptyChatContent.animate()
                    .translationX(0f)
                    .alpha(1f)
                    .setDuration(EMPTY_STATE_SLIDE_DURATION_MS)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }
        scheduleEmptyStateCarousel();
    }

    private void scheduleEmptyStateCarousel() {
        handler.removeCallbacks(emptyStateCarouselRunnable);
        if (emptyStateCarouselActive && emptyChatState.getVisibility() == View.VISIBLE) {
            handler.postDelayed(emptyStateCarouselRunnable, EMPTY_STATE_CAROUSEL_INTERVAL_MS);
        }
    }

    private void resetEmptyStateContentPosition() {
        if (emptyChatContent == null) {
            return;
        }
        emptyChatContent.setTranslationX(0f);
        emptyChatContent.setAlpha(1f);
    }

    private void applyEmptyStateContent() {
        if (ivEmptyChatIcon == null || tvEmptyChatTitle == null || tvEmptyChatDesc == null) {
            return;
        }

        if (emptyStateShowingHistoryIntro) {
            ivEmptyChatIcon.setImageResource(R.drawable.ic_history);
            tvEmptyChatTitle.setText("聊天记录");
            tvEmptyChatDesc.setText("点右上角时钟，可以查看以前和小伴聊过的内容");
        } else {
            ivEmptyChatIcon.setImageResource(R.drawable.ic_new_chat);
            tvEmptyChatTitle.setText("新的聊天");
            tvEmptyChatDesc.setText("按住说话，或点上方手写/键盘输入");
        }
        updateEmptyStateDots();
    }

    private void updateEmptyStateDots() {
        if (dotEmptyNewChat == null || dotEmptyHistory == null) {
            return;
        }
        dotEmptyNewChat.setBackgroundResource(emptyStateShowingHistoryIntro
                ? R.drawable.bg_empty_dot_inactive
                : R.drawable.bg_empty_dot_active);
        dotEmptyHistory.setBackgroundResource(emptyStateShowingHistoryIntro
                ? R.drawable.bg_empty_dot_active
                : R.drawable.bg_empty_dot_inactive);
    }

    private void showHistoryPanel() {
        if (historyPanel.getVisibility() == View.VISIBLE) {
            return;
        }
        if (INPUT_MODE_TEXT.equals(inputMode)) {
            etTextMessage.clearFocus();
            hideKeyboard();
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

    private void showKeyboard() {
        handler.postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(etTextMessage, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 120);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(etTextMessage.getWindowToken(), 0);
        }
    }

    private boolean hasMeaningfulText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        String value = text.trim();
        for (int offset = 0; offset < value.length(); ) {
            int codePoint = value.codePointAt(offset);
            if (Character.isLetterOrDigit(codePoint)) {
                return true;
            }
            offset += Character.charCount(codePoint);
        }
        return false;
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
            updateEmptyState();
            scrollToBottom();
            VoiceManager.getInstance().getSynthesizer().speak(
                "您" + senderName + "说，" + correctionText, null
            );
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopEmptyStateCarousel();
        stopThinkingAnimation();
        VoiceManager.getInstance().getSynthesizer().stop();
    }
}
