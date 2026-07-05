package com.xiaoban.app.model;

import com.google.gson.annotations.SerializedName;

public class ChildDashboard {

    @SerializedName("elder")
    private ElderInfo elder;

    @SerializedName("todayStatus")
    private TodayStatus todayStatus;

    @SerializedName("dailySummary")
    private DailySummary dailySummary;

    @SerializedName("reminderSummary")
    private ReminderSummary reminderSummary;

    @SerializedName("messageSummary")
    private MessageSummary messageSummary;

    public ElderInfo getElder() { return elder; }
    public TodayStatus getTodayStatus() { return todayStatus; }
    public DailySummary getDailySummary() { return dailySummary; }
    public ReminderSummary getReminderSummary() { return reminderSummary; }
    public MessageSummary getMessageSummary() { return messageSummary; }

    public static class ElderInfo {
        @SerializedName("elderId")
        private long elderId;
        @SerializedName("nickname")
        private String nickname;
        @SerializedName("bound")
        private boolean bound;

        public long getElderId() { return elderId; }
        public String getNickname() { return nickname; }
        public boolean isBound() { return bound; }
    }

    public static class TodayStatus {
        @SerializedName("chatCount")
        private int chatCount;
        @SerializedName("moodScore")
        private Integer moodScore;
        @SerializedName("moodLabel")
        private String moodLabel;
        @SerializedName("lastActiveAt")
        private String lastActiveAt;
        @SerializedName("pendingCount")
        private int pendingCount;

        public int getChatCount() { return chatCount; }
        public Integer getMoodScore() { return moodScore; }
        public String getMoodLabel() { return moodLabel; }
        public String getLastActiveAt() { return lastActiveAt; }
        public int getPendingCount() { return pendingCount; }
    }

    public static class DailySummary {
        @SerializedName("reportId")
        private Long reportId;
        @SerializedName("reportDate")
        private String reportDate;
        @SerializedName("generated")
        private boolean generated;
        @SerializedName("summaryText")
        private String summaryText;

        public Long getReportId() { return reportId; }
        public String getReportDate() { return reportDate; }
        public boolean isGenerated() { return generated; }
        public String getSummaryText() { return summaryText; }
    }

    public static class ReminderSummary {
        @SerializedName("activeCount")
        private int activeCount;
        @SerializedName("todayCount")
        private int todayCount;

        public int getActiveCount() { return activeCount; }
        public int getTodayCount() { return todayCount; }
    }

    public static class MessageSummary {
        @SerializedName("unreadCount")
        private int unreadCount;

        public int getUnreadCount() { return unreadCount; }
    }
}
