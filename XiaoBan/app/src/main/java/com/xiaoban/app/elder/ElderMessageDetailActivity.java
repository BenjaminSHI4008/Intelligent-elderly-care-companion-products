package com.xiaoban.app.elder;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xiaoban.app.R;
import com.xiaoban.app.base.BaseActivity;
import com.xiaoban.app.network.ApiCallback;
import com.xiaoban.app.network.ApiClient;
import com.xiaoban.app.util.ImageUrlUtil;
import com.xiaoban.app.voice.VoiceManager;
import com.xiaoban.app.voice.VoiceRecognizer;
import com.xiaoban.app.widget.VoiceButton;

import java.util.HashMap;
import java.util.Map;

public class ElderMessageDetailActivity extends BaseActivity {

    private View btnBack;
    private View cardInputSwitch;
    private TextView tvInputSwitch;
    private TextView tvSenderInitial;
    private TextView tvSenderName;
    private TextView tvMessageTime;
    private TextView tvMessageType;
    private TextView tvMessageContent;
    private TextView tvPlayHint;
    private TextView tvReplyTarget;
    private View panelVoiceReply;
    private View panelTextReply;
    private VoiceButton voiceReplyButton;
    private EditText etTextReply;
    private View btnSendTextReply;
    private ImageView ivPhoto;
    private long senderId = -1L;
    private String senderName = "家人";
    private boolean textMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elder_message_detail);

        initViews();
        bindMessage();
        setupReplyControls();
        markMessageReadIfNeeded();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        cardInputSwitch = findViewById(R.id.card_input_switch);
        tvInputSwitch = findViewById(R.id.tv_input_switch);
        tvSenderInitial = findViewById(R.id.tv_sender_initial);
        tvSenderName = findViewById(R.id.tv_sender_name);
        tvMessageTime = findViewById(R.id.tv_message_time);
        tvMessageType = findViewById(R.id.tv_message_type);
        tvMessageContent = findViewById(R.id.tv_message_content);
        tvPlayHint = findViewById(R.id.tv_play_hint);
        tvReplyTarget = findViewById(R.id.tv_reply_target);
        panelVoiceReply = findViewById(R.id.panel_voice_reply);
        panelTextReply = findViewById(R.id.panel_text_reply);
        voiceReplyButton = findViewById(R.id.voice_reply_button);
        etTextReply = findViewById(R.id.et_text_reply);
        btnSendTextReply = findViewById(R.id.btn_send_text_reply);
        ivPhoto = findViewById(R.id.iv_photo);

        btnBack.setOnClickListener(v -> finish());
    }

    private void bindMessage() {
        senderId = getIntent().getLongExtra("senderId", -1L);
        senderName = getIntent().getStringExtra("senderName");
        String messageType = getIntent().getStringExtra("messageType");
        String content = getIntent().getStringExtra("content");
        String mediaUrl = getIntent().getStringExtra("mediaUrl");
        String time = getIntent().getStringExtra("time");
        int duration = getIntent().getIntExtra("duration", 0);

        if (senderName == null || senderName.isEmpty()) {
            senderName = "家人";
        }
        if (content == null || content.isEmpty()) {
            content = "收到一条家人消息";
        }

        tvSenderName.setText(senderName);
        tvSenderInitial.setText(senderName.substring(0, 1));
        tvMessageTime.setText(time == null ? "" : time);
        tvReplyTarget.setText("回复 " + senderName);

        switch (messageType == null ? "text" : messageType) {
            case "voice":
                tvMessageType.setText("语音消息");
                tvMessageContent.setText(senderName + "发来一条语音消息，时长 " + formatDuration(duration));
                tvPlayHint.setVisibility(View.VISIBLE);
                tvPlayHint.setText("🔊 点击播放 " + formatDuration(duration) + " ▶");
                String voiceText = content;
                tvPlayHint.setOnClickListener(v -> VoiceManager.getInstance().getSynthesizer().speak(voiceText, null));
                break;
            case "photo":
                tvMessageType.setText("照片消息");
                tvMessageContent.setText(content);
                ivPhoto.setVisibility(View.VISIBLE);
                if (mediaUrl != null && !mediaUrl.isEmpty()) {
                    Glide.with(this)
                            .load(ImageUrlUtil.resolve(mediaUrl))
                            .centerCrop()
                            .into(ivPhoto);
                } else {
                    ivPhoto.setImageResource(R.drawable.bg_message_photo);
                }
                break;
            default:
                tvMessageType.setText("文字消息");
                tvMessageContent.setText(content);
                break;
        }
    }

    private void setupReplyControls() {
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
            showToast("请输入要回复家人的内容");
            return;
        }
        etTextReply.setText("");
        sendReply(reply);
    }

    private void sendReply(String text) {
        String reply = text == null ? "" : text.trim();
        if (reply.isEmpty()) {
            showToast("没有识别到回复内容");
            return;
        }
        if (senderId <= 0) {
            showToast("无法确定回复对象");
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("receiverId", senderId);
        body.put("msgType", "text");
        body.put("content", reply);

        ApiClient.getInstance(this).getApi().sendMessage(body).enqueue(new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                runOnUiThread(() -> showToast("已发送回复"));
            }

            @Override
            public void onNetworkError(String errorMsg) {
                runOnUiThread(() -> showToast("发送失败：" + errorMsg));
            }
        });
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

    private void markMessageReadIfNeeded() {
        long messageId = getIntent().getLongExtra("messageId", -1);
        if (messageId <= 0) {
            return;
        }
        ApiClient.getInstance(this).getApi().markRead(messageId).enqueue(new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
            }
        });
    }

    private String formatDuration(int seconds) {
        int min = seconds / 60;
        int sec = seconds % 60;
        return String.format("%d:%02d", min, sec);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VoiceManager.getInstance().getSynthesizer().stop();
    }
}
