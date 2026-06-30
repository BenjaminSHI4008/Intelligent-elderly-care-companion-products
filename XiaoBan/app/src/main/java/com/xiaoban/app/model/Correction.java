package com.xiaoban.app.model;

public class Correction {
    private String id;
    private String elderQuestion;
    private String aiAnswer;
    private String time;
    private String status;
    private String correctedAnswer;

    public Correction(String id, String elderQuestion, String aiAnswer, String time, String status) {
        this.id = id;
        this.elderQuestion = elderQuestion;
        this.aiAnswer = aiAnswer;
        this.time = time;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getElderQuestion() { return elderQuestion; }
    public void setElderQuestion(String elderQuestion) { this.elderQuestion = elderQuestion; }
    public String getAiAnswer() { return aiAnswer; }
    public void setAiAnswer(String aiAnswer) { this.aiAnswer = aiAnswer; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCorrectedAnswer() { return correctedAnswer; }
    public void setCorrectedAnswer(String correctedAnswer) { this.correctedAnswer = correctedAnswer; }
}
