package com.xiaoban.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.xiaoban.app.R;
import com.xiaoban.app.model.ReminderItem;
import java.util.ArrayList;
import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ViewHolder> {

    private List<ReminderItem> reminders = new ArrayList<>();
    private OnReminderActionListener listener;

    public interface OnReminderActionListener {
        void onToggle(ReminderItem reminder, boolean enabled);
        void onEdit(ReminderItem reminder);
        void onDelete(ReminderItem reminder);
    }

    public void setOnActionListener(OnReminderActionListener listener) {
        this.listener = listener;
    }

    public void setReminders(List<ReminderItem> reminders) {
        this.reminders = reminders != null ? reminders : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addReminder(ReminderItem reminder) {
        reminders.add(0, reminder);
        notifyItemInserted(0);
    }

    public void updateReminder(ReminderItem reminder) {
        for (int i = 0; i < reminders.size(); i++) {
            if (reminders.get(i).getId().equals(reminder.getId())) {
                reminders.set(i, reminder);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void removeReminder(String reminderId) {
        for (int i = 0; i < reminders.size(); i++) {
            if (reminders.get(i).getId().equals(reminderId)) {
                reminders.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reminder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReminderItem reminder = reminders.get(position);
        holder.bind(reminder);
    }

    @Override
    public int getItemCount() {
        return reminders.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;
        private TextView tvTime;
        private TextView tvContent;
        private TextView tvRepeat;
        private SwitchCompat switchEnabled;
        private TextView btnEdit;
        private TextView btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvRepeat = itemView.findViewById(R.id.tv_repeat);
            switchEnabled = itemView.findViewById(R.id.switch_enabled);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }

        public void bind(ReminderItem reminder) {
            tvTitle.setText(reminder.getTitle());
            tvTime.setText(reminder.getTime());
            tvContent.setText(reminder.getContent());
            tvRepeat.setText(reminder.getRepeatTypeText());
            switchEnabled.setChecked(reminder.isEnabled());

            float alpha = reminder.isEnabled() ? 1.0f : 0.5f;
            tvTitle.setAlpha(alpha);
            tvTime.setAlpha(alpha);
            tvContent.setAlpha(alpha);
            tvRepeat.setAlpha(alpha);

            switchEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onToggle(reminder, isChecked);
                }
            });

            btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEdit(reminder);
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(reminder);
            });
        }
    }
}
