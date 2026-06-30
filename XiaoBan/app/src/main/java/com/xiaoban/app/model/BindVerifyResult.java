package com.xiaoban.app.model;

public class BindVerifyResult {

    private long elderId;
    private String elderNickname;
    private String bindTime;

    public long getElderId() { return elderId; }
    public void setElderId(long elderId) { this.elderId = elderId; }

    public String getElderNickname() { return elderNickname; }
    public void setElderNickname(String elderNickname) { this.elderNickname = elderNickname; }

    public String getBindTime() { return bindTime; }
    public void setBindTime(String bindTime) { this.bindTime = bindTime; }

    public String getDisplayName() {
        if (elderNickname != null && !elderNickname.isEmpty()) {
            return elderNickname;
        }
        return "老人账户";
    }
}
