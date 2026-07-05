package com.xiaoban.server.controller;

import com.xiaoban.server.common.Result;
import com.xiaoban.server.dto.ReminderCreateRequest;
import com.xiaoban.server.entity.Reminder;
import com.xiaoban.server.service.ReminderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "远程提醒模块")
@RestController
@RequestMapping("/api/reminder")
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;

    @Operation(summary = "创建提醒（子女调用）")
    @PostMapping("/create")
    public Result<Reminder> create(@Valid @RequestBody ReminderCreateRequest req, Authentication auth) {
        Long childId = (Long) auth.getPrincipal();
        return Result.success(reminderService.create(childId, req));
    }

    @Operation(summary = "获取提醒列表")
    @GetMapping("/list")
    public Result<List<Reminder>> list(@RequestParam Long elderId) {
        return Result.success(reminderService.listByElder(elderId));
    }

    @Operation(summary = "获取今日提醒（老人端调用）")
    @GetMapping("/today")
    public Result<List<Reminder>> today(Authentication auth) {
        Long elderId = (Long) auth.getPrincipal();
        return Result.success(reminderService.todayReminders(elderId));
    }

    @Operation(summary = "启用/禁用提醒")
    @PutMapping("/{id}/toggle")
    public Result<Void> toggle(@PathVariable Long id) {
        reminderService.toggle(id);
        return Result.success();
    }

    @Operation(summary = "删除提醒")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        reminderService.delete(id);
        return Result.success();
    }
}
