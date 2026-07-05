package com.xiaoban.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaoban.server.common.Result;
import com.xiaoban.server.entity.DailyReport;
import com.xiaoban.server.mapper.DailyReportMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "智能日报模块")
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final DailyReportMapper dailyReportMapper;

    @Operation(summary = "获取指定老人日报")
    @GetMapping("/daily")
    public Result<List<DailyReport>> daily(
            @RequestParam Long elderId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LambdaQueryWrapper<DailyReport> query = new LambdaQueryWrapper<DailyReport>()
                .eq(DailyReport::getElderId, elderId)
                .orderByDesc(DailyReport::getReportDate);
        if (date != null) {
            query.eq(DailyReport::getReportDate, date);
        }
        return Result.success(dailyReportMapper.selectList(query));
    }
}
