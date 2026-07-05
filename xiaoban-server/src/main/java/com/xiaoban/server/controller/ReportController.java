package com.xiaoban.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaoban.server.common.Result;
import com.xiaoban.server.entity.DailyReport;
import com.xiaoban.server.mapper.DailyReportMapper;
import com.xiaoban.server.service.DailyReportService;
import com.xiaoban.server.service.FamilyAccessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "智能日报模块")
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final DailyReportMapper dailyReportMapper;
    private final DailyReportService dailyReportService;
    private final FamilyAccessService familyAccessService;

    @Operation(summary = "获取指定老人日报")
    @GetMapping("/daily")
    public Result<List<DailyReport>> daily(
            @RequestParam Long elderId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        familyAccessService.assertCanAccessElder(userId, elderId);

        LambdaQueryWrapper<DailyReport> query = new LambdaQueryWrapper<DailyReport>()
                .eq(DailyReport::getElderId, elderId)
                .orderByDesc(DailyReport::getReportDate)
                .orderByDesc(DailyReport::getId);
        if (date != null) {
            query.eq(DailyReport::getReportDate, date);
        }
        return Result.success(dailyReportMapper.selectList(query));
    }

    @Operation(summary = "基于真实对话生成或更新指定日期日报")
    @PostMapping("/daily/generate")
    public Result<DailyReport> generate(
            @RequestParam Long elderId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        familyAccessService.assertCanAccessElder(userId, elderId);
        return Result.success(dailyReportService.generateOrUpdateReport(
                elderId,
                date != null ? date : LocalDate.now()));
    }
}
