package com.xiaoban.app.child;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xiaoban.app.R;
import com.xiaoban.app.base.BaseActivity;
import com.xiaoban.app.child.adapter.ChildChatAdapter;
import com.xiaoban.app.child.adapter.FamilyGroupMemberAdapter;
import com.xiaoban.app.model.BindingRelationItem;
import com.xiaoban.app.model.FamilyGroupMember;
import com.xiaoban.app.model.Message;
import com.xiaoban.app.network.ApiCallback;
import com.xiaoban.app.network.ApiClient;
import com.xiaoban.app.util.ImageUploadHelper;
import com.xiaoban.app.voice.VoiceRecognizer;
import com.xiaoban.app.widget.VoiceButton;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class ChildFamilyChatActivity extends BaseActivity {

    public static final String EXTRA_ELDER_ID = "elder_id";
    public static final String EXTRA_ELDER_NAME = "elder_name";

    private RecyclerView rvMessages;
    private EditText etMessage;
    private VoiceButton voiceButton;
    private View panelActions;
    private TextView tvTitle;
    private ImageView btnVoiceToggle;
    private ImageView btnPlus;

    private ChildChatAdapter chatAdapter;
    private long elderId = -1L;
    private String elderName = "";
    private boolean voiceInputMode = false;
    private boolean actionPanelVisible = false;

    private static final String SEND_ORIGIN_VOICE = "voice";
    private static final String SEND_ORIGIN_TEXT = "text";
    /** 本次发送来源：语音识别填框后待发送，或键盘直接输入 */
    private String pendingSendOrigin;

    private ActivityResultLauncher<String> pickImageLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;
    private Uri pendingCameraUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_family_chat);

        elderId = getIntent().getLongExtra(EXTRA_ELDER_ID, -1L);
        elderName = getIntent().getStringExtra(EXTRA_ELDER_NAME);

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                this::uploadAndSendPhoto);
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (Boolean.TRUE.equals(success) && pendingCameraUri != null) {
                        uploadAndSendPhoto(pendingCameraUri);
                    }
                    pendingCameraUri = null;
                });
        requestCameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (Boolean.TRUE.equals(granted)) {
                        launchCameraCapture();
                    } else {
                        showToast("需要相机权限才能拍照");
                    }
                });

        initViews();
        setupChatList();
        setupInputArea();

        if (elderId > 0 && elderName != null && !elderName.isEmpty()) {
            bindElder(elderId, elderName);
        } else {
            loadDefaultElder();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (elderId > 0) {
            loadFamilyGroupMessages();
        }
    }

    private void initViews() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_more).setOnClickListener(v -> showFamilyMembers());
        tvTitle = findViewById(R.id.tv_chat_title);
        rvMessages = findViewById(R.id.rv_chat_messages);
        etMessage = findViewById(R.id.et_message);
        voiceButton = findViewById(R.id.voice_button);
        panelActions = findViewById(R.id.panel_actions);
        btnVoiceToggle = findViewById(R.id.btn_voice_toggle);
        btnPlus = findViewById(R.id.btn_plus);

        findViewById(R.id.action_gallery).setOnClickListener(v -> {
            hideActionPanel();
            pickImageLauncher.launch("image/*");
        });
        findViewById(R.id.action_camera).setOnClickListener(v -> {
            hideActionPanel();
            openCamera();
        });
    }

    private void setupChatList() {
        chatAdapter = new ChildChatAdapter();
        Long userId = getCurrentUserId();
        chatAdapter.setCurrentUserId(userId != null ? userId : 0L);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(chatAdapter);
        chatAdapter.setOnMessageActionListener(this::confirmRecallMessage);
    }

    private void setupInputArea() {
        btnPlus.setOnClickListener(v -> toggleActionPanel());
        btnVoiceToggle.setOnClickListener(v -> {
            pendingSendOrigin = null;
            applyVoiceInputMode(!voiceInputMode, !voiceInputMode);
        });

        etMessage.setImeOptions(EditorInfo.IME_ACTION_SEND);
        etMessage.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        etMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendTextMessage();
                return true;
            }
            return false;
        });

        voiceButton.setRectangleIdleText("按住 说话");
        voiceButton.setVoiceCallback(new VoiceRecognizer.Callback() {
            @Override
            public void onResult(String text) {
                runOnUiThread(() -> onVoiceRecognitionResult(text));
            }

            @Override
            public void onPartialResult(String text) {
            }

            @Override
            public void onError(String errorMsg) {
                runOnUiThread(() -> showToast("语音识别失败：" + errorMsg));
            }
        });
    }

    private void onVoiceRecognitionResult(String text) {
        String content = normalizeReplyText(text);
        if (content.isEmpty()) {
            showToast("未识别到语音内容");
            return;
        }
        pendingSendOrigin = SEND_ORIGIN_VOICE;
        etMessage.setText(content);
        etMessage.setSelection(content.length());
        applyVoiceInputMode(false, true);
    }

    private void applyVoiceInputMode(boolean useVoiceMode, boolean showKeyboard) {
        voiceInputMode = useVoiceMode;
        etMessage.setVisibility(voiceInputMode ? View.GONE : View.VISIBLE);
        voiceButton.setVisibility(voiceInputMode ? View.VISIBLE : View.GONE);
        btnVoiceToggle.setImageResource(voiceInputMode
                ? R.drawable.ic_close
                : R.drawable.ic_microphone);
        if (voiceInputMode) {
            hideActionPanel();
            etMessage.clearFocus();
            hideKeyboard();
        } else if (showKeyboard) {
            hideActionPanel();
            etMessage.requestFocus();
            showKeyboard();
        }
    }

    private void restoreInputModeAfterSend() {
        if (SEND_ORIGIN_VOICE.equals(pendingSendOrigin)) {
            applyVoiceInputMode(true, false);
        } else {
            applyVoiceInputMode(false, false);
        }
        pendingSendOrigin = null;
        etMessage.setText("");
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

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            etMessage.postDelayed(() ->
                    imm.showSoftInput(etMessage, InputMethodManager.SHOW_IMPLICIT), 100);
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(etMessage.getWindowToken(), 0);
        }
    }

    private void loadDefaultElder() {
        ApiClient.getInstance(this).getApi().getBindRelations()
                .enqueue(new ApiCallback<List<BindingRelationItem>>() {
                    @Override
                    public void onSuccess(List<BindingRelationItem> data) {
                        runOnUiThread(() -> {
                            if (data == null || data.isEmpty()) {
                                showToast("请先绑定老人账户");
                                finish();
                                return;
                            }
                            for (BindingRelationItem item : data) {
                                if ("active".equals(item.getStatus()) && item.getElderId() > 0) {
                                    bindElder(item.getElderId(), item.getElderNickname());
                                    return;
                                }
                            }
                            showToast("请先绑定老人账户");
                            finish();
                        });
                    }

                    @Override
                    public void onNetworkError(String message) {
                        runOnUiThread(() -> {
                            showToast("加载绑定关系失败");
                            finish();
                        });
                    }
                });
    }

    private void bindElder(long id, String name) {
        elderId = id;
        elderName = name;
        tvTitle.setText(elderName);
        chatAdapter.setElderId(elderId);
        loadFamilyGroupMessages();
    }

    private void loadFamilyGroupMessages() {
        ApiClient.getInstance(this).getApi()
                .getFamilyGroupMessages(elderId, 1, 100, true)
                .enqueue(new ApiCallback<List<Message>>() {
                    @Override
                    public void onSuccess(List<Message> data) {
                        runOnUiThread(() -> {
                            chatAdapter.setMessages(data);
                            scrollToBottom();
                            markUnreadMessages(data);
                        });
                    }

                    @Override
                    public void onNetworkError(String message) {
                        runOnUiThread(() -> showToast("消息加载失败"));
                    }
                });
    }

    private void markUnreadMessages(List<Message> messages) {
        if (messages == null) {
            return;
        }
        Long userId = getCurrentUserId();
        if (userId == null) {
            return;
        }
        for (Message message : messages) {
            if (!message.isRead() && message.getReceiverId() == userId) {
                ApiClient.getInstance(this).getApi().markRead(message.getId())
                        .enqueue(new ApiCallback<Void>() {
                            @Override
                            public void onSuccess(Void data) {
                            }
                        });
            }
        }
    }

    private void sendTextMessage() {
        String content = etMessage.getText() == null ? "" : etMessage.getText().toString().trim();
        if (content.isEmpty()) {
            return;
        }
        if (pendingSendOrigin == null) {
            pendingSendOrigin = SEND_ORIGIN_TEXT;
        }
        etMessage.setText("");
        dispatchSend("text", content, null, 0, true);
    }

    private void dispatchSend(String msgType, String content, String mediaUrl, int duration) {
        dispatchSend(msgType, content, mediaUrl, duration, false);
    }

    private void dispatchSend(String msgType, String content, String mediaUrl, int duration,
                              boolean restoreInputAfterSuccess) {
        if (elderId <= 0) {
            showToast("未找到聊天对象");
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("elderId", elderId);
        body.put("receiverId", elderId);
        body.put("msgType", msgType);
        body.put("content", content);
        if (mediaUrl != null) {
            body.put("mediaUrl", mediaUrl);
        }
        if (duration > 0) {
            body.put("duration", duration);
        }

        ApiClient.getInstance(this).getApi().sendFamilyGroupMessage(body)
                .enqueue(new ApiCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        runOnUiThread(() -> {
                            if (restoreInputAfterSuccess) {
                                restoreInputModeAfterSend();
                            }
                            loadFamilyGroupMessages();
                        });
                    }

                    @Override
                    public void onNetworkError(String message) {
                        runOnUiThread(() -> showToast("发送失败，请检查网络"));
                    }

                    @Override
                    public void onBusinessError(int code, String message) {
                        runOnUiThread(() -> showToast(message));
                    }
                });
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            launchCameraCapture();
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchCameraCapture() {
        try {
            File file = new File(getCacheDir(), "camera_" + System.currentTimeMillis() + ".jpg");
            pendingCameraUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    file);
            takePictureLauncher.launch(pendingCameraUri);
        } catch (Exception e) {
            showToast("无法打开相机");
        }
    }

    private void uploadAndSendPhoto(Uri uri) {
        if (uri == null || elderId <= 0) {
            return;
        }
        try {
            File file = ImageUploadHelper.copyToCache(this, uri);
            RequestBody requestBody = RequestBody.create(file, MediaType.parse("image/jpeg"));
            MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), requestBody);

            ApiClient.getInstance(this).getApi().uploadImage(part)
                    .enqueue(new ApiCallback<String>() {
                        @Override
                        public void onSuccess(String mediaUrl) {
                            dispatchSend("photo", "发来一张照片", mediaUrl, 0);
                        }

                        @Override
                        public void onNetworkError(String message) {
                            runOnUiThread(() -> showToast("照片上传失败"));
                        }

                        @Override
                        public void onBusinessError(int code, String message) {
                            runOnUiThread(() -> showToast(message));
                        }
                    });
        } catch (IOException e) {
            showToast("读取照片失败");
        }
    }

    private void toggleActionPanel() {
        actionPanelVisible = !actionPanelVisible;
        panelActions.setVisibility(actionPanelVisible ? View.VISIBLE : View.GONE);
        btnPlus.setRotation(actionPanelVisible ? 45f : 0f);
    }

    private void hideActionPanel() {
        actionPanelVisible = false;
        panelActions.setVisibility(View.GONE);
        btnPlus.setRotation(0f);
    }

    private void scrollToBottom() {
        if (chatAdapter.getItemCount() > 0) {
            rvMessages.scrollToPosition(chatAdapter.getItemCount() - 1);
        }
    }

    private void confirmRecallMessage(Message message) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("撤回消息")
                .setMessage("确定撤回这条消息吗？")
                .setNegativeButton("取消", null)
                .setPositiveButton("撤回", (dialog, which) -> recallMessage(message))
                .show();
    }

    private void recallMessage(Message message) {
        ApiClient.getInstance(this).getApi().recallMessage(message.getId())
                .enqueue(new ApiCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        runOnUiThread(() -> {
                            showToast("已撤回");
                            loadFamilyGroupMessages();
                        });
                    }

                    @Override
                    public void onNetworkError(String errorMsg) {
                        runOnUiThread(() -> showToast("撤回失败：" + errorMsg));
                    }

                    @Override
                    public void onBusinessError(int code, String message) {
                        runOnUiThread(() -> showToast(message));
                    }
                });
    }

    private void showFamilyMembers() {
        if (elderId <= 0) {
            return;
        }
        ApiClient.getInstance(this).getApi().getFamilyGroupMembers(elderId)
                .enqueue(new ApiCallback<List<FamilyGroupMember>>() {
                    @Override
                    public void onSuccess(List<FamilyGroupMember> data) {
                        runOnUiThread(() -> displayFamilyMembersDialog(data));
                    }

                    @Override
                    public void onNetworkError(String message) {
                        runOnUiThread(() -> showToast("成员列表加载失败"));
                    }

                    @Override
                    public void onBusinessError(int code, String message) {
                        runOnUiThread(() -> showToast(message));
                    }
                });
    }

    private void displayFamilyMembersDialog(List<FamilyGroupMember> members) {
        View contentView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_family_group_members, null, false);
        RecyclerView rvMembers = contentView.findViewById(R.id.rv_members);
        FamilyGroupMemberAdapter adapter = new FamilyGroupMemberAdapter();
        adapter.setMembers(members);
        rvMembers.setLayoutManager(new GridLayoutManager(this, 4));
        rvMembers.setAdapter(adapter);

        new AlertDialog.Builder(this)
                .setView(contentView)
                .setPositiveButton("关闭", null)
                .show();
    }
}
