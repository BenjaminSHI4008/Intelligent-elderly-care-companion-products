package com.xiaoban.app.elder;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xiaoban.app.R;
import com.xiaoban.app.base.BaseActivity;
import com.xiaoban.app.network.ApiCallback;
import com.xiaoban.app.network.ApiClient;
import com.xiaoban.app.util.ImageUrlUtil;
import com.xiaoban.app.voice.VoiceManager;

public class ElderMessageDetailActivity extends BaseActivity {

    private View btnBack;
    private TextView tvSenderInitial;
    private TextView tvSenderName;
    private TextView tvMessageTime;
    private TextView tvMessageType;
    private TextView tvMessageContent;
    private TextView tvPlayHint;
    private ImageView ivPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elder_message_detail);

        initViews();
        bindMessage();
        markMessageReadIfNeeded();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tvSenderInitial = findViewById(R.id.tv_sender_initial);
        tvSenderName = findViewById(R.id.tv_sender_name);
        tvMessageTime = findViewById(R.id.tv_message_time);
        tvMessageType = findViewById(R.id.tv_message_type);
        tvMessageContent = findViewById(R.id.tv_message_content);
        tvPlayHint = findViewById(R.id.tv_play_hint);
        ivPhoto = findViewById(R.id.iv_photo);

        btnBack.setOnClickListener(v -> finish());
    }

    private void bindMessage() {
        String senderName = getIntent().getStringExtra("senderName");
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
