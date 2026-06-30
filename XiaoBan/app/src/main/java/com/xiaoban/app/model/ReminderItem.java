package com.xiaoban.app.model;

public class ReminderItem {
    private String id;
    private String title;
    private String content;
    private String time;
    private String repeatType;
    private String repeatDays;
    private boolean enabled;

    public ReminderItem() {}

    public ReminderItem(String id, String title, String content, String time,
                        String repeatType, String repeatDays, boolean enabled) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.time = time;
        this.repeatType = repeatType;
        this.repeatDays = repeatDays;
        this.enabled = enabled;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getRepeatType() { return repeatType; }
    public void setRepeatType(String repeatType) { this.repeatType = repeatType; }
    public String getRepeatDays() { return repeatDays; }
    public void setRepeatDays(String repeatDays) { this.repeatDays = repeatDays; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getRepeatTypeText() {
        if (repeatType == null) return "未知";
        switch (repeatType) {
            case "once": return "仅一次";
            case "daily": return "每天";
            case "weekly": return getWeeklyText();
            case "monthly": return "每月";
            default: return "未知";
        }
    }

    private String getWeeklyText() {
        if (repeatDays == null || repeatDays.isEmpty()) return "每周";
        String[] dayNames = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        String[] days = repeatDays.split(",");
        StringBuilder sb = new StringBuilder();
        for (String day : days) {
            try {
                int dayIndex = Integer.parseInt(day.trim()) - 1;
                if (dayIndex >= 0 && dayIndex < dayNames.length) {
                    if (sb.length() > 0) sb.append("、");
                    sb.append(dayNames[dayIndex]);
                }
            } catch (NumberFormatException e) {}
        }
        return sb.length() > 0 ? sb.toString() : "每周";
    }
}
