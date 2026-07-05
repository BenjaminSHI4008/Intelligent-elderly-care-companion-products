package com.xiaoban.server.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Aggregated child-home dashboard for a single elder on a given day.
 * Every field is derived from real tables; nullable fields (summaryText,
 * moodScore, lastActiveAt) mean "no data yet" and must render as empty state
 * on the client rather than being back-filled with placeholders.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChildDashboardVO {

    private Long elderId;
    private String elderNickname;

    /** conversation (isPrivate=0) count for the day. */
    private Integer todayChatCount;
    /** timestamp of the last non-private conversation on the day, nullable. */
    private LocalDateTime lastActiveAt;
    /** conversations awaiting child confirmation (confirmStatus='pending'). */
    private Integer pendingCount;

    /** latest daily_report summary for the day, nullable until generated. */
    private String summaryText;
    /** mood score 1-5 from daily_report, nullable. */
    private Integer moodScore;

    /** active reminders (isActive=1) for the elder. */
    private Integer activeReminderCount;

    /** unread family_message count for the current child within this family. */
    private Integer unreadMessageCount;
}
