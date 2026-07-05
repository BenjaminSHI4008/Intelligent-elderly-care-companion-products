package com.xiaoban.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaoban.server.common.BusinessException;
import com.xiaoban.server.common.ResultCode;
import com.xiaoban.server.dto.ReminderCreateRequest;
import com.xiaoban.server.entity.Reminder;
import com.xiaoban.server.mapper.ReminderMapper;
<<<<<<< HEAD
import com.xiaoban.server.security.BindingGuard;
=======
>>>>>>> ae5245dd4789e2f7f705836e323616db126ae8e9
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReminderService {

    private final ReminderMapper reminderMapper;
    private final PushService pushService;
<<<<<<< HEAD
    private final BindingGuard bindingGuard;

    public Reminder create(Long childId, ReminderCreateRequest req) {
        // A child may only create reminders for elders they are actively bound to.
        bindingGuard.assertChildBoundToElder(childId, req.getElderId());
=======

    public Reminder create(Long childId, ReminderCreateRequest req) {
>>>>>>> ae5245dd4789e2f7f705836e323616db126ae8e9
        Reminder reminder = Reminder.builder()
                .childId(childId)
                .elderId(req.getElderId())
                .content(req.getContent())
                .remindTime(LocalTime.parse(req.getRemindTime()))
                .repeatType(req.getRepeatType())
                .isActive(1)
                .build();
        reminderMapper.insert(reminder);
        return reminder;
    }

<<<<<<< HEAD
    public List<Reminder> listByElder(Long childId, Long elderId) {
        bindingGuard.assertChildBoundToElder(childId, elderId);
=======
    public List<Reminder> listByElder(Long elderId) {
>>>>>>> ae5245dd4789e2f7f705836e323616db126ae8e9
        return reminderMapper.selectList(
                new LambdaQueryWrapper<Reminder>().eq(Reminder::getElderId, elderId));
    }

    public List<Reminder> todayReminders(Long elderId) {
        return reminderMapper.selectList(
                new LambdaQueryWrapper<Reminder>()
                        .eq(Reminder::getElderId, elderId)
                        .eq(Reminder::getIsActive, 1));
    }

<<<<<<< HEAD
    public void toggle(Long childId, Long id) {
        Reminder reminder = reminderMapper.selectById(id);
        if (reminder == null) throw new BusinessException(ResultCode.NOT_FOUND);
        // Ensure the caller owns (is bound to) the elder this reminder targets.
        bindingGuard.assertChildBoundToElder(childId, reminder.getElderId());
=======
    public void toggle(Long id) {
        Reminder reminder = reminderMapper.selectById(id);
        if (reminder == null) throw new BusinessException(ResultCode.NOT_FOUND);
>>>>>>> ae5245dd4789e2f7f705836e323616db126ae8e9
        reminder.setIsActive(reminder.getIsActive() == 1 ? 0 : 1);
        reminderMapper.updateById(reminder);
    }

<<<<<<< HEAD
    public void delete(Long childId, Long id) {
        Reminder reminder = reminderMapper.selectById(id);
        if (reminder == null) throw new BusinessException(ResultCode.NOT_FOUND);
        bindingGuard.assertChildBoundToElder(childId, reminder.getElderId());
=======
    public void delete(Long id) {
>>>>>>> ae5245dd4789e2f7f705836e323616db126ae8e9
        reminderMapper.deleteById(id);
    }

    @Scheduled(cron = "0 * * * * *")
    public void checkReminders() {
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);
        List<Reminder> reminders = reminderMapper.selectList(
                new LambdaQueryWrapper<Reminder>().eq(Reminder::getIsActive, 1));
        for (Reminder reminder : reminders) {
            LocalTime rt = reminder.getRemindTime().withSecond(0).withNano(0);
            if (rt.equals(now)) {
                pushService.pushToUser(reminder.getElderId(), "小伴提醒", "小伴提醒您，该" + reminder.getContent() + "了", null);
            }
        }
    }
}
