package com.xiaoban.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaoban.server.entity.Conversation;
import com.xiaoban.server.entity.DailyReport;
import com.xiaoban.server.entity.FamilyMessage;
import com.xiaoban.server.entity.Reminder;
import com.xiaoban.server.entity.User;
import com.xiaoban.server.mapper.ConversationMapper;
import com.xiaoban.server.mapper.DailyReportMapper;
import com.xiaoban.server.mapper.FamilyMessageMapper;
import com.xiaoban.server.mapper.ReminderMapper;
import com.xiaoban.server.vo.ChildDashboardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChildDashboardService {

    private final FamilyAccessService familyAccessService;
    private final ConversationMapper conversationMapper;
    private final DailyReportMapper dailyReportMapper;
    private final ReminderMapper reminderMapper;
    private final FamilyMessageMapper familyMessageMapper;

    public ChildDashboardVO getDashboard(Long childId, Long elderId, LocalDate date) {
        LocalDate reportDate = date != null ? date : LocalDate.now();
        User child = familyAccessService.requireChild(childId);
        User elder = familyAccessService.requireElder(elderId);
        familyAccessService.assertChildCanAccessElder(child.getUserId(), elder.getUserId());

        LocalDateTime start = reportDate.atStartOfDay();
        LocalDateTime end = reportDate.plusDays(1).atStartOfDay();
        DailyReport report = findReport(elderId, reportDate);

        return ChildDashboardVO.builder()
                .elder(ChildDashboardVO.ElderInfo.builder()
                        .elderId(elder.getUserId())
                        .nickname(elder.getNickname())
                        .bound(true)
                        .build())
                .todayStatus(ChildDashboardVO.TodayStatus.builder()
                        .chatCount(toInt(countPublicConversations(elderId, start, end)))
                        .moodScore(report != null ? report.getMoodScore() : null)
                        .moodLabel(resolveMoodLabel(report != null ? report.getMoodScore() : null))
                        .lastActiveAt(findLastPublicConversationAt(elderId, start, end))
                        .pendingCount(toInt(countPendingConversations(elderId, start, end)))
                        .build())
                .dailySummary(ChildDashboardVO.DailySummary.builder()
                        .reportId(report != null ? report.getId() : null)
                        .reportDate(reportDate)
                        .generated(report != null)
                        .summaryText(report != null ? report.getSummaryText() : null)
                        .build())
                .reminderSummary(ChildDashboardVO.ReminderSummary.builder()
                        .activeCount(toInt(countActiveReminders(elderId)))
                        .todayCount(toInt(countActiveReminders(elderId)))
                        .build())
                .messageSummary(ChildDashboardVO.MessageSummary.builder()
                        .unreadCount(toInt(countUnreadFamilyMessages(childId, elderId)))
                        .build())
                .build();
    }

    private DailyReport findReport(Long elderId, LocalDate date) {
        return dailyReportMapper.selectOne(
                new LambdaQueryWrapper<DailyReport>()
                        .eq(DailyReport::getElderId, elderId)
                        .eq(DailyReport::getReportDate, date)
                        .orderByDesc(DailyReport::getCreatedAt)
                        .orderByDesc(DailyReport::getId)
                        .last("LIMIT 1"));
    }

    private Long countPublicConversations(Long elderId, LocalDateTime start, LocalDateTime end) {
        return conversationMapper.selectCount(
                new LambdaQueryWrapper<Conversation>()
                        .eq(Conversation::getElderId, elderId)
                        .eq(Conversation::getIsPrivate, 0)
                        .between(Conversation::getCreatedAt, start, end));
    }

    private Long countPendingConversations(Long elderId, LocalDateTime start, LocalDateTime end) {
        return conversationMapper.selectCount(
                new LambdaQueryWrapper<Conversation>()
                        .eq(Conversation::getElderId, elderId)
                        .eq(Conversation::getIsPrivate, 0)
                        .eq(Conversation::getConfirmStatus, "pending")
                        .between(Conversation::getCreatedAt, start, end));
    }

    private LocalDateTime findLastPublicConversationAt(Long elderId, LocalDateTime start, LocalDateTime end) {
        Conversation latest = conversationMapper.selectOne(
                new LambdaQueryWrapper<Conversation>()
                        .eq(Conversation::getElderId, elderId)
                        .eq(Conversation::getIsPrivate, 0)
                        .between(Conversation::getCreatedAt, start, end)
                        .orderByDesc(Conversation::getCreatedAt)
                        .orderByDesc(Conversation::getId)
                        .last("LIMIT 1"));
        return latest != null ? latest.getCreatedAt() : null;
    }

    private Long countActiveReminders(Long elderId) {
        return reminderMapper.selectCount(
                new LambdaQueryWrapper<Reminder>()
                        .eq(Reminder::getElderId, elderId)
                        .eq(Reminder::getIsActive, 1));
    }

    private Long countUnreadFamilyMessages(Long childId, Long elderId) {
        List<Long> familyIds = familyAccessService.activeFamilyIds(elderId);
        if (familyIds.size() <= 1) {
            return 0L;
        }
        return familyMessageMapper.selectCount(
                new LambdaQueryWrapper<FamilyMessage>()
                        .eq(FamilyMessage::getReceiverId, childId)
                        .eq(FamilyMessage::getIsRead, 0)
                        .in(FamilyMessage::getSenderId, familyIds)
                        .and(wrapper -> wrapper
                                .isNull(FamilyMessage::getStatus)
                                .or()
                                .eq(FamilyMessage::getStatus, 0)));
    }

    private String resolveMoodLabel(Integer moodScore) {
        if (moodScore == null || moodScore <= 0) {
            return "🙂";
        }
        if (moodScore <= 2) {
            return "😟";
        }
        if (moodScore == 3) {
            return "🙂";
        }
        if (moodScore == 4) {
            return "😊";
        }
        return "😄";
    }

    private int toInt(Long value) {
        return value != null ? value.intValue() : 0;
    }
}
