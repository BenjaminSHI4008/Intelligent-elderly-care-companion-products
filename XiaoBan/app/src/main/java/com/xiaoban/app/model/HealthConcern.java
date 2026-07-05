package com.xiaoban.app.model;

public class HealthConcern {
    private String level;
    private String title;
    private String detail;

    public HealthConcern(String level, String title, String detail) {
        this.level = level;
        this.title = title;
        this.detail = detail;
    }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
}
