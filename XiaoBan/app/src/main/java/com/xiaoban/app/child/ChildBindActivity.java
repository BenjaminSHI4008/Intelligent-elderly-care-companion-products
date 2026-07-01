package com.xiaoban.app.child;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.xiaoban.app.R;
import com.xiaoban.app.adapter.DeviceAdapter;
import com.xiaoban.app.base.BaseActivity;
import com.xiaoban.app.model.BindVerifyResult;
import com.xiaoban.app.model.BindingRelationItem;
import com.xiaoban.app.model.Device;
import com.xiaoban.app.network.ApiCallback;
import com.xiaoban.app.network.ApiClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChildBindActivity extends BaseActivity {

    private RecyclerView rvDevices;
    private LinearLayout emptyView;
    private CardView cardBluetooth;
    private CardView cardCode;
    private ImageView ivBack;
    private SwipeRefreshLayout swipeRefresh;

    private DeviceAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_bind);

        initViews();
        initAdapter();
        loadDevices();
    }

    private void initViews() {
        rvDevices = findViewById(R.id.rv_devices);
        emptyView = findViewById(R.id.empty_view);
        cardBluetooth = findViewById(R.id.card_bluetooth);
        cardCode = findViewById(R.id.card_code);
        ivBack = findViewById(R.id.iv_back);
        swipeRefresh = findViewById(R.id.swipe_refresh);

        ivBack.setOnClickListener(v -> finish());
        cardBluetooth.setOnClickListener(v ->
                showToast("蓝牙绑定即将推出，请使用设备码绑定"));
        cardCode.setOnClickListener(v -> showDeviceCodeDialog());
        cardBluetooth.setAlpha(0.55f);

        if (swipeRefresh != null) {
            swipeRefresh.setColorSchemeResources(R.color.child_primary);
            swipeRefresh.setOnRefreshListener(() -> loadDevices());
        }
    }

    private void initAdapter() {
        adapter = new DeviceAdapter();
        rvDevices.setLayoutManager(new LinearLayoutManager(this));
        rvDevices.setAdapter(adapter);

        adapter.setOnActionListener(new DeviceAdapter.OnDeviceActionListener() {
            @Override
            public void onDetail(Device device) {
                showDeviceDetailDialog(device);
            }

            @Override
            public void onUnbind(Device device) {
                showUnbindConfirmDialog(device);
            }
        });
    }

    private void loadDevices() {
        ApiClient.getInstance(this).getApi().getBindRelations()
                .enqueue(new ApiCallback<List<BindingRelationItem>>() {
                    @Override
                    public void onSuccess(List<BindingRelationItem> data) {
                        if (isFinishing()) return;
                        runOnUiThread(() -> {
                            if (swipeRefresh != null) {
                                swipeRefresh.setRefreshing(false);
                            }
                            parseDevices(data);
                        });
                    }

                    @Override
                    public void onNetworkError(String message) {
                        if (isFinishing()) return;
                        runOnUiThread(() -> {
                            if (swipeRefresh != null) {
                                swipeRefresh.setRefreshing(false);
                            }
                            showToast("加载失败，请下拉刷新重试");
                        });
                    }
                });
    }

    private void parseDevices(List<BindingRelationItem> data) {
        List<Device> devices = new ArrayList<>();
        if (data != null) {
            for (BindingRelationItem item : data) {
                if (!"active".equals(item.getStatus())) {
                    continue;
                }
                Device device = new Device();
                device.setId(String.valueOf(item.getId()));
                device.setName(item.getDisplayNameForChild());
                device.setDeviceId(String.valueOf(item.getElderId()));
                device.setStatus("online");
                device.setBindTime(item.getBindTime() != null ? item.getBindTime() : "");
                device.setBindTypeLabel(item.getBindTypeLabel());
                devices.add(device);
            }
        }
        adapter.setDevices(devices);
        updateEmptyView(devices.isEmpty());
    }

    private void showDeviceCodeDialog() {
        EditText etCode = new EditText(this);
        etCode.setHint("请输入6位绑定码");
        etCode.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etCode.setMaxLines(1);
        etCode.setPadding(32, 32, 32, 32);

        new AlertDialog.Builder(this)
                .setTitle("设备码绑定")
                .setMessage("请老人在「绑定家人」页面查看绑定码")
                .setView(etCode)
                .setPositiveButton("绑定", (dialog, which) -> {
                    String code = etCode.getText().toString().trim();
                    if (code.length() != 6) {
                        Toast.makeText(this, "请输入6位绑定码", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    bindDeviceByCode(code);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void bindDeviceByCode(String code) {
        Map<String, String> body = new HashMap<>();
        body.put("code", code);

        ApiClient.getInstance(this).getApi().verifyCode(body)
                .enqueue(new ApiCallback<BindVerifyResult>() {
                    @Override
                    public void onSuccess(BindVerifyResult data) {
                        if (isFinishing()) return;
                        runOnUiThread(() -> {
                            String name = data != null ? data.getDisplayName() : "老人";
                            Toast.makeText(ChildBindActivity.this,
                                    "已成功绑定「" + name + "」", Toast.LENGTH_LONG).show();
                            loadDevices();
                        });
                    }

                    @Override
                    public void onBusinessError(int code, String message) {
                        if (isFinishing()) return;
                        runOnUiThread(() -> showToast(message));
                    }

                    @Override
                    public void onNetworkError(String message) {
                        if (isFinishing()) return;
                        runOnUiThread(() -> showToast("绑定失败，请检查网络"));
                    }
                });
    }

    private void showDeviceDetailDialog(Device device) {
        String bindType = device.getBindTypeLabel() != null ? device.getBindTypeLabel() : "设备码绑定";
        String details = "称呼：" + device.getName() + "\n"
                + "账户ID：" + device.getDeviceId() + "\n"
                + "绑定方式：" + bindType + "\n"
                + "绑定时间：" + device.getFormattedBindTime() + "\n"
                + "状态：" + device.getStatusText();

        new AlertDialog.Builder(this)
                .setTitle("老人账户详情")
                .setMessage(details)
                .setPositiveButton("确定", null)
                .show();
    }

    private void showUnbindConfirmDialog(Device device) {
        new AlertDialog.Builder(this)
                .setTitle("解除绑定")
                .setMessage("确定要解绑「" + device.getName() + "」吗？\n解绑后仍可重新绑定，历史数据不会丢失。")
                .setPositiveButton("解绑", (dialog, which) -> unbindDevice(device))
                .setNegativeButton("取消", null)
                .show();
    }

    private void unbindDevice(Device device) {
        long relationId = Long.parseLong(device.getId());
        ApiClient.getInstance(this).getApi().unbind(relationId)
                .enqueue(new ApiCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        if (isFinishing()) return;
                        runOnUiThread(() -> {
                            adapter.removeDevice(device.getId());
                            Toast.makeText(ChildBindActivity.this, "已解绑", Toast.LENGTH_SHORT).show();
                            updateEmptyView(adapter.getItemCount() == 0);
                        });
                    }

                    @Override
                    public void onBusinessError(int code, String message) {
                        if (isFinishing()) return;
                        runOnUiThread(() -> showToast(message));
                    }
                });
    }

    private void updateEmptyView(boolean isEmpty) {
        if (isEmpty) {
            rvDevices.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            rvDevices.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDevices();
    }
}
