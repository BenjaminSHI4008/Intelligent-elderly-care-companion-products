package com.xiaoban.app.model;

import com.google.gson.annotations.SerializedName;

public class Conversation {

    @SerializedName("id")
    private long id;

    @SerializedName("elderId")
    private long elderId;

    @SerializedName("sessionId")
    private String sessionId;

    @SerializedName("userQuestion")
    private String userQuestion;

    @SerializedName("aiAnswer")
    private String aiAnswer;

    @SerializedName("category")
    private String category;

    @SerializedName("confirmStatus")
    private String confirmStatus;

    @SerializedName("childCorrection")
    private String childCorrection;

    @SerializedName("createdAt")
    private String createdAt;

    public long getId() { return id; }
    public long getElderId() { return elderId; }
    public String getSessionId() { return sessionId; }
    public String getUserQuestion() { return userQuestion; }
    public String getAiAnswer() { return aiAnswer; }
    public String getCategory() { return category; }
    public String getConfirmStatus() { return confirmStatus; }
    public String getChildCorrection() { return childCorrection; }
    public String getCreatedAt() { return createdAt; }
}
