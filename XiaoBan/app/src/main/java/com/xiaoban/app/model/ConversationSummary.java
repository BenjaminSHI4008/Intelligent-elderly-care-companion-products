package com.xiaoban.app.model;

import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ConversationSummary {

    @SerializedName("peerId")
    private long peerId;

    @SerializedName("peerName")
    private String peerName;

    @SerializedName("lastMsgType")
    private String lastMsgType;

    @SerializedName("lastContent")
    private String lastContent;

    @SerializedName("lastTime")
    private String lastTime;

    @SerializedName("unreadCount")
    private int unreadCount;

    @SerializedName("muted")
    private boolean muted;

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

    public long getPeerId() { return peerId; }
    public String getPeerName() { return peerName; }
    public String getLastMsgType() { return lastMsgType; }
    public String getLastContent() { return lastContent; }
    public String getLastTimeRaw() { return lastTime; }
    public int getUnreadCount() { return unreadCount; }
    public boolean isMuted() { return muted; }

    public Date getLastTime() {
        if (lastTime == null) {
            return new Date();
        }
        try {
            return DATE_FORMAT.parse(lastTime);
        } catch (ParseException ignored) {
        }
        try {
            if (lastTime.contains("T")) {
                SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.CHINA);
                return iso.parse(lastTime.substring(0, Math.min(19, lastTime.length())));
            }
        } catch (ParseException ignored) {
        }
        return new Date();
    }

    public String getPreviewText() {
        if (lastContent != null && lastContent.contains("撤回")) {
            return lastContent;
        }
        if ("photo".equals(lastMsgType)) {
            return "[图片]";
        }
        if ("voice".equals(lastMsgType)) {
            return "[语音]";
        }
        return lastContent != null ? lastContent : "";
    }
}
