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
import com.xiaoban.app.base.Constants;
import com.xiaoban.app.location.LocationHelper;
import com.xiaoban.app.model.BindingRelationItem;
import com.xiaoban.app.model.ChatResponse;
import com.xiaoban.app.model.Message;
import com.xiaoban.app.model.User;
import com.xiaoban.app.model.WeatherInfo;
import com.xiaoban.app.network.ApiCallback;
import com.xiaoban.app.network.ApiClient;
import com.xiaoban.app.util.BindNotificationHelper;
import com.xiaoban.app.util.FamilyMessageNotifier;
import com.xiaoban.app.util.PermissionUtil;
import com.xiaoban.app.util.SharedPrefUtil;
import com.xiaoban.app.util.TimeUtil;
import com.xiaoban.app.voice.VoiceManager;
import com.xiaoban.app.voice.VoiceRecognizer;
import com.xiaoban.app.widget.VoiceButton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ElderHomeActivity extends BaseActivity implements FamilyMessageNotifier.Listener {

    private TextView tvDate;
    private TextView tvWeather;
    private View cardMessage;
    private View cardBind;
    private View cardAiChatHistory;
    private TextView tvBindHint;
    private TextView tvMessagePreview;
    private View unreadDot;
    private View voiceButtonCard;
    private TextView tvVoiceHint;
    private VoiceButton voiceButton;
    private View btnEmergency;
    private View btnProfile;
    private TextView tvProfileInitial;
    private View welcomeBubble;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable messageCheckRunnable;
    private boolean bindBaselineInitialized = false;

    private static final int REQUEST_CALL_PERMISSION = 100;
    private static final String DEFAULT_EMERGENCY_PHONE = "120";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elder_home);

        PermissionUtil.checkPermissions(this);

        initViews();
        setupListeners();
        loadProfileCache();
        updateDateWeather();
        loadLatestMessage();
        loadBindStatus(false);
        playWelcomeMessage();
        startMessagePolling();
    }

    private void initViews() {
        tvDate = findViewById(R.id.tv_date);
        tvWeather = findViewById(R.id.tv_weather);
        cardMessage = findViewById(R.id.card_message);
        cardBind = findViewById(R.id.card_bind);
        cardAiChatHistory = findViewById(R.id.card_ai_chat_history);
        tvBindHint = findViewById(R.id.tv_bind_hint);
        tvMessagePreview = findViewById(R.id.tv_message_preview);
        unreadDot = findViewById(R.id.unread_dot);
        voiceButtonCard = findViewById(R.id.voice_button_card);
        tvVoiceHint = findViewById(R.id.tv_voice_hint);
        voiceButton = findViewById(R.id.voice_button);
        btnEmergency = findViewById(R.id.btn_emergency);
        btnProfile = findViewById(R.id.btn_profile);
        tvProfileInitial = findViewById(R.id.tv_profile_initial);
        welcomeBubble = findViewById(R.id.welcome_bubble);
    }

    private void setupListeners() {
        btnProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ElderProfileActivity.class)));

        cardMessage.setOnClickListener(v ->
                startActivity(new Intent(this, ElderMessageActivity.class)));

        cardBind.setOnClickListener(v ->
                startActivity(new Intent(this, ElderBindActivity.class)));

        cardAiChatHistory.setOnClickListener(v -> openAiChatHistory());

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

    private void updateProfileInitial() {
        String nickname = SharedPrefUtil.getString(this, Constants.SP_NICKNAME, "");
        String displayName = nickname == null || nickname.isEmpty() ? "伴" : nickname;
        tvProfileInitial.setText(displayName.substring(0, 1));
    }

    private void loadProfileCache() {
        ApiClient.getInstance(this).getApi().getProfile().enqueue(new ApiCallback<User>() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    SharedPrefUtil.putString(ElderHomeActivity.this, Constants.SP_PHONE, user.getPhone());
                    SharedPrefUtil.putString(ElderHomeActivity.this, Constants.SP_NICKNAME, user.getNickname());
                    SharedPrefUtil.putString(ElderHomeActivity.this, Constants.SP_GENDER, user.getGender());
                    SharedPrefUtil.putString(ElderHomeActivity.this, Constants.SP_BIRTHDAY, user.getBirthday());
                    SharedPrefUtil.putString(ElderHomeActivity.this, Constants.SP_EMERGENCY_CONTACT, user.getEmergencyContact());
                    updateProfileInitial();
                });
            }

            @Override
            public void onBusinessError(int code, String message) {
            }

            @Override
            public void onNetworkError(String message) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateProfileInitial();
        loadBindStatus(true);
        loadLatestMessage();
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
        loadLatestMessage();
    }

    private void updateDateWeather() {
        tvDate.setText(TimeUtil.getTodayDate());
        tvWeather.setText("天气加载中...");

        loadWeather(null);

        LocationHelper.getCurrentAdCode(this, new LocationHelper.LocationCallback() {
            @Override
            public void onSuccess(String adCode) {
                loadWeather(adCode);
            }

            @Override
            public void onError(String message) {
                // 已用默认城市加载
            }
        });
    }

    private void loadWeather(String cityCode) {
        ApiClient.getInstance(this).getApi().getCurrentWeather(cityCode)
                .enqueue(new ApiCallback<WeatherInfo>() {
                    @Override
                    public void onSuccess(WeatherInfo data) {
                        runOnUiThread(() -> {
                            if (data != null) {
                                tvWeather.setText(data.getCity() + " · "
                                        + data.getWeather() + " "
                                        + data.getTemperature() + "°C");
                            } else {
                                tvWeather.setText("天气暂不可用");
                            }
                        });
                    }

                    @Override
                    public void onBusinessError(int code, String message) {
                        runOnUiThread(() -> tvWeather.setText("天气暂不可用"));
                    }

                    @Override
                    public void onNetworkError(String message) {
                        runOnUiThread(() -> tvWeather.setText("天气暂不可用"));
                    }
                });
    }

    private void loadBindStatus(boolean announce) {
        ApiClient.getInstance(this).getApi().getBindRelations()
                .enqueue(new ApiCallback<List<BindingRelationItem>>() {
                    @Override
                    public void onSuccess(List<BindingRelationItem> data) {
                        if (isFinishing()) return;
                        runOnUiThread(() -> {
                            if (announce && bindBaselineInitialized) {
                                BindNotificationHelper.processBindings(
                                        ElderHomeActivity.this, data, true);
                            } else if (!bindBaselineInitialized) {
                                BindNotificationHelper.processBindings(
                                        ElderHomeActivity.this, data, false);
                                bindBaselineInitialized = true;
                            }

                            int count = BindNotificationHelper.countActiveRelations(data);
                            tvBindHint.setText(count > 0
                                    ? "已绑定 " + count + " 位家人 ›"
                                    : "查看绑定码 ›");
                        });
                    }

                    @Override
                    public void onNetworkError(String message) {
                        // 首页静默失败，避免启动时反复弹 Toast
                    }
                });
    }

    private void loadLatestMessage() {
        ApiClient.getInstance(this).getApi().getMessages(1, 1).enqueue(new ApiCallback<List<Message>>() {
            @Override
            public void onSuccess(List<Message> data) {
                if (isFinishing()) return;
                runOnUiThread(() -> {
                    if (data != null && !data.isEmpty()) {
                        Message msg = data.get(0);
                        updateMessagePreview(msg);
                        unreadDot.setVisibility(msg.isRead() ? View.GONE : View.VISIBLE);
                    } else {
                        tvMessagePreview.setText("暂无未读家人消息");
                        unreadDot.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onNetworkError(String message) {
                // 首页静默失败，保留默认文案
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

    private void openAiChatHistory() {
        Intent intent = new Intent(this, ElderChatActivity.class);
        intent.putExtra("sessionId", "mock-ai-history");
        intent.putExtra("userQuestion", "小伴，我降压药什么时候吃？");
        intent.putExtra("aiAnswer", "降压药一般建议每天早上起床后服用，饭前饭后都可以，但最好固定时间。不过每个人的情况不一样，最好问问医生哦。");
        intent.putExtra("category", "health");
        startActivity(intent);
    }

    private void playWelcomeMessage() {
        handler.postDelayed(() -> {
            if (isFinishing()) return;
            welcomeBubble.setVisibility(View.VISIBLE);
            welcomeBubble.setAlpha(0f);
            welcomeBubble.animate().alpha(1f).setDuration(600).start();

            VoiceManager.getInstance().getSynthesizer().speak(
                    "您好！我是小伴，有什么想聊的吗？", null);

            handler.postDelayed(() -> {
                if (isFinishing()) return;
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

        tvVoiceHint.setText("小伴思考中...");
        ApiClient.getInstance(this).getApi().chat(body).enqueue(new ApiCallback<ChatResponse>() {
            @Override
            public void onSuccess(ChatResponse data) {
                if (isFinishing()) return;
                runOnUiThread(() -> {
                    tvVoiceHint.setText("按住说话");
                    Intent intent = new Intent(ElderHomeActivity.this, ElderChatActivity.class);
                    intent.putExtra("userQuestion", text);
                    intent.putExtra("aiAnswer", data.getAnswer());
                    intent.putExtra("category", data.getCategory());
                    intent.putExtra("sessionId", sessionId);
                    startActivity(intent);
                });
            }

            @Override
            public void onBusinessError(int code, String message) {
                if (isFinishing()) return;
                runOnUiThread(() -> {
                    tvVoiceHint.setText("按住说话");
                    showToast(message);
                });
            }

            @Override
            public void onNetworkError(String message) {
                if (isFinishing()) return;
                runOnUiThread(() -> {
                    tvVoiceHint.setText("按住说话");
                    showToast("对话失败，请检查网络");
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
        intent.setData(Uri.parse("tel:" + getEmergencyPhoneNumber()));
        startActivity(intent);
    }

    private String getEmergencyPhoneNumber() {
        String emergencyContact = SharedPrefUtil.getString(this, Constants.SP_EMERGENCY_CONTACT, "").trim();
        return emergencyContact.isEmpty() ? DEFAULT_EMERGENCY_PHONE : emergencyContact;
    }

    private void notifyEmergency() {
        ApiClient.getInstance(this).getApi().getBindRelations().enqueue(new ApiCallback<List<BindingRelationItem>>() {
            @Override
            public void onSuccess(List<BindingRelationItem> data) {
                if (data == null || data.isEmpty()) {
                    return;
                }
                String content = "老人发起了紧急求助，请立即联系！";
                for (BindingRelationItem relation : data) {
                    if (!"active".equals(relation.getStatus()) || relation.getChildId() <= 0) {
                        continue;
                    }
                    Map<String, Object> body = new HashMap<>();
                    body.put("receiverId", relation.getChildId());
                    body.put("msgType", "text");
                    body.put("content", content);
                    ApiClient.getInstance(ElderHomeActivity.this).getApi().sendMessage(body)
                            .enqueue(new ApiCallback<Void>() {
                                @Override
                                public void onSuccess(Void ignored) {
                                }

                                @Override
                                public void onNetworkError(String message) {
                                }
                            });
                }
            }

            @Override
            public void onNetworkError(String message) {
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
        handler.removeCallbacksAndMessages(null);
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
