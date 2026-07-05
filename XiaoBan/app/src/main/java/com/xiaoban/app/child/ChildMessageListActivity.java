package com.xiaoban.app.child;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xiaoban.app.R;
import com.xiaoban.app.base.BaseActivity;
import com.xiaoban.app.child.adapter.ChatListAdapter;
import com.xiaoban.app.elder.ElderProfileActivity;
import com.xiaoban.app.model.ConversationSummary;
import com.xiaoban.app.network.ApiCallback;
import com.xiaoban.app.network.ApiClient;

import java.util.List;

public class ChildMessageListActivity extends BaseActivity {

    private TextView tvTitle;
    private RecyclerView rvConversations;
    private TextView tvEmpty;
    private ChatListAdapter adapter;

    private LinearLayout navHome;
    private LinearLayout navChat;
    private LinearLayout navRemind;
    private LinearLayout navSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_message_list);

        initViews();
        setupList();
        setupBottomNav();
        loadConversations();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadConversations();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tv_title);
        rvConversations = findViewById(R.id.rv_conversations);
        tvEmpty = findViewById(R.id.tv_empty);
        findViewById(R.id.btn_back).setOnClickListener(v -> {
            startActivity(new Intent(this, ChildHomeActivity.class));
            finish();
        });
        findViewById(R.id.btn_compose).setOnClickListener(v ->
                startActivity(new Intent(this, ChildBindActivity.class)));
    }

    private void setupList() {
        adapter = new ChatListAdapter();
        adapter.setOnConversationClickListener(item -> {
            Intent intent = new Intent(this, ChildFamilyChatActivity.class);
            intent.putExtra(ChildFamilyChatActivity.EXTRA_ELDER_ID, item.getPeerId());
            intent.putExtra(ChildFamilyChatActivity.EXTRA_ELDER_NAME, item.getPeerName());
            startActivity(intent);
        });
        rvConversations.setLayoutManager(new LinearLayoutManager(this));
        rvConversations.setAdapter(adapter);
    }

    private void setupBottomNav() {
        navHome = findViewById(R.id.nav_home);
        navChat = findViewById(R.id.nav_chat);
        navRemind = findViewById(R.id.nav_remind);
        navSettings = findViewById(R.id.nav_settings);

        navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, ChildHomeActivity.class));
            finish();
        });
        navChat.setOnClickListener(v ->
                startActivity(new Intent(this, ChildCorrectActivity.class)));
        navRemind.setOnClickListener(v ->
                startActivity(new Intent(this, ChildReminderActivity.class)));
        navSettings.setOnClickListener(v ->
                startActivity(new Intent(this, ElderProfileActivity.class)));
    }

    private void loadConversations() {
        ApiClient.getInstance(this).getApi().getConversations()
                .enqueue(new ApiCallback<List<ConversationSummary>>() {
                    @Override
                    public void onSuccess(List<ConversationSummary> data) {
                        runOnUiThread(() -> updateUi(data));
                    }

                    @Override
                    public void onNetworkError(String message) {
                        runOnUiThread(() -> showToast("消息列表加载失败"));
                    }

                    @Override
                    public void onBusinessError(int code, String message) {
                        runOnUiThread(() -> showToast(message));
                    }
                });
    }

    private void updateUi(List<ConversationSummary> data) {
        int totalUnread = 0;
        if (data != null) {
            for (ConversationSummary item : data) {
                totalUnread += item.getUnreadCount();
            }
        }
        tvTitle.setText(totalUnread > 0 ? "消息 (" + totalUnread + ")" : "消息");

        boolean empty = data == null || data.isEmpty();
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvConversations.setVisibility(empty ? View.GONE : View.VISIBLE);
        adapter.setItems(data);
    }
}
