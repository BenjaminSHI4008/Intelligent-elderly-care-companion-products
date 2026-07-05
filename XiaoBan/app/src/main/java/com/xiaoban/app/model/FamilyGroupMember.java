package com.xiaoban.app.model;

import com.google.gson.annotations.SerializedName;

public class FamilyGroupMember {

    @SerializedName("userId")
    private long userId;

    @SerializedName("nickname")
    private String nickname;

    @SerializedName("role")
    private String role;

    @SerializedName("elder")
    private boolean elder;

    public long getUserId() { return userId; }

    public String getNickname() {
        return nickname != null ? nickname : "家人";
    }

    public String getRole() { return role; }

    public boolean isElder() { return elder; }

    public String getAvatarText() {
        String name = getNickname();
        return name.isEmpty() ? "家" : name.substring(0, 1);
    }
}
