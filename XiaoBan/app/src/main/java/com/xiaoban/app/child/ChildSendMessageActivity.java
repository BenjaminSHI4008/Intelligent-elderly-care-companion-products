package com.xiaoban.app.child;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.bumptech.glide.Glide;
import com.xiaoban.app.R;
import com.xiaoban.app.base.BaseActivity;
import com.xiaoban.app.model.BindingRelationItem;
import com.xiaoban.app.network.ApiCallback;
import com.xiaoban.app.network.ApiClient;
import com.xiaoban.app.util.ImageUploadHelper;
import com.xiaoban.app.voice.VoiceRecognizer;
import com.xiaoban.app.widget.VoiceButton;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class ChildSendMessageActivity extends BaseActivity {

    private static final String TYPE_TEXT = "text";
    private static final String TYPE_VOICE = "voice";
    private static final String TYPE_PHOTO = "photo";

    private Spinner spinnerElder;
    private RadioGroup rgMessageType;
    private LinearLayout panelText;
    private LinearLayout panelVoice;
    private LinearLayout panelPhoto;
    private EditText etMessage;
    private TextView tvVoicePreview;
    private VoiceButton voiceButton;
    private ImageView ivPhotoPreview;
    private TextView btnPickPhoto;
    private EditText etPhotoCaption;
    private TextView btnSend;
    private ImageView ivBack;

    private final List<BindingRelationItem> activeRelations = new ArrayList<>();
    private String selectedMessageType = TYPE_TEXT;
    private String voiceContent = "";
    private int voiceDurationSeconds = 0;
    private Uri selectedPhotoUri;

    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_send_message);

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedPhotoUri = uri;
                        Glide.with(this).load(uri).centerCrop().into(ivPhotoPreview);
                    }
                });

        initViews();
        loadBoundElders();
    }

    private void initViews() {
        spinnerElder = findViewById(R.id.spinner_elder);
        rgMessageType = findViewById(R.id.rg_message_type);
        panelText = findViewById(R.id.panel_text);
        panelVoice = findViewById(R.id.panel_voice);
        panelPhoto = findViewById(R.id.panel_photo);
        etMessage = findViewById(R.id.et_message);
        tvVoicePreview = findViewById(R.id.tv_voice_preview);
        voiceButton = findViewById(R.id.voice_button);
        ivPhotoPreview = findViewById(R.id.iv_photo_preview);
        btnPickPhoto = findViewById(R.id.btn_pick_photo);
        etPhotoCaption = findViewById(R.id.et_photo_caption);
        btnSend = findViewById(R.id.btn_send);
        ivBack = findViewById(R.id.iv_back);

        ivBack.setOnClickListener(v -> finish());
        btnSend.setOnClickListener(v -> sendMessage());
        btnPickPhoto.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        rgMessageType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_voice) {
                selectedMessageType = TYPE_VOICE;
            } else if (checkedId == R.id.rb_photo) {
                selectedMessageType = TYPE_PHOTO;
            } else {
                selectedMessageType = TYPE_TEXT;
            }
            updateMessageTypePanels();
        });

        voiceButton.setVoiceCallback(new VoiceRecognizer.Callback() {
            @Override
            public void onResult(String text) {
                runOnUiThread(() -> {
                    voiceContent = text == null ? "" : text.trim();
                    voiceDurationSeconds = Math.max(1, voiceContent.length() / 4);
                    tvVoicePreview.setText(voiceContent.isEmpty()
                            ? "未识别到语音内容，请重试"
                            : voiceContent);
                });
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

    private void updateMessageTypePanels() {
        panelText.setVisibility(TYPE_TEXT.equals(selectedMessageType) ? View.VISIBLE : View.GONE);
        panelVoice.setVisibility(TYPE_VOICE.equals(selectedMessageType) ? View.VISIBLE : View.GONE);
        panelPhoto.setVisibility(TYPE_PHOTO.equals(selectedMessageType) ? View.VISIBLE : View.GONE);
    }

    private void loadBoundElders() {
        ApiClient.getInstance(this).getApi().getBindRelations()
                .enqueue(new ApiCallback<List<BindingRelationItem>>() {
                    @Override
                    public void onSuccess(List<BindingRelationItem> data) {
                        runOnUiThread(() -> bindElderOptions(data));
                    }

                    @Override
                    public void onNetworkError(String message) {
                        runOnUiThread(() -> showToast("加载绑定关系失败"));
                    }
                });
    }

    private void bindElderOptions(List<BindingRelationItem> relations) {
        activeRelations.clear();
        if (relations != null) {
            for (BindingRelationItem relation : relations) {
                if ("active".equals(relation.getStatus()) && relation.getElderId() > 0) {
                    activeRelations.add(relation);
                }
            }
        }

        if (activeRelations.isEmpty()) {
            showToast("请先绑定老人账户");
            finish();
            return;
        }

        List<String> labels = new ArrayList<>();
        for (BindingRelationItem relation : activeRelations) {
            labels.add(relation.getDisplayNameForChild());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, labels);
        spinnerElder.setAdapter(adapter);
    }

    private void sendMessage() {
        if (activeRelations.isEmpty()) {
            showToast("暂无可发送的老人账户");
            return;
        }

        int index = spinnerElder.getSelectedItemPosition();
        if (index < 0 || index >= activeRelations.size()) {
            showToast("请选择发送对象");
            return;
        }

        BindingRelationItem target = activeRelations.get(index);
        switch (selectedMessageType) {
            case TYPE_VOICE:
                sendVoiceMessage(target);
                break;
            case TYPE_PHOTO:
                sendPhotoMessage(target);
                break;
            default:
                sendTextMessage(target);
                break;
        }
    }

    private void sendTextMessage(BindingRelationItem target) {
        String content = etMessage.getText().toString().trim();
        if (content.isEmpty()) {
            showToast("请输入消息内容");
            return;
        }
        dispatchSend(target.getElderId(), TYPE_TEXT, content, null, 0);
    }

    private void sendVoiceMessage(BindingRelationItem target) {
        if (voiceContent.isEmpty()) {
            showToast("请先录制语音消息");
            return;
        }
        dispatchSend(target.getElderId(), TYPE_VOICE, voiceContent, null, voiceDurationSeconds);
    }

    private void sendPhotoMessage(BindingRelationItem target) {
        if (selectedPhotoUri == null) {
            showToast("请先选择照片");
            return;
        }

        btnSend.setEnabled(false);
        try {
            File file = ImageUploadHelper.copyToCache(this, selectedPhotoUri);
            RequestBody requestBody = RequestBody.create(file, MediaType.parse("image/jpeg"));
            MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), requestBody);

            ApiClient.getInstance(this).getApi().uploadImage(part).enqueue(new ApiCallback<String>() {
                @Override
                public void onSuccess(String mediaUrl) {
                    String caption = etPhotoCaption.getText().toString().trim();
                    if (caption.isEmpty()) {
                        caption = "发来一张照片";
                    }
                    dispatchSend(target.getElderId(), TYPE_PHOTO, caption, mediaUrl, 0);
                }

                @Override
                public void onNetworkError(String message) {
                    runOnUiThread(() -> {
                        btnSend.setEnabled(true);
                        showToast("照片上传失败，请检查网络");
                    });
                }

                @Override
                public void onBusinessError(int code, String message) {
                    runOnUiThread(() -> {
                        btnSend.setEnabled(true);
                        showToast(message);
                    });
                }
            });
        } catch (Exception e) {
            btnSend.setEnabled(true);
            showToast("读取照片失败");
        }
    }

    private void dispatchSend(long receiverId, String msgType, String content, String mediaUrl, int duration) {
        Map<String, Object> body = new HashMap<>();
        body.put("receiverId", receiverId);
        body.put("msgType", msgType);
        body.put("content", content);
        if (mediaUrl != null) {
            body.put("mediaUrl", mediaUrl);
        }
        if (duration > 0) {
            body.put("duration", duration);
        }

        btnSend.setEnabled(false);
        ApiClient.getInstance(this).getApi().sendMessage(body).enqueue(new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                runOnUiThread(() -> {
                    showToast("消息已发送");
                    finish();
                });
            }

            @Override
            public void onNetworkError(String message) {
                runOnUiThread(() -> {
                    btnSend.setEnabled(true);
                    showToast("发送失败，请检查网络");
                });
            }

            @Override
            public void onBusinessError(int code, String message) {
                runOnUiThread(() -> {
                    btnSend.setEnabled(true);
                    showToast(message);
                });
            }
        });
    }
}
