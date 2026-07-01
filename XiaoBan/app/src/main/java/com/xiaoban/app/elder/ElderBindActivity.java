package com.xiaoban.app.elder;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.xiaoban.app.R;
import com.xiaoban.app.adapter.BoundFamilyAdapter;
import com.xiaoban.app.base.BaseActivity;
import com.xiaoban.app.model.BindingRelationItem;
import com.xiaoban.app.model.GenerateCodeResponse;
import com.xiaoban.app.network.ApiCallback;
import com.xiaoban.app.network.ApiClient;
import com.xiaoban.app.util.BindNotificationHelper;
import com.xiaoban.app.voice.VoiceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 老人端绑定设置页：生成绑定码、展示已绑定家人列表。
 */
public class ElderBindActivity extends BaseActivity {

    private static final long POLL_INTERVAL_MS = 4000L;

    private TextView tvBindCode;
    private TextView tvCountdown;
    private TextView tvBoundCount;
    private TextView tvBoundEmpty;
    private ProgressBar progressLoading;
    private View btnRefreshCode;
    private View btnReplayCode;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvBoundFamily;

    private BoundFamilyAdapter boundFamilyAdapter;
    private CountDownTimer countDownTimer;
    private Handler pollHandler;
    private Runnable pollRunnable;
    private String currentCode = "";
    private boolean baselineInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elder_bind);

        pollHandler = new Handler(Looper.getMainLooper());
        initViews();
        generateBindCode();
        loadBoundRelations(false);
    }

    private void initViews() {
        tvBindCode = findViewById(R.id.tv_bind_code);
        tvCountdown = findViewById(R.id.tv_countdown);
        tvBoundCount = findViewById(R.id.tv_bound_count);
        tvBoundEmpty = findViewById(R.id.tv_bound_empty);
        progressLoading = findViewById(R.id.progress_loading);
        btnRefreshCode = findViewById(R.id.btn_refresh_code);
        btnReplayCode = findViewById(R.id.btn_replay_code);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        rvBoundFamily = findViewById(R.id.rv_bound_family);

        boundFamilyAdapter = new BoundFamilyAdapter();
        rvBoundFamily.setLayoutManager(new LinearLayoutManager(this));
        rvBoundFamily.setAdapter(boundFamilyAdapter);

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());
        btnRefreshCode.setOnClickListener(v -> generateBindCode());
        btnReplayCode.setOnClickListener(v -> replayCode());
        swipeRefresh.setColorSchemeResources(R.color.primary);
        swipeRefresh.setOnRefreshListener(() -> {
            loadBoundRelations(true);
            if (currentCode.isEmpty()) {
                generateBindCode();
            }
        });
    }

    private void generateBindCode() {
        setCodeLoading(true);
        ApiClient.getInstance(this).getApi().generateCode()
                .enqueue(new ApiCallback<GenerateCodeResponse>() {
                    @Override
                    public void onSuccess(GenerateCodeResponse data) {
                        if (isFinishing()) return;
                        runOnUiThread(() -> {
                            setCodeLoading(false);
                            swipeRefresh.setRefreshing(false);
                            if (data == null || data.getCode() == null || data.getCode().isEmpty()) {
                                showToast("获取绑定码失败");
                                return;
                            }
                            currentCode = data.getCode();
                            displayCode(currentCode);
                            startCountdown(data.getExpiresInSeconds() > 0
                                    ? data.getExpiresInSeconds() * 1000L : 5 * 60 * 1000L);
                            speakCode(currentCode);
                        });
                    }

                    @Override
                    public void onBusinessError(int code, String message) {
                        if (isFinishing()) return;
                        runOnUiThread(() -> {
                            setCodeLoading(false);
                            swipeRefresh.setRefreshing(false);
                            showToast(message);
                        });
                    }

                    @Override
                    public void onNetworkError(String message) {
                        if (isFinishing()) return;
                        runOnUiThread(() -> {
                            setCodeLoading(false);
                            swipeRefresh.setRefreshing(false);
                            showToast("网络异常，请检查网络后重试");
                        });
                    }
                });
    }

    private void replayCode() {
        if (currentCode == null || currentCode.length() != 6) {
            showToast("请先获取绑定码");
            return;
        }
        speakCode(currentCode);
    }

    private void displayCode(String code) {
        tvBindCode.setText(String.format("%s %s %s",
                code.substring(0, 2), code.substring(2, 4), code.substring(4, 6)));
        btnReplayCode.setEnabled(true);
        btnReplayCode.setAlpha(1f);
    }

    private void speakCode(String code) {
        String speech = "您的绑定码是，" + code.charAt(0) + "，" + code.charAt(1) + "，"
                + code.charAt(2) + "，" + code.charAt(3) + "，"
                + code.charAt(4) + "，" + code.charAt(5)
                + "。请告诉您的子女。";
        VoiceManager.getInstance().getSynthesizer().speak(speech, null);
    }

    private void startCountdown(long durationMs) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(durationMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int totalSeconds = (int) (millisUntilFinished / 1000);
                int minutes = totalSeconds / 60;
                int seconds = totalSeconds % 60;
                tvCountdown.setText(String.format(Locale.getDefault(),
                        "%d:%02d 后失效", minutes, seconds));
            }

            @Override
            public void onFinish() {
                tvCountdown.setText("绑定码已过期，请点击重新生成");
                tvBindCode.setText("- -  - -  - -");
                currentCode = "";
                btnReplayCode.setEnabled(false);
                btnReplayCode.setAlpha(0.5f);
            }
        };
        countDownTimer.start();
    }

    private void loadBoundRelations(boolean announce) {
        ApiClient.getInstance(this).getApi().getBindRelations()
                .enqueue(new ApiCallback<List<BindingRelationItem>>() {
                    @Override
                    public void onSuccess(List<BindingRelationItem> data) {
                        if (isFinishing()) return;
                        runOnUiThread(() -> {
                            swipeRefresh.setRefreshing(false);
                            if (announce && baselineInitialized) {
                                BindNotificationHelper.processBindings(
                                        ElderBindActivity.this, data, true);
                            } else if (!baselineInitialized) {
                                BindNotificationHelper.processBindings(
                                        ElderBindActivity.this, data, false);
                                baselineInitialized = true;
                            }
                            displayBoundRelations(data);
                        });
                    }

                    @Override
                    public void onNetworkError(String message) {
                        if (isFinishing()) return;
                        runOnUiThread(() -> swipeRefresh.setRefreshing(false));
                    }
                });
    }

    private void displayBoundRelations(List<BindingRelationItem> relations) {
        List<BindingRelationItem> active = new ArrayList<>();
        if (relations != null) {
            for (BindingRelationItem item : relations) {
                if ("active".equals(item.getStatus())) {
                    active.add(item);
                }
            }
        }

        tvBoundCount.setText(active.size() + " 位家人");
        boundFamilyAdapter.setItems(active);

        if (active.isEmpty()) {
            rvBoundFamily.setVisibility(View.GONE);
            tvBoundEmpty.setVisibility(View.VISIBLE);
        } else {
            rvBoundFamily.setVisibility(View.VISIBLE);
            tvBoundEmpty.setVisibility(View.GONE);
        }
    }

    private void startBindPolling() {
        stopBindPolling();
        pollRunnable = () -> {
            if (!isFinishing()) {
                loadBoundRelations(true);
                pollHandler.postDelayed(pollRunnable, POLL_INTERVAL_MS);
            }
        };
        pollHandler.postDelayed(pollRunnable, POLL_INTERVAL_MS);
    }

    private void stopBindPolling() {
        if (pollHandler != null && pollRunnable != null) {
            pollHandler.removeCallbacks(pollRunnable);
        }
    }

    private void setCodeLoading(boolean loading) {
        progressLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnRefreshCode.setEnabled(!loading);
        btnRefreshCode.setAlpha(loading ? 0.6f : 1f);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBoundRelations(true);
        startBindPolling();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopBindPolling();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopBindPolling();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        VoiceManager.getInstance().getSynthesizer().stop();
    }
}
