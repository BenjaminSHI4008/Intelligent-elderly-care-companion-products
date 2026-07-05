package com.xiaoban.app.model;

import com.google.gson.annotations.SerializedName;

public class Reminder {

    @SerializedName("id")
    private long id;

    @SerializedName("childId")
    private long childId;

    @SerializedName("elderId")
    private long elderId;

    @SerializedName("content")
    private String content;

    @SerializedName("remindTime")
    private String remindTime;

    @SerializedName("repeatType")
    private String repeatType;

    @SerializedName("isActive")
    private int isActive;

    public long getId() { return id; }
    public long getChildId() { return childId; }
    public long getElderId() { return elderId; }
    public String getContent() { return content; }
    public String getRemindTime() { return remindTime; }
    public String getRepeatType() { return repeatType; }
    public int getIsActive() { return isActive; }
}
