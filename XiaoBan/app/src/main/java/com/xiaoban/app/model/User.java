package com.xiaoban.app.model;

import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("token")
    private String token;

    @SerializedName("userId")
    private long userId;

    @SerializedName("role")
    private String role;

    @SerializedName("nickname")
    private String nickname;

    @SerializedName("phone")
    private String phone;

    @SerializedName("avatarUrl")
    private String avatarUrl;

    public String getToken() { return token; }
    public long getUserId() { return userId; }
    public String getRole() { return role; }
    public String getNickname() { return nickname; }
    public String getPhone() { return phone; }
    public String getAvatarUrl() { return avatarUrl; }
}
