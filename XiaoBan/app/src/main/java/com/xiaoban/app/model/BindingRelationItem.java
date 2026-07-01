package com.xiaoban.app.model;

public class BindingRelationItem {

    private long id;
    private long elderId;
    private long childId;
    private String elderNickname;
    private String childNickname;
    private String elderPhoneMasked;
    private String childPhoneMasked;
    private String bindType;
    private String bindTypeLabel;
    private String status;
    private String bindTime;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getElderId() { return elderId; }
    public void setElderId(long elderId) { this.elderId = elderId; }

    public long getChildId() { return childId; }
    public void setChildId(long childId) { this.childId = childId; }

    public String getElderNickname() { return elderNickname; }
    public void setElderNickname(String elderNickname) { this.elderNickname = elderNickname; }

    public String getChildNickname() { return childNickname; }
    public void setChildNickname(String childNickname) { this.childNickname = childNickname; }

    public String getElderPhoneMasked() { return elderPhoneMasked; }
    public void setElderPhoneMasked(String elderPhoneMasked) { this.elderPhoneMasked = elderPhoneMasked; }

    public String getChildPhoneMasked() { return childPhoneMasked; }
    public void setChildPhoneMasked(String childPhoneMasked) { this.childPhoneMasked = childPhoneMasked; }

    public String getBindType() { return bindType; }
    public void setBindType(String bindType) { this.bindType = bindType; }

    public String getBindTypeLabel() { return bindTypeLabel; }
    public void setBindTypeLabel(String bindTypeLabel) { this.bindTypeLabel = bindTypeLabel; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getBindTime() { return bindTime; }
    public void setBindTime(String bindTime) { this.bindTime = bindTime; }

    public String getDisplayNameForElder() {
        if (childNickname != null && !childNickname.isEmpty()) {
            return childNickname;
        }
        if (childPhoneMasked != null && !childPhoneMasked.isEmpty()) {
            return childPhoneMasked;
        }
        return "家人";
    }

    public String getDisplayNameForChild() {
        if (elderNickname != null && !elderNickname.isEmpty()) {
            return elderNickname;
        }
        if (elderPhoneMasked != null && !elderPhoneMasked.isEmpty()) {
            return elderPhoneMasked;
        }
        return "老人账户";
    }

    public String getFormattedBindTime() {
        if (bindTime == null || bindTime.isEmpty()) {
            return "未知";
        }
        return bindTime.length() >= 10 ? "绑定于 " + bindTime.substring(0, 10) : bindTime;
    }
}
