package com.xiaoban.app.model;

public class Topic {
    private String icon;
    private String name;
    private String detail;
    private int count;

    public Topic(String icon, String name, String detail, int count) {
        this.icon = icon;
        this.name = name;
        this.detail = detail;
        this.count = count;
    }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
}
