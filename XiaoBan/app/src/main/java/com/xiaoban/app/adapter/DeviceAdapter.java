package com.xiaoban.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.xiaoban.app.R;
import com.xiaoban.app.model.Device;
import java.util.ArrayList;
import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {

    private List<Device> devices = new ArrayList<>();
    private OnDeviceActionListener listener;

    public interface OnDeviceActionListener {
        void onDetail(Device device);
        void onUnbind(Device device);
    }

    public void setOnActionListener(OnDeviceActionListener listener) {
        this.listener = listener;
    }

    public void setDevices(List<Device> devices) {
        this.devices = devices != null ? devices : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addDevice(Device device) {
        devices.add(0, device);
        notifyItemInserted(0);
    }

    public void removeDevice(String deviceId) {
        for (int i = 0; i < devices.size(); i++) {
            if (devices.get(i).getId().equals(deviceId)) {
                devices.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_device, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Device device = devices.get(position);
        holder.bind(device);
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDeviceName;
        private TextView tvDeviceId;
        private TextView tvBindTime;
        private TextView tvStatus;
        private TextView btnDetail;
        private TextView btnUnbind;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDeviceName = itemView.findViewById(R.id.tv_device_name);
            tvDeviceId = itemView.findViewById(R.id.tv_device_id);
            tvBindTime = itemView.findViewById(R.id.tv_bind_time);
            tvStatus = itemView.findViewById(R.id.tv_status);
            btnDetail = itemView.findViewById(R.id.btn_detail);
            btnUnbind = itemView.findViewById(R.id.btn_unbind);
        }

        public void bind(Device device) {
            tvDeviceName.setText(device.getName());
            tvDeviceId.setText(device.getFormattedDeviceId());
            tvBindTime.setText(device.getFormattedBindTime());
            tvStatus.setText(device.getStatusText());

            if (device.isOnline()) {
                tvStatus.setBackgroundResource(R.drawable.bg_tag_confirmed);
            } else {
                tvStatus.setBackgroundResource(R.drawable.bg_tag_pending);
            }

            btnDetail.setOnClickListener(v -> {
                if (listener != null) listener.onDetail(device);
            });

            btnUnbind.setOnClickListener(v -> {
                if (listener != null) listener.onUnbind(device);
            });
        }
    }
}
