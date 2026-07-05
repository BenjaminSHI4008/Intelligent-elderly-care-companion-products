package com.xiaoban.app.model;

import com.google.gson.annotations.SerializedName;

public class DailyReport {

    @SerializedName("id")
    private long id;

    @SerializedName("elderId")
    private long elderId;

    @SerializedName("reportDate")
    private String reportDate;

    @SerializedName("totalConversations")
    private int totalConversations;

    @SerializedName("summaryText")
    private String summaryText;

    @SerializedName("healthFlags")
    private String healthFlags;

    @SerializedName("moodScore")
    private int moodScore;

    @SerializedName("topicDistribution")
    private String topicDistribution;

    public long getId() { return id; }
    public long getElderId() { return elderId; }
    public String getReportDate() { return reportDate; }
    public int getTotalConversations() { return totalConversations; }
    public String getSummaryText() { return summaryText; }
    public String getHealthFlags() { return healthFlags; }
    public int getMoodScore() { return moodScore; }
    public String getTopicDistribution() { return topicDistribution; }
}
