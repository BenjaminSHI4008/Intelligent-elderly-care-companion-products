package com.xiaoban.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaoban.server.entity.BindingRelation;
import com.xiaoban.server.entity.Conversation;
import com.xiaoban.server.entity.DailyReport;
import com.xiaoban.server.entity.FamilyMessage;
import com.xiaoban.server.entity.Reminder;
import com.xiaoban.server.entity.User;
import com.xiaoban.server.mapper.BindingRelationMapper;
import com.xiaoban.server.mapper.ConversationMapper;
import com.xiaoban.server.mapper.DailyReportMapper;
import com.xiaoban.server.mapper.FamilyMessageMapper;
import com.xiaoban.server.mapper.ReminderMapper;
import com.xiaoban.server.mapper.UserMapper;
import com.xiaoban.server.security.BindingGuard;
import com.xiaoban.server.vo.ChildDashboardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Aggregates the child-home dashboard purely from real tables.
 *
 * <p>Privacy rule: only conversations with {@code isPrivate=0} contribute to any
 * count exposed to the child. Access is gated by {@link BindingGuard} so a child
 * can only read elders they are actively bound to.
 */
@Service
@RequiredArgsConstructor
public class ChildDashboardService {

    private static final String STATUS_ACTIVE = "active";
    private static final String CONFIRM_PENDING = "pending";
    private static final int PRIVACY_PUBLIC = 0;
    private static final int READ_UNREAD = 0;
    private static final int REMINDER_ACTIVE = 1;

    private final BindingGuard bindingGuard;
    private final UserMapper userMapper;
    private final ConversationMapper conversationMapper;
    private final DailyReportMapper dailyReportMapper;
    private final ReminderMapper reminderMapper;
    private final FamilyMessageMapper familyMessageMapper;
    private final BindingRelationMapper bindingRelationMapper;

    public ChildDashboardVO getDashboard(Long childId, Long elderId, LocalDate date) {
        bindingGuard.assertChildBoundToElder(childId, elderId);

        LocalDate day = date != null ? date : LocalDate.now();
        LocalDateTime dayStart = day.atStartOfDay();
        LocalDateTime dayEnd = day.plusDays(1).atStartOfDay();

        User elder = userMapper.selectById(elderId);

        List<Conversation> todayConversations = conversationMapper.selectList(
                new LambdaQueryWrapper<Conversation>()
                        .eq(Conversation::getElderId, elderId)
                        .eq(Conversation::getIsPrivate, PRIVACY_PUBLIC)
                        .between(Conversation::getCreatedAt, dayStart, dayEnd));

        LocalDateTime lastActiveAt = todayConversations.stream()
                .map(Conversation::getCreatedAt)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null);

        int pendingCount = (int) todayConversations.stream()
                .filter(c -> CONFIRM_PENDING.equals(c.getConfirmStatus()))
                .count();

        DailyReport report = dailyReportMapper.selectOne(
                new LambdaQueryWrapper<DailyReport>()
                        .eq(DailyReport::getElderId, elderId)
                        .eq(DailyReport::getReportDate, day)
                        .orderByDesc(DailyReport::getCreatedAt)
                        .last("LIMIT 1"));

        Long activeReminders = reminderMapper.selectCount(
                new LambdaQueryWrapper<Reminder>()
                        .eq(Reminder::getElderId, elderId)
                        .eq(Reminder::getIsActive, REMINDER_ACTIVE));

        int unreadMessageCount = countFamilyUnread(childId, elderId);

        return ChildDashboardVO.builder()
                .elderId(elderId)
                .elderNickname(elder != null ? elder.getNickname() : "")
                .todayChatCount(todayConversations.size())
                .lastActiveAt(lastActiveAt)
                .pendingCount(pendingCount)
                .summaryText(report != null ? report.getSummaryText() : null)
                .moodScore(report != null ? report.getMoodScore() : null)
                .activeReminderCount(activeReminders != null ? activeReminders.intValue() : 0)
                .unreadMessageCount(unreadMessageCount)
                .build();
    }

    /** Unread family messages for this child sent by any other family member of the elder. */
    private int countFamilyUnread(Long childId, Long elderId) {
        List<Long> familyIds = resolveFamilyIds(elderId);
        Long count = familyMessageMapper.selectCount(
                new LambdaQueryWrapper<FamilyMessage>()
                        .eq(FamilyMessage::getReceiverId, childId)
                        .eq(FamilyMessage::getIsRead, READ_UNREAD)
                        .in(FamilyMessage::getSenderId, familyIds)
                        .ne(FamilyMessage::getSenderId, childId));
        return count != null ? count.intValue() : 0;
    }

    private List<Long> resolveFamilyIds(Long elderId) {
        List<Long> familyIds = new ArrayList<>();
        familyIds.add(elderId);
        bindingRelationMapper.selectList(
                        new LambdaQueryWrapper<BindingRelation>()
                                .eq(BindingRelation::getElderId, elderId)
                                .eq(BindingRelation::getStatus, STATUS_ACTIVE)).stream()
                .map(BindingRelation::getChildId)
                .filter(Objects::nonNull)
                .forEach(familyIds::add);
        return familyIds;
    }
}
