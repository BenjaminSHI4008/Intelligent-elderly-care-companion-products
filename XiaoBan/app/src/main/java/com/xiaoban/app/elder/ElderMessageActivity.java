package com.xiaoban.app.elder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xiaoban.app.R;
import com.xiaoban.app.base.BaseActivity;
import com.xiaoban.app.elder.adapter.MessageAdapter;
import com.xiaoban.app.model.BindingRelationItem;
import com.xiaoban.app.model.Message;
import com.xiaoban.app.network.ApiCallback;
import com.xiaoban.app.network.ApiClient;
import com.xiaoban.app.util.FamilyMessageNotifier;
import com.xiaoban.app.util.TimeUtil;
import com.xiaoban.app.voice.VoiceManager;
import com.xiaoban.app.voice.VoiceRecognizer;
import com.xiaoban.app.widget.VoiceButton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElderMessageActivity extends BaseActivity implements FamilyMessageNotifier.Listener {

    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private VoiceButton voiceReplyButton;
    private View btnBack;
    private boolean announceUnreadOnLoad = true;

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
        btnBack = findViewById(R.id.btn_back);

        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        loadMessages(false);
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter();
        messageAdapter.setOnMessageClickListener(new MessageAdapter.OnMessageClickListener() {
            @Override
            public void onMessageClick(Message message) {
                openMessageDetail(message);
            }

            @Override
            public void onVoiceClick(Message message) {
                openMessageDetail(message);
            }

            @Override
            public void onPhotoClick(Message message) {
                openMessageDetail(message);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);
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

    private void loadMessages(boolean announceUnread) {
        ApiClient.getInstance(this).getApi().getMessages(1, 20).enqueue(new ApiCallback<List<Message>>() {
            @Override
            public void onSuccess(List<Message> data) {
                runOnUiThread(() -> {
                    if (data != null) {
                        messageAdapter.setMessages(data);
                        if (announceUnread && !data.isEmpty() && !data.get(0).isRead()) {
                            playNewMessageNotification(data.get(0));
                        }
                    }
                });
            }
        });
    }

    private void openMessageDetail(Message message) {
        markAsRead(message);
        Intent intent = new Intent(this, ElderMessageDetailActivity.class);
        intent.putExtra("messageId", message.getId());
        intent.putExtra("senderName", message.getSenderName());
        intent.putExtra("messageType", message.getType());
        intent.putExtra("content", message.getContent());
        intent.putExtra("mediaUrl", message.getMediaUrl());
        intent.putExtra("duration", message.getDuration());
        intent.putExtra("time", TimeUtil.formatMessageTime(message.getCreateTime()));
        startActivity(intent);
    }

    private void sendReply(String text) {
        ApiClient.getInstance(this).getApi().getBindRelations().enqueue(new ApiCallback<List<BindingRelationItem>>() {
            @Override
            public void onSuccess(List<BindingRelationItem> data) {
                Long receiverId = resolveReplyReceiverId(data);
                if (receiverId == null) {
                    runOnUiThread(() -> showToast("暂未绑定家人，无法回复"));
                    return;
                }

                Map<String, Object> body = new HashMap<>();
                body.put("receiverId", receiverId);
                body.put("msgType", "text");
                body.put("content", text);

                ApiClient.getInstance(ElderMessageActivity.this).getApi().sendMessage(body)
                        .enqueue(new ApiCallback<Void>() {
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

            @Override
            public void onNetworkError(String message) {
                runOnUiThread(() -> showToast("发送失败：无法获取绑定关系"));
            }
        });
    }

    private Long resolveReplyReceiverId(List<BindingRelationItem> relations) {
        Message latest = messageAdapter.getLatestMessage();
        if (latest != null && latest.getSenderId() > 0) {
            return latest.getSenderId();
        }
        if (relations == null) {
            return null;
        }
        for (BindingRelationItem relation : relations) {
            if ("active".equals(relation.getStatus()) && relation.getChildId() > 0) {
                return relation.getChildId();
            }
        }
        return null;
    }

    private void markAsRead(Message message) {
        if (!message.isRead()) {
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
        loadMessages(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VoiceManager.getInstance().getSynthesizer().stop();
    }
}
