package com.xiaoban.app.elder;

import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xiaoban.app.R;
import com.xiaoban.app.base.BaseActivity;
import com.xiaoban.app.elder.adapter.MessageAdapter;
import com.xiaoban.app.model.Message;
import com.xiaoban.app.network.ApiCallback;
import com.xiaoban.app.network.ApiClient;
import com.xiaoban.app.voice.VoiceManager;
import com.xiaoban.app.voice.VoiceRecognizer;
import com.xiaoban.app.widget.VoiceButton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElderMessageActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private VoiceButton voiceReplyButton;
    private View btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elder_message);

        initViews();
        setupRecyclerView();
        setupVoiceButton();
        loadMessages();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.rv_messages);
        voiceReplyButton = findViewById(R.id.voice_reply_button);
        btnBack = findViewById(R.id.btn_back);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter();
        messageAdapter.setOnMessageClickListener(new MessageAdapter.OnMessageClickListener() {
            @Override
            public void onVoiceClick(Message message) {
                playVoiceMessage(message);
            }

            @Override
            public void onPhotoClick(Message message) {
                showPhotoFullscreen(message);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                markVisibleMessagesAsRead();
            }
        });
    }

    private void setupVoiceButton() {
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

    private void loadMessages() {
        ApiClient.getInstance(this).getApi().getMessages(1, 20).enqueue(new ApiCallback<List<Message>>() {
            @Override
            public void onSuccess(List<Message> data) {
                runOnUiThread(() -> {
                    if (data != null) {
                        messageAdapter.setMessages(data);
                        if (!data.isEmpty() && !data.get(0).isRead()) {
                            playNewMessageNotification(data.get(0));
                        }
                    }
                });
            }
        });
    }

    private void playVoiceMessage(Message message) {
        if ("voice".equals(message.getType())) {
            VoiceManager.getInstance().getSynthesizer().speak(message.getContent(), null);
            markAsRead(message);
        }
    }

    private void showPhotoFullscreen(Message message) {
        showToast("查看照片：" + message.getContent());
        markAsRead(message);
    }

    private void sendReply(String text) {
        Map<String, Object> body = new HashMap<>();
        body.put("content", text);
        body.put("type", "voice");

        ApiClient.getInstance(this).getApi().sendMessage(body).enqueue(new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                runOnUiThread(() -> showToast("已发送回复"));
            }

            public void onNetworkError(String errorMsg) {
                runOnUiThread(() -> showToast("发送失败：" + errorMsg));
            }
        });
    }

    private void markAsRead(Message message) {
        if (!message.isRead()) {
            message.setRead(true);
            ApiClient.getInstance(this).getApi().markRead(message.getId()).enqueue(new ApiCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                }
            });
        }
    }

    private void markVisibleMessagesAsRead() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        if (layoutManager != null) {
            int firstVisible = layoutManager.findFirstVisibleItemPosition();
            int lastVisible = layoutManager.findLastVisibleItemPosition();

            for (int i = firstVisible; i <= lastVisible; i++) {
                if (i >= 0 && i < messageAdapter.getItemCount()) {
                }
            }
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
        runOnUiThread(() -> {
            messageAdapter.addMessage(message);
            playNewMessageNotification(message);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VoiceManager.getInstance().getSynthesizer().stop();
    }
}
