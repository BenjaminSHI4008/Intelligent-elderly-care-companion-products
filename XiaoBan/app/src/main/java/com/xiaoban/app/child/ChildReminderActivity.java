package com.xiaoban.app.child;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xiaoban.app.R;
import com.xiaoban.app.adapter.ReminderAdapter;
import com.xiaoban.app.base.BaseActivity;
import com.xiaoban.app.model.Reminder;
import com.xiaoban.app.model.ReminderItem;
import com.xiaoban.app.network.ApiCallback;
import com.xiaoban.app.network.ApiClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChildReminderActivity extends BaseActivity {

    private RecyclerView rvReminders;
    private LinearLayout emptyView;
    private TextView btnAdd;
    private ImageView ivBack;

    private ReminderAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_reminder);

        initViews();
        initAdapter();
        loadReminders();
    }

    private void initViews() {
        rvReminders = findViewById(R.id.rv_reminders);
        emptyView = findViewById(R.id.empty_view);
        btnAdd = findViewById(R.id.btn_add);
        ivBack = findViewById(R.id.iv_back);

        ivBack.setOnClickListener(v -> finish());
        btnAdd.setOnClickListener(v -> showAddReminderDialog());
    }

    private void initAdapter() {
        adapter = new ReminderAdapter();
        rvReminders.setLayoutManager(new LinearLayoutManager(this));
        rvReminders.setAdapter(adapter);

        adapter.setOnActionListener(new ReminderAdapter.OnReminderActionListener() {
            @Override
            public void onToggle(ReminderItem reminder, boolean enabled) {
                updateReminderStatus(reminder, enabled);
            }

            @Override
            public void onEdit(ReminderItem reminder) {
                showEditReminderDialog(reminder);
            }

            @Override
            public void onDelete(ReminderItem reminder) {
                showDeleteConfirmDialog(reminder);
            }
        });
    }

    private void loadReminders() {
        long elderId = 1L;
        ApiClient.getInstance(this).getApi().getReminders(elderId)
            .enqueue(new ApiCallback<List<Reminder>>() {
                @Override
                public void onSuccess(List<Reminder> data) {
                    runOnUiThread(() -> {
                        List<ReminderItem> reminders = convertReminders(data);
                        adapter.setReminders(reminders);
                        updateEmptyView(reminders.isEmpty());
                    });
                }
            });
    }

    private List<ReminderItem> convertReminders(List<Reminder> apiReminders) {
        List<ReminderItem> result = new ArrayList<>();
        if (apiReminders == null) return result;
        for (Reminder r : apiReminders) {
            ReminderItem item = new ReminderItem(
                String.valueOf(r.getId()),
                r.getContent(),
                r.getContent(),
                r.getRemindTime(),
                r.getRepeatType(),
                "",
                r.getIsActive() == 1
            );
            result.add(item);
        }
        return result;
    }

    private void updateReminderStatus(ReminderItem reminder, boolean enabled) {
        long id = Long.parseLong(reminder.getId());
        ApiClient.getInstance(this).getApi().toggleReminder(id)
            .enqueue(new ApiCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    runOnUiThread(() -> {
                        reminder.setEnabled(enabled);
                        adapter.updateReminder(reminder);
                        Toast.makeText(ChildReminderActivity.this,
                                enabled ? "已开启" : "已关闭",
                                Toast.LENGTH_SHORT).show();
                    });
                }
            });
    }

    private void showAddReminderDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_reminder, null);
        EditText etTitle = dialogView.findViewById(R.id.et_title);
        EditText etContent = dialogView.findViewById(R.id.et_content);
        TextView tvTime = dialogView.findViewById(R.id.tv_time);
        TextView tvRepeat = dialogView.findViewById(R.id.tv_repeat);

        tvTime.setText("08:00");
        tvTime.setOnClickListener(v -> showTimePicker(tvTime));

        String[] repeatTypes = {"仅一次", "每天", "每周", "每月"};
        String[] repeatValues = {"once", "daily", "weekly", "monthly"};
        final int[] selectedRepeat = {1};

        tvRepeat.setText(repeatTypes[selectedRepeat[0]]);
        tvRepeat.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("选择重复方式")
                    .setSingleChoiceItems(repeatTypes, selectedRepeat[0], (dialog, which) -> {
                        selectedRepeat[0] = which;
                        tvRepeat.setText(repeatTypes[which]);
                        dialog.dismiss();
                    })
                    .show();
        });

        new AlertDialog.Builder(this)
                .setTitle("添加提醒")
                .setView(dialogView)
                .setPositiveButton("确定", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String content = etContent.getText().toString().trim();
                    String time = tvTime.getText().toString();
                    String repeatType = repeatValues[selectedRepeat[0]];

                    if (title.isEmpty()) {
                        Toast.makeText(this, "请输入提醒标题", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    createReminder(title, content, time, repeatType);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showEditReminderDialog(ReminderItem reminder) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_reminder, null);
        EditText etTitle = dialogView.findViewById(R.id.et_title);
        EditText etContent = dialogView.findViewById(R.id.et_content);
        TextView tvTime = dialogView.findViewById(R.id.tv_time);
        TextView tvRepeat = dialogView.findViewById(R.id.tv_repeat);

        etTitle.setText(reminder.getTitle());
        etContent.setText(reminder.getContent());
        tvTime.setText(reminder.getTime());
        tvTime.setOnClickListener(v -> showTimePicker(tvTime));

        String[] repeatTypes = {"仅一次", "每天", "每周", "每月"};
        String[] repeatValues = {"once", "daily", "weekly", "monthly"};
        int currentIndex = 0;
        for (int i = 0; i < repeatValues.length; i++) {
            if (repeatValues[i].equals(reminder.getRepeatType())) {
                currentIndex = i;
                break;
            }
        }
        final int[] selectedRepeat = {currentIndex};
        tvRepeat.setText(repeatTypes[selectedRepeat[0]]);

        tvRepeat.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("选择重复方式")
                    .setSingleChoiceItems(repeatTypes, selectedRepeat[0], (dialog, which) -> {
                        selectedRepeat[0] = which;
                        tvRepeat.setText(repeatTypes[which]);
                        dialog.dismiss();
                    })
                    .show();
        });

        new AlertDialog.Builder(this)
                .setTitle("编辑提醒")
                .setView(dialogView)
                .setPositiveButton("保存", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String content = etContent.getText().toString().trim();
                    String time = tvTime.getText().toString();
                    String repeatType = repeatValues[selectedRepeat[0]];

                    if (title.isEmpty()) {
                        Toast.makeText(this, "请输入提醒标题", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    long id = Long.parseLong(reminder.getId());
                    ApiClient.getInstance(this).getApi().deleteReminder(id)
                        .enqueue(new ApiCallback<Void>() {
                            @Override
                            public void onSuccess(Void data) {
                                createReminder(title, content, time, repeatType);
                            }
                        });
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showDeleteConfirmDialog(ReminderItem reminder) {
        new AlertDialog.Builder(this)
                .setTitle("删除提醒")
                .setMessage("确定要删除「" + reminder.getTitle() + "」吗？")
                .setPositiveButton("删除", (dialog, which) -> deleteReminder(reminder))
                .setNegativeButton("取消", null)
                .show();
    }

    private void showTimePicker(TextView tvTime) {
        String currentTime = tvTime.getText().toString();
        String[] parts = currentTime.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);

        new TimePickerDialog(this, (view, hourOfDay, minuteOfHour) -> {
            String time = String.format(Locale.CHINA, "%02d:%02d", hourOfDay, minuteOfHour);
            tvTime.setText(time);
        }, hour, minute, true).show();
    }

    private void createReminder(String title, String content, String time, String repeatType) {
        Map<String, Object> body = new HashMap<>();
        body.put("content", title + "：" + content);
        body.put("remindTime", time);
        body.put("repeatType", repeatType);
        body.put("elderId", 1L);

        ApiClient.getInstance(this).getApi().createReminder(body)
            .enqueue(new ApiCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    runOnUiThread(() -> {
                        Toast.makeText(ChildReminderActivity.this,
                                "添加成功", Toast.LENGTH_SHORT).show();
                        loadReminders();
                    });
                }
            });
    }

    private void deleteReminder(ReminderItem reminder) {
        long id = Long.parseLong(reminder.getId());
        ApiClient.getInstance(this).getApi().deleteReminder(id)
            .enqueue(new ApiCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    runOnUiThread(() -> {
                        adapter.removeReminder(reminder.getId());
                        Toast.makeText(ChildReminderActivity.this,
                                "已删除", Toast.LENGTH_SHORT).show();
                        updateEmptyView(adapter.getItemCount() == 0);
                    });
                }
            });
    }

    private void updateEmptyView(boolean isEmpty) {
        if (isEmpty) {
            rvReminders.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            rvReminders.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }
}
