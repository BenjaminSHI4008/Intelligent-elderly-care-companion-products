package com.xiaoban.app.network;

import android.content.Context;

import com.xiaoban.app.model.Conversation;
import com.xiaoban.app.model.DailyReport;
import com.xiaoban.app.model.DashboardData;
import com.xiaoban.app.model.Reminder;

import java.util.List;
import java.util.Map;

/**
 * Single entry point for all child-side reads against the real backend.
 *
 * <p>Centralizing these calls keeps {@code elderId} an explicit parameter on
 * every data operation (no hardcoded ids) and gives one place to evolve caching
 * or multi-elder switching later. This is intentionally a thin wrapper over
 * {@link ApiService} to stay consistent with the existing Activity + callback
 * architecture rather than introducing a heavier framework.
 */
public final class ChildRepository {

    private final ApiService api;

    private ChildRepository(Context context) {
        this.api = ApiClient.getInstance(context).getApi();
    }

    public static ChildRepository from(Context context) {
        return new ChildRepository(context);
    }

    public void loadDashboard(long elderId, String date, ApiCallback<DashboardData> cb) {
        api.getChildDashboard(elderId, date).enqueue(cb);
    }

    public void loadPending(ApiCallback<List<Conversation>> cb) {
        api.getPending().enqueue(cb);
    }

    public void confirmConversation(long conversationId, ApiCallback<Void> cb) {
        api.confirm(Map.of("conversationId", conversationId)).enqueue(cb);
    }

    public void correctConversation(long conversationId, String correction, ApiCallback<Void> cb) {
        api.correct(Map.of("conversationId", conversationId, "correction", correction)).enqueue(cb);
    }

    public void loadDailyReport(long elderId, String date, ApiCallback<List<DailyReport>> cb) {
        api.getDailyReport(elderId, date).enqueue(cb);
    }

    public void loadReminders(long elderId, ApiCallback<List<Reminder>> cb) {
        api.getReminders(elderId).enqueue(cb);
    }

    public void createReminder(long elderId, String content, String remindTime, String repeatType,
                               ApiCallback<Void> cb) {
        Map<String, Object> body = Map.of(
                "elderId", elderId,
                "content", content,
                "remindTime", remindTime,
                "repeatType", repeatType);
        api.createReminder(body).enqueue(cb);
    }

    public void toggleReminder(long reminderId, ApiCallback<Void> cb) {
        api.toggleReminder(reminderId).enqueue(cb);
    }

    public void deleteReminder(long reminderId, ApiCallback<Void> cb) {
        api.deleteReminder(reminderId).enqueue(cb);
    }
}
