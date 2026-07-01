package com.xiaoban.app.model;

public class GenerateCodeResponse {

    private String code;
    private String expireAt;
    private int expiresInSeconds;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getExpireAt() { return expireAt; }
    public void setExpireAt(String expireAt) { this.expireAt = expireAt; }

    public int getExpiresInSeconds() { return expiresInSeconds; }
    public void setExpiresInSeconds(int expiresInSeconds) { this.expiresInSeconds = expiresInSeconds; }
}
