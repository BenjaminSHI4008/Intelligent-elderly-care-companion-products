package com.xiaoban.server.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChildDashboardVO {

    private ElderInfo elder;
    private TodayStatus todayStatus;
    private DailySummary dailySummary;
    private ReminderSummary reminderSummary;
    private MessageSummary messageSummary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ElderInfo {
        private Long elderId;
        private String nickname;
        private Boolean bound;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TodayStatus {
        private Integer chatCount;
        private Integer moodScore;
        private String moodLabel;
        private LocalDateTime lastActiveAt;
        private Integer pendingCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailySummary {
        private Long reportId;
        private LocalDate reportDate;
        private Boolean generated;
        private String summaryText;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReminderSummary {
        private Integer activeCount;
        private Integer todayCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageSummary {
        private Integer unreadCount;
    }
}
