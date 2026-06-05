package com.xiaoban.app.elder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

import com.xiaoban.app.R;
import com.xiaoban.app.base.BaseActivity;
import com.xiaoban.app.model.ChatResponse;
import com.xiaoban.app.model.Message;
import com.xiaoban.app.network.ApiCallback;
import com.xiaoban.app.network.ApiClient;
import com.xiaoban.app.util.PermissionUtil;
import com.xiaoban.app.util.TimeUtil;
import com.xiaoban.app.voice.VoiceManager;
import com.xiaoban.app.voice.VoiceRecognizer;
import com.xiaoban.app.widget.VoiceButton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ElderHomeActivity extends BaseActivity {

    private TextView tvDateWeather;
    private TextView tvDate;
    private TextView tvWeather;
    private View cardMessage;
    private TextView tvMessagePreview;
    private View unreadDot;
    private View voiceButtonCard;
    private TextView tvVoiceHint;
    private VoiceButton voiceButton;
    private View btnEmergency;
    private View welcomeBubble;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable messageCheckRunnable;

    private static final int REQUEST_CALL_PERMISSION = 100;
    private static final String EMERGENCY_PHONE = "10086";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elder_home);

        PermissionUtil.checkPermissions(this);
        VoiceManager.getInstance().init(this);

        initViews();
        setupListeners();
        updateDateWeather();
        loadLatestMessage();
        playWelcomeMessage();
        startMessagePolling();
    }

    private void initViews() {
        tvDate = findViewById(R.id.tv_date);
        tvWeather = findViewById(R.id.tv_weather);
        cardMessage = findViewById(R.id.card_message);
        tvMessagePreview = findViewById(R.id.tv_message_preview);
        unreadDot = findViewById(R.id.unread_dot);
        voiceButtonCard = findViewById(R.id.voice_button_card);
        tvVoiceHint = findViewById(R.id.tv_voice_hint);
        voiceButton = findViewById(R.id.voice_button);
        btnEmergency = findViewById(R.id.btn_emergency);
        welcomeBubble = findViewById(R.id.welcome_bubble);
    }

    private void setupListeners() {
        cardMessage.setOnClickListener(v -> {
            startActivity(new Intent(this, ElderMessageActivity.class));
        });

        voiceButton.setVoiceCallback(new VoiceRecognizer.Callback() {
            @Override
            public void onResult(String text) {
                runOnUiThread(() -> sendVoiceChat(text));
            }

            @Override
            public void onPartialResult(String text) {
            }

            @Override
            public void onError(String errorMsg) {
                runOnUiThread(() -> showToast("识别失败：" + errorMsg));
            }
        });

        voiceButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    voiceButtonCard.setScaleX(0.95f);
                    voiceButtonCard.setScaleY(0.95f);
                    tvVoiceHint.setText("正在听...");
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    voiceButtonCard.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                    tvVoiceHint.setText("按住说话");
                    break;
            }
            return false;
        });

        btnEmergency.setOnClickListener(v -> makeEmergencyCall());
    }

    private void updateDateWeather() {
        String dateStr = TimeUtil.getTodayDate();
        tvDate.setText(dateStr);
        tvWeather.setText("多云 18°C");
    }

    private void loadLatestMessage() {
        ApiClient.getInstance(this).getApi().getMessages(1, 1).enqueue(new ApiCallback<List<Message>>() {
            @Override
            public void onSuccess(List<Message> data) {
                runOnUiThread(() -> {
                    if (data != null && !data.isEmpty()) {
                        Message msg = data.get(0);
                        updateMessagePreview(msg);
                        unreadDot.setVisibility(msg.isRead() ? View.GONE : View.VISIBLE);
                    }
                });
            }
        });
    }

    private void updateMessagePreview(Message msg) {
        String preview = msg.getSenderName();
        switch (msg.getType()) {
            case "voice":
                preview += "发来一条语音消息";
                break;
            case "photo":
                preview += "发来了一张照片";
                break;
            case "text":
                preview += "说：" + msg.getContent();
                break;
        }
        tvMessagePreview.setText(preview);
    }

    private void playWelcomeMessage() {
        handler.postDelayed(() -> {
            welcomeBubble.setVisibility(View.VISIBLE);
            welcomeBubble.setAlpha(0f);
            welcomeBubble.animate().alpha(1f).setDuration(600).start();

            VoiceManager.getInstance().getSynthesizer().speak(
                "您好！我是小伴，有什么想聊的吗？", null
            );

            handler.postDelayed(() -> {
                welcomeBubble.animate().alpha(0f).setDuration(400).withEndAction(() -> {
                    welcomeBubble.setVisibility(View.GONE);
                }).start();
            }, 2500);
        }, 1000);
    }

    private void sendVoiceChat(String text) {
        String sessionId = UUID.randomUUID().toString();
        Map<String, String> body = new HashMap<>();
        body.put("text", text);
        body.put("sessionId", sessionId);

        ApiClient.getInstance(this).getApi().chat(body).enqueue(new ApiCallback<ChatResponse>() {
            @Override
            public void onSuccess(ChatResponse data) {
                runOnUiThread(() -> {
                    Intent intent = new Intent(ElderHomeActivity.this, ElderChatActivity.class);
                    intent.putExtra("userQuestion", text);
                    intent.putExtra("aiAnswer", data.getAnswer());
                    intent.putExtra("category", data.getCategory());
                    intent.putExtra("sessionId", sessionId);
                    startActivity(intent);
                });
            }
        });
    }

    private void makeEmergencyCall() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PERMISSION);
            return;
        }

        notifyEmergency();

        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + EMERGENCY_PHONE));
        startActivity(intent);
    }

    private void notifyEmergency() {
        Map<String, Object> body = new HashMap<>();
        ApiClient.getInstance(this).getApi().sendMessage(body).enqueue(new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
            }
        });
    }

    private void startMessagePolling() {
        messageCheckRunnable = new Runnable() {
            @Override
            public void run() {
                loadLatestMessage();
                handler.postDelayed(this, 30000);
            }
        };
        handler.postDelayed(messageCheckRunnable, 30000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VoiceManager.getInstance().destroy();
        if (messageCheckRunnable != null) {
            handler.removeCallbacks(messageCheckRunnable);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makeEmergencyCall();
            } else {
                showToast("需要拨号权限才能紧急呼叫");
            }
        }
    }
}
