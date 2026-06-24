package com.xiaoban.app.child;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xiaoban.app.R;
import com.xiaoban.app.adapter.DeviceAdapter;
import com.xiaoban.app.base.BaseActivity;
import com.xiaoban.app.model.Device;
import com.xiaoban.app.network.ApiCallback;
import com.xiaoban.app.network.ApiClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChildBindActivity extends BaseActivity {

    private RecyclerView rvDevices;
    private LinearLayout emptyView;
    private CardView cardBluetooth;
    private CardView cardCode;
    private ImageView ivBack;

    private DeviceAdapter adapter;
    private BluetoothAdapter bluetoothAdapter;
    private List<BluetoothDevice> discoveredDevices = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_bind);

        initViews();
        initAdapter();
        initBluetooth();
        loadDevices();
    }

    private void initViews() {
        rvDevices = findViewById(R.id.rv_devices);
        emptyView = findViewById(R.id.empty_view);
        cardBluetooth = findViewById(R.id.card_bluetooth);
        cardCode = findViewById(R.id.card_code);
        ivBack = findViewById(R.id.iv_back);

        ivBack.setOnClickListener(v -> finish());
        cardBluetooth.setOnClickListener(v -> startBluetoothScan());
        cardCode.setOnClickListener(v -> showDeviceCodeDialog());
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

    private void initBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "您的设备不支持蓝牙", Toast.LENGTH_SHORT).show();
            cardBluetooth.setEnabled(false);
        }
    }

    private void loadDevices() {
        ApiClient.getInstance(this).getApi().getBindRelations()
            .enqueue(new ApiCallback<List<Map<String, Object>>>() {
                @Override
                public void onSuccess(List<Map<String, Object>> data) {
                    runOnUiThread(() -> parseDevices(data));
                }
            });
    }

    private void parseDevices(List<Map<String, Object>> data) {
        List<Device> devices = new ArrayList<>();
        if (data != null) {
            for (Map<String, Object> item : data) {
                Device device = new Device();
                device.setId(String.valueOf(item.get("id")));
                device.setName(item.containsKey("elderNickname") ?
                    String.valueOf(item.get("elderNickname")) : "未知设备");
                device.setDeviceId(String.valueOf(item.getOrDefault("deviceId", "")));
                device.setStatus("online");
                device.setBindTime(String.valueOf(item.getOrDefault("createdAt", "")));
                devices.add(device);
            }
        }
        adapter.setDevices(devices);
        updateEmptyView(devices.isEmpty());
    }

    private void startBluetoothScan() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
            return;
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("扫描蓝牙设备")
                .setMessage("正在搜索附近的小伴设备...")
                .create();
        dialog.show();

        discoveredDevices.clear();

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices != null) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName() != null && device.getName().contains("XiaoBan")) {
                    discoveredDevices.add(device);
                }
            }
        }

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(bluetoothReceiver, filter);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            bluetoothAdapter.cancelDiscovery();
            try { unregisterReceiver(bluetoothReceiver); } catch (Exception ignored) {}
            dialog.dismiss();
            showBluetoothDeviceList();
        }, 3000);
    }

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && device.getName() != null && device.getName().contains("XiaoBan")) {
                    if (!discoveredDevices.contains(device)) {
                        discoveredDevices.add(device);
                    }
                }
            }
        }
    };

    private void showBluetoothDeviceList() {
        if (discoveredDevices.isEmpty()) {
            Toast.makeText(this, "未发现小伴设备", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] deviceNames = new String[discoveredDevices.size()];
        for (int i = 0; i < discoveredDevices.size(); i++) {
            deviceNames[i] = discoveredDevices.get(i).getName() + "\n" +
                    discoveredDevices.get(i).getAddress();
        }

        new AlertDialog.Builder(this)
                .setTitle("选择设备")
                .setItems(deviceNames, (dialog, which) -> {
                    bindBluetoothDevice(discoveredDevices.get(which));
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void bindBluetoothDevice(BluetoothDevice device) {
        Map<String, Long> body = new HashMap<>();
        body.put("elderId", 1L);

        ApiClient.getInstance(this).getApi().bindBluetooth(body)
            .enqueue(new ApiCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    runOnUiThread(() -> {
                        Toast.makeText(ChildBindActivity.this, "绑定成功", Toast.LENGTH_SHORT).show();
                        loadDevices();
                    });
                }
            });
    }

    private void showDeviceCodeDialog() {
        EditText etCode = new EditText(this);
        etCode.setHint("请输入6位设备码");
        etCode.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etCode.setMaxLines(1);
        etCode.setPadding(32, 32, 32, 32);

        new AlertDialog.Builder(this)
                .setTitle("设备码绑定")
                .setMessage("在设备端打开「绑定设置」查看绑定码")
                .setView(etCode)
                .setPositiveButton("绑定", (dialog, which) -> {
                    String code = etCode.getText().toString().trim();
                    if (code.length() != 6) {
                        Toast.makeText(this, "请输入6位设备码", Toast.LENGTH_SHORT).show();
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
            .enqueue(new ApiCallback<Map<String, Object>>() {
                @Override
                public void onSuccess(Map<String, Object> data) {
                    runOnUiThread(() -> {
                        Toast.makeText(ChildBindActivity.this, "绑定成功", Toast.LENGTH_SHORT).show();
                        loadDevices();
                    });
                }
            });
    }

    private void showDeviceDetailDialog(Device device) {
        String details = "设备名称：" + device.getName() + "\n" +
                "设备ID：" + device.getDeviceId() + "\n" +
                "绑定时间：" + device.getBindTime() + "\n" +
                "在线状态：" + device.getStatusText();

        new AlertDialog.Builder(this)
                .setTitle("设备详情")
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
                    runOnUiThread(() -> {
                        adapter.removeDevice(device.getId());
                        Toast.makeText(ChildBindActivity.this, "已解绑", Toast.LENGTH_SHORT).show();
                        updateEmptyView(adapter.getItemCount() == 0);
                    });
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
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
    }
}
