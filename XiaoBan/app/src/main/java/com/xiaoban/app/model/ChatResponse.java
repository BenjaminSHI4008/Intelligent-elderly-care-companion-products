package com.xiaoban.app.model;

import com.google.gson.annotations.SerializedName;

public class ChatResponse {

    @SerializedName("answer")
    private String answer;

    @SerializedName("category")
    private String category;

    @SerializedName("safetyTip")
    private String safetyTip;

    @SerializedName("conversationId")
    private long conversationId;

    public String getAnswer() { return answer; }
    public String getCategory() { return category; }
    public String getSafetyTip() { return safetyTip; }
    public long getConversationId() { return conversationId; }
}
