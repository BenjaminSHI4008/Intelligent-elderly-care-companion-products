package com.xiaoban.app.model;

import com.google.gson.annotations.SerializedName;

/**
 * Child-home dashboard payload from GET /api/child/dashboard.
 * Nullable fields (summaryText, moodScore, lastActiveAt) indicate "no data yet"
 * and must be rendered as empty state — never replaced with fake placeholders.
 */
public class DashboardData {

    @SerializedName("elderId")
    private long elderId;

    @SerializedName("elderNickname")
    private String elderNickname;

    @SerializedName("todayChatCount")
    private Integer todayChatCount;

    @SerializedName("lastActiveAt")
    private String lastActiveAt;

    @SerializedName("pendingCount")
    private Integer pendingCount;

    @SerializedName("summaryText")
    private String summaryText;

    @SerializedName("moodScore")
    private Integer moodScore;

    @SerializedName("activeReminderCount")
    private Integer activeReminderCount;

    @SerializedName("unreadMessageCount")
    private Integer unreadMessageCount;

    public long getElderId() { return elderId; }
    public String getElderNickname() { return elderNickname; }
    public Integer getTodayChatCount() { return todayChatCount; }
    public String getLastActiveAt() { return lastActiveAt; }
    public Integer getPendingCount() { return pendingCount; }
    public String getSummaryText() { return summaryText; }
    public Integer getMoodScore() { return moodScore; }
    public Integer getActiveReminderCount() { return activeReminderCount; }
    public Integer getUnreadMessageCount() { return unreadMessageCount; }
}
