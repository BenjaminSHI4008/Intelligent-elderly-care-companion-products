package com.xiaoban.app.model;

public class Device {
    private String id;
    private String name;
    private String deviceId;
    private String status;
    private String bindTime;
    private String lastActiveTime;
    private String elderName;
    private String bluetoothAddress;
    private String firmwareVersion;

    public Device() {}

    public Device(String id, String name, String deviceId, String status) {
        this.id = id;
        this.name = name;
        this.deviceId = deviceId;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getBindTime() { return bindTime; }
    public void setBindTime(String bindTime) { this.bindTime = bindTime; }
    public String getLastActiveTime() { return lastActiveTime; }
    public void setLastActiveTime(String lastActiveTime) { this.lastActiveTime = lastActiveTime; }
    public String getElderName() { return elderName; }
    public void setElderName(String elderName) { this.elderName = elderName; }
    public String getBluetoothAddress() { return bluetoothAddress; }
    public void setBluetoothAddress(String bluetoothAddress) { this.bluetoothAddress = bluetoothAddress; }
    public String getFirmwareVersion() { return firmwareVersion; }
    public void setFirmwareVersion(String firmwareVersion) { this.firmwareVersion = firmwareVersion; }

    public boolean isOnline() { return "online".equals(status); }

    public String getStatusText() { return isOnline() ? "在线" : "离线"; }

    public int getStatusColor() {
        return isOnline() ? android.R.color.holo_green_light : android.R.color.darker_gray;
    }

    public String getFormattedBindTime() {
        if (bindTime == null || bindTime.isEmpty()) return "未知";
        return "绑定于 " + bindTime;
    }

    public String getFormattedDeviceId() {
        if (deviceId == null || deviceId.isEmpty()) return "未知设备";
        return "设备ID: " + deviceId;
    }
}
