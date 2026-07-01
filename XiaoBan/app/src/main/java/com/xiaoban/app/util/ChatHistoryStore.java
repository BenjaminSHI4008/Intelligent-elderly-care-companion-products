package com.xiaoban.app.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.xiaoban.app.base.Constants;
import com.xiaoban.app.model.ChatHistorySession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatHistoryStore {

    private static final String KEY_CHAT_HISTORY = "chat_history_sessions";
    private static final int MAX_SESSIONS = 50;
    private static final int MAX_MESSAGES_PER_SESSION = 100;
    private static final SimpleDateFormat DATE_TIME_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

    private ChatHistoryStore() {
    }

    public static List<ChatHistorySession> getSessions(Context context) {
        JSONArray sessions = readSessions(context);
        List<ChatHistorySession> result = new ArrayList<>();
        for (int i = 0; i < sessions.length(); i++) {
            JSONObject sessionJson = sessions.optJSONObject(i);
            if (sessionJson != null) {
                result.add(toSession(sessionJson));
            }
        }
        return result;
    }

    public static ChatHistorySession getSession(Context context, String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return null;
        }

        JSONArray sessions = readSessions(context);
        for (int i = 0; i < sessions.length(); i++) {
            JSONObject sessionJson = sessions.optJSONObject(i);
            if (sessionJson != null && sessionId.equals(sessionJson.optString("sessionId"))) {
                return toSession(sessionJson);
            }
        }
        return null;
    }

    public static void addExchange(Context context, String sessionId, String userQuestion,
                                   String aiAnswer, String category) {
        if (sessionId == null || sessionId.isEmpty() || userQuestion == null || aiAnswer == null) {
            return;
        }

        try {
            JSONArray sessions = readSessions(context);
            JSONObject currentSession = null;
            JSONArray nextSessions = new JSONArray();

            for (int i = 0; i < sessions.length(); i++) {
                JSONObject sessionJson = sessions.optJSONObject(i);
                if (sessionJson == null) {
                    continue;
                }
                if (sessionId.equals(sessionJson.optString("sessionId"))) {
                    currentSession = sessionJson;
                } else if (nextSessions.length() < MAX_SESSIONS - 1) {
                    nextSessions.put(sessionJson);
                }
            }

            String now = DATE_TIME_FORMAT.format(new Date());
            if (currentSession == null) {
                currentSession = new JSONObject();
                currentSession.put("sessionId", sessionId);
                currentSession.put("title", buildTitle(userQuestion, aiAnswer));
                currentSession.put("createdAt", now);
                currentSession.put("messages", new JSONArray());
            } else if (currentSession.optString("title").isEmpty()) {
                currentSession.put("title", buildTitle(userQuestion, aiAnswer));
            }

            currentSession.put("updatedAt", now);

            JSONArray messages = currentSession.optJSONArray("messages");
            if (messages == null) {
                messages = new JSONArray();
            }
            JSONObject message = new JSONObject();
            message.put("userQuestion", userQuestion);
            message.put("aiAnswer", aiAnswer);
            message.put("category", category == null ? "normal" : category);
            message.put("createdAt", now);
            messages.put(message);

            JSONArray trimmedMessages = new JSONArray();
            int start = Math.max(0, messages.length() - MAX_MESSAGES_PER_SESSION);
            for (int i = start; i < messages.length(); i++) {
                trimmedMessages.put(messages.get(i));
            }
            currentSession.put("messages", trimmedMessages);

            JSONArray reorderedSessions = new JSONArray();
            reorderedSessions.put(currentSession);
            for (int i = 0; i < nextSessions.length(); i++) {
                reorderedSessions.put(nextSessions.get(i));
            }

            getPreferences(context).edit()
                    .putString(KEY_CHAT_HISTORY, reorderedSessions.toString())
                    .apply();
        } catch (JSONException ignored) {
        }
    }

    public static String formatDisplayTime(String value) {
        if (value == null || value.length() < 16) {
            return "";
        }
        return value.substring(5, 16);
    }

    private static ChatHistorySession toSession(JSONObject json) {
        JSONArray messageArray = json.optJSONArray("messages");
        List<ChatHistorySession.Message> messages = new ArrayList<>();
        if (messageArray != null) {
            for (int i = 0; i < messageArray.length(); i++) {
                JSONObject messageJson = messageArray.optJSONObject(i);
                if (messageJson == null) {
                    continue;
                }
                messages.add(new ChatHistorySession.Message(
                        messageJson.optString("userQuestion"),
                        messageJson.optString("aiAnswer"),
                        messageJson.optString("category", "normal"),
                        messageJson.optString("createdAt")
                ));
            }
        }

        return new ChatHistorySession(
                json.optString("sessionId"),
                json.optString("title", "新的对话"),
                json.optString("createdAt"),
                json.optString("updatedAt"),
                messages
        );
    }

    private static JSONArray readSessions(Context context) {
        String raw = getPreferences(context).getString(KEY_CHAT_HISTORY, "[]");
        try {
            return new JSONArray(raw);
        } catch (JSONException e) {
            return new JSONArray();
        }
    }

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE);
    }

    private static String buildTitle(String userQuestion, String aiAnswer) {
        String text = cleanTitleSource(userQuestion);
        if (text.contains("降压药") && (text.contains("什么时候") || text.contains("时间")
                || text.contains("吃") || text.contains("服"))) {
            return "降压药服用时间";
        }
        if (text.contains("吃药") || text.contains("用药") || text.contains("药")) {
            return trimTitle(text, "用药咨询");
        }
        if (text.contains("疼") || text.contains("痛") || text.contains("不舒服")) {
            return trimTitle(text, "身体不适咨询");
        }
        if (text.contains("睡") || text.contains("失眠")) {
            return trimTitle(text, "睡眠问题");
        }
        if (text.isEmpty()) {
            text = cleanTitleSource(aiAnswer);
        }
        return trimTitle(text, "新的对话");
    }

    private static String cleanTitleSource(String source) {
        if (source == null) {
            return "";
        }
        return source
                .replace("小伴", "")
                .replace("请问", "")
                .replace("我想问", "")
                .replace("我想知道", "")
                .replace("帮我", "")
                .replace("一下", "")
                .replaceAll("[\\s，。！？!?、：:；;,.]+", "")
                .trim();
    }

    private static String trimTitle(String text, String fallback) {
        if (text == null || text.isEmpty()) {
            return fallback;
        }
        if (text.length() <= 12) {
            return text;
        }
        return text.substring(0, 12);
    }
}
