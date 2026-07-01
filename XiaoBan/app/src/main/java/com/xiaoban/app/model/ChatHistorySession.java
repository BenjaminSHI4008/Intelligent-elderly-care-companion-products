package com.xiaoban.app.model;

import java.util.ArrayList;
import java.util.List;

public class ChatHistorySession {

    private final String sessionId;
    private final String title;
    private final String createdAt;
    private final String updatedAt;
    private final List<Message> messages;

    public ChatHistorySession(String sessionId, String title, String createdAt,
                              String updatedAt, List<Message> messages) {
        this.sessionId = sessionId;
        this.title = title;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.messages = messages != null ? messages : new ArrayList<>();
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getTitle() {
        return title;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public int getMessageCount() {
        return messages.size();
    }

    public String getPreview() {
        if (messages.isEmpty()) {
            return "暂无对话内容";
        }
        return messages.get(0).getUserQuestion();
    }

    public static class Message {
        private final String userQuestion;
        private final String aiAnswer;
        private final String category;
        private final String createdAt;

        public Message(String userQuestion, String aiAnswer, String category, String createdAt) {
            this.userQuestion = userQuestion;
            this.aiAnswer = aiAnswer;
            this.category = category;
            this.createdAt = createdAt;
        }

        public String getUserQuestion() {
            return userQuestion;
        }

        public String getAiAnswer() {
            return aiAnswer;
        }

        public String getCategory() {
            return category;
        }

        public String getCreatedAt() {
            return createdAt;
        }
    }
}
