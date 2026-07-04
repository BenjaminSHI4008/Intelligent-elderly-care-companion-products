package com.xiaoban.app.elder;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.xiaoban.app.R;
import com.xiaoban.app.base.BaseActivity;
import com.xiaoban.app.elder.adapter.MessageAdapter;
import com.xiaoban.app.model.BindingRelationItem;
import com.xiaoban.app.model.Message;
import com.xiaoban.app.network.ApiCallback;
import com.xiaoban.app.network.ApiClient;
import com.xiaoban.app.util.FamilyMessageNotifier;
import com.xiaoban.app.util.ImageUrlUtil;
import com.xiaoban.app.voice.VoiceManager;
import com.xiaoban.app.voice.VoiceRecognizer;
import com.xiaoban.app.widget.VoiceButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElderMessageActivity extends BaseActivity implements FamilyMessageNotifier.Listener {

    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private VoiceButton voiceReplyButton;
    private View cardInputSwitch;
    private TextView tvInputSwitch;
    private TextView tvMemberCount;
    private TextView tvReplyTarget;
    private TextView tvEmptyState;
    private View btnMembers;
    private View panelVoiceReply;
    private View panelTextReply;
    private EditText etTextReply;
    private View btnSendTextReply;
    private View btnBack;
    private final List<BindingRelationItem> activeRelations = new ArrayList<>();
    private boolean announceUnreadOnLoad = true;
    private boolean textMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elder_message);

        initViews();
        setupRecyclerView();
        setupVoiceButton();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.rv_messages);
        voiceReplyButton = findViewById(R.id.voice_reply_button);
        cardInputSwitch = findViewById(R.id.card_input_switch);
        tvInputSwitch = findViewById(R.id.tv_input_switch);
        tvMemberCount = findViewById(R.id.tv_member_count);
        tvReplyTarget = findViewById(R.id.tv_reply_target);
        tvEmptyState = findViewById(R.id.tv_empty_state);
        btnMembers = findViewById(R.id.btn_members);
        panelVoiceReply = findViewById(R.id.panel_voice_reply);
        panelTextReply = findViewById(R.id.panel_text_reply);
        etTextReply = findViewById(R.id.et_text_reply);
        btnSendTextReply = findViewById(R.id.btn_send_text_reply);
        btnBack = findViewById(R.id.btn_back);

        btnBack.setOnClickListener(v -> finish());
        btnMembers.setOnClickListener(v -> showMembersDialog());
        cardInputSwitch.setOnClickListener(v -> applyInputMode(!textMode, true));
        btnSendTextReply.setOnClickListener(v -> sendTextReply());
        etTextReply.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendTextReply();
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFamilyMembers();
        loadMessages(announceUnreadOnLoad);
        announceUnreadOnLoad = false;
        FamilyMessageNotifier.addListener(this);
    }

    @Override
    protected void onPause() {
        FamilyMessageNotifier.removeListener(this);
        super.onPause();
    }

    @Override
    public void onFamilyMessageReceived() {
        if (isFinishing()) {
            return;
        }
        loadFamilyMembers();
        loadMessages(false);
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter();
        messageAdapter.setCurrentUserId(getCurrentUserId());
        messageAdapter.setOnMessageClickListener(new MessageAdapter.OnMessageClickListener() {
            @Override
            public void onMessageClick(Message message) {
                markAsRead(message);
            }

            @Override
            public void onVoiceClick(Message message) {
                playVoiceMessage(message);
            }

            @Override
            public void onPhotoClick(Message message) {
                showPhotoPreview(message);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);
    }

    private void setupVoiceButton() {
        voiceReplyButton.setRectangleIdleText("按住说话回复");
        voiceReplyButton.setVoiceCallback(new VoiceRecognizer.Callback() {
            @Override
            public void onResult(String text) {
                runOnUiThread(() -> sendReply(text));
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

    private void applyInputMode(boolean useTextMode, boolean allowKeyboard) {
        textMode = useTextMode;
        panelVoiceReply.setVisibility(textMode ? View.GONE : View.VISIBLE);
        panelTextReply.setVisibility(textMode ? View.VISIBLE : View.GONE);
        tvInputSwitch.setText(textMode ? "🎤 语音回复" : "✍ 手写/键盘回复");

        if (textMode && allowKeyboard) {
            etTextReply.requestFocus();
            showKeyboard();
        } else if (!textMode) {
            etTextReply.clearFocus();
            hideKeyboard();
        }
    }

    private void sendTextReply() {
        String reply = etTextReply.getText() == null
                ? ""
                : etTextReply.getText().toString().trim();
        if (reply.isEmpty()) {
            showToast("请输入要发到家庭聊天群的内容");
            return;
        }
        etTextReply.setText("");
        sendReply(reply);
    }

    private void loadFamilyMembers() {
        ApiClient.getInstance(this).getApi().getBindRelations().enqueue(new ApiCallback<List<BindingRelationItem>>() {
            @Override
            public void onSuccess(List<BindingRelationItem> data) {
                runOnUiThread(() -> {
                    activeRelations.clear();
                    if (data != null) {
                        for (BindingRelationItem relation : data) {
                            if ("active".equals(relation.getStatus())) {
                                activeRelations.add(relation);
                            }
                        }
                    }
                    updateFamilyHeader();
                });
            }

            @Override
            public void onNetworkError(String message) {
                runOnUiThread(() -> updateFamilyHeader());
            }
        });
    }

    private void loadMessages(boolean announceUnread) {
        ApiClient.getInstance(this).getApi().getFamilyGroupMessages(1, 50).enqueue(new ApiCallback<List<Message>>() {
            @Override
            public void onSuccess(List<Message> data) {
                runOnUiThread(() -> {
                    List<Message> messages = data != null ? data : new ArrayList<>();
                    messageAdapter.setMessages(messages);
                    tvEmptyState.setVisibility(messages.isEmpty() ? View.VISIBLE : View.GONE);
                    if (announceUnread) {
                        Message unread = findLatestUnreadIncoming(messages);
                        if (unread != null) {
                            playNewMessageNotification(unread);
                        }
                    }
                });
            }

            @Override
            public void onNetworkError(String message) {
                runOnUiThread(() -> {
                    tvEmptyState.setVisibility(View.VISIBLE);
                    showToast("家庭聊天群加载失败");
                });
            }
        });
    }

    private void updateFamilyHeader() {
        int count = activeRelations.size();
        tvMemberCount.setText(count > 0 ? "已绑定 " + count + " 位家人" : "暂未绑定家人");
        tvReplyTarget.setText(count > 0 ? "发到家庭聊天群" : "暂未绑定家人");
    }

    private void showMembersDialog() {
        if (activeRelations.isEmpty()) {
            showToast("暂未绑定家人");
            return;
        }

        ScrollView scrollView = new ScrollView(this);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int padding = dp(20);
        container.setPadding(padding, padding / 2, padding, padding / 2);
        scrollView.addView(container);

        for (BindingRelationItem relation : activeRelations) {
            TextView nameView = new TextView(this);
            nameView.setText(relation.getDisplayNameForElder());
            nameView.setTextColor(getResources().getColor(R.color.primary_dark));
            nameView.setTextSize(20);
            nameView.setTypeface(null, android.graphics.Typeface.BOLD);
            container.addView(nameView);

            TextView metaView = new TextView(this);
            String bindType = relation.getBindTypeLabel() != null && !relation.getBindTypeLabel().isEmpty()
                    ? relation.getBindTypeLabel()
                    : relation.getBindType();
            String phone = relation.getChildPhoneMasked() == null ? "" : " · " + relation.getChildPhoneMasked();
            metaView.setText((bindType == null || bindType.isEmpty() ? "绑定家人" : bindType) + phone);
            metaView.setTextColor(getResources().getColor(R.color.text_secondary));
            metaView.setTextSize(16);
            metaView.setPadding(0, 4, 0, 18);
            container.addView(metaView);
        }

        new AlertDialog.Builder(this)
                .setTitle("家庭成员")
                .setView(scrollView)
                .setPositiveButton("知道了", null)
                .show();
    }

    private void sendReply(String text) {
        String reply = normalizeReplyText(text);
        if (reply.isEmpty()) {
            showToast("没有识别到回复内容");
            return;
        }
        if (activeRelations.isEmpty()) {
            showToast("暂未绑定家人，无法回复");
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("msgType", "text");
        body.put("content", reply);

        ApiClient.getInstance(this).getApi().sendFamilyGroupMessage(body).enqueue(new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                runOnUiThread(() -> {
                    showToast("已发到家庭聊天群");
                    loadMessages(false);
                });
            }

            @Override
            public void onNetworkError(String errorMsg) {
                runOnUiThread(() -> showToast("发送失败：" + errorMsg));
            }
        });
    }

    private String normalizeReplyText(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replace('\u3000', ' ')
                .replace("\u200B", "")
                .replace("\uFEFF", "")
                .trim();
    }

    private void playVoiceMessage(Message message) {
        markAsRead(message);
        String text = message.getContent();
        if (text == null || text.trim().isEmpty()) {
            text = message.getSenderName() + "发来一条语音消息";
        }
        VoiceManager.getInstance().getSynthesizer().speak(text, null);
    }

    private void showPhotoPreview(Message message) {
        markAsRead(message);
        ImageView imageView = new ImageView(this);
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        int padding = dp(12);
        imageView.setPadding(padding, padding, padding, padding);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        if (message.getMediaUrl() != null && !message.getMediaUrl().isEmpty()) {
            Glide.with(this)
                    .load(ImageUrlUtil.resolve(message.getMediaUrl()))
                    .fitCenter()
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.bg_message_photo);
        }

        new AlertDialog.Builder(this)
                .setTitle(message.getSenderName() + "发来的图片")
                .setView(imageView)
                .setPositiveButton("关闭", null)
                .show();
    }

    private Message findLatestUnreadIncoming(List<Message> messages) {
        long currentUserId = getCurrentUserId();
        for (Message message : messages) {
            if (!message.isRead() && message.getReceiverId() == currentUserId) {
                return message;
            }
        }
        return null;
    }

    private void markAsRead(Message message) {
        if (!message.isRead() && message.getReceiverId() == getCurrentUserId()) {
            message.setRead(true);
            messageAdapter.notifyDataSetChanged();
            ApiClient.getInstance(this).getApi().markRead(message.getId()).enqueue(new ApiCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                }
            });
        }
    }

    private void playNewMessageNotification(Message message) {
        String notification = message.getSenderName();
        switch (message.getType()) {
            case "voice":
                notification += "发来了一条语音消息";
                break;
            case "photo":
                notification += "发来了一张照片";
                break;
            case "text":
                notification += "说：" + message.getContent();
                break;
        }
        VoiceManager.getInstance().getSynthesizer().speak(notification, null);
    }

    public void handleNewMessagePush(Message message) {
        loadFamilyMembers();
        loadMessages(false);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void showKeyboard() {
        etTextReply.postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(etTextReply, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 120);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(etTextReply.getWindowToken(), 0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VoiceManager.getInstance().getSynthesizer().stop();
    }
}
