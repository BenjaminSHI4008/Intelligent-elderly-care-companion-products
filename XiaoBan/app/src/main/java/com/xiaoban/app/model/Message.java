package com.xiaoban.app.model;

import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Message {

    @SerializedName("id")
    private long id;

    @SerializedName("senderId")
    private long senderId;

    @SerializedName("receiverId")
    private long receiverId;

    @SerializedName("msgType")
    private String msgType;

    @SerializedName("content")
    private String content;

    @SerializedName("mediaUrl")
    private String mediaUrl;

    @SerializedName("duration")
    private int duration;

    @SerializedName("isRead")
    private int isRead;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("senderName")
    private String senderName;

    @SerializedName("status")
    private int status;

    @SerializedName("recalled")
    private boolean recalled;

    private static final long RECALL_WINDOW_MS = 2 * 60 * 1000L;
    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

    public static Message mock(long id, String senderName, String msgType, String content,
                               int duration, boolean isRead, String createdAt) {
        Message message = new Message();
        message.id = id;
        message.senderName = senderName;
        message.msgType = msgType;
        message.content = content;
        message.duration = duration;
        message.isRead = isRead ? 1 : 0;
        message.createdAt = createdAt;
        return message;
    }

    public long getId() { return id; }
    public long getSenderId() { return senderId; }
    public long getReceiverId() { return receiverId; }
    public String getMsgType() { return msgType; }
    public String getContent() { return content; }
    public String getMediaUrl() { return mediaUrl; }
    public int getDuration() { return duration; }
    public int getIsRead() { return isRead; }
    public String getCreatedAt() { return createdAt; }

    public String getSenderName() {
        return senderName != null ? senderName : "家人";
    }

    public String getType() {
        if (msgType == null) {
            return "text";
        }
        switch (msgType) {
            case "voice":
                return "voice";
            case "photo":
            case "image":
                return "photo";
            default:
                return "text";
        }
    }

    public boolean isRead() {
        return isRead == 1;
    }

    public void setRead(boolean read) {
        this.isRead = read ? 1 : 0;
    }

    public Date getCreateTime() {
        if (createdAt == null) {
            return new Date();
        }
        try {
            return DATE_FORMAT.parse(createdAt);
        } catch (ParseException ignored) {
        }
        try {
            if (createdAt.contains("T")) {
                SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.CHINA);
                return iso.parse(createdAt.substring(0, Math.min(19, createdAt.length())));
            }
        } catch (ParseException ignored) {
        }
        return new Date();
    }

    public void setSenderName(String name) {
        this.senderName = name;
    }

    public int getStatus() { return status; }

    public boolean isRecalled() {
        return recalled || status == 1;
    }

    public boolean canRecall(long currentUserId) {
        if (isRecalled() || senderId != currentUserId) {
            return false;
        }
        return System.currentTimeMillis() - getCreateTime().getTime() <= RECALL_WINDOW_MS;
    }

    public String getRecallDisplayText(boolean isMine) {
        return isMine ? "你撤回了一条消息" : "对方撤回了一条消息";
    }
}
