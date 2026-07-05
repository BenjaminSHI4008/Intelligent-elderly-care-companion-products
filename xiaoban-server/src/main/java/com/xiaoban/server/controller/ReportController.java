package com.xiaoban.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaoban.server.common.Result;
import com.xiaoban.server.entity.DailyReport;
import com.xiaoban.server.mapper.DailyReportMapper;
<<<<<<< HEAD
import com.xiaoban.server.security.BindingGuard;
=======
>>>>>>> ae5245dd4789e2f7f705836e323616db126ae8e9
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
<<<<<<< HEAD
import org.springframework.security.core.Authentication;
=======
>>>>>>> ae5245dd4789e2f7f705836e323616db126ae8e9
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "智能日报模块")
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final DailyReportMapper dailyReportMapper;
<<<<<<< HEAD
    private final BindingGuard bindingGuard;
=======
>>>>>>> ae5245dd4789e2f7f705836e323616db126ae8e9

    @Operation(summary = "获取指定老人日报")
    @GetMapping("/daily")
    public Result<List<DailyReport>> daily(
            @RequestParam Long elderId,
<<<<<<< HEAD
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication auth) {
        Long childId = (Long) auth.getPrincipal();
        bindingGuard.assertChildBoundToElder(childId, elderId);
=======
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
>>>>>>> ae5245dd4789e2f7f705836e323616db126ae8e9
        LambdaQueryWrapper<DailyReport> query = new LambdaQueryWrapper<DailyReport>()
                .eq(DailyReport::getElderId, elderId)
                .orderByDesc(DailyReport::getReportDate);
        if (date != null) {
            query.eq(DailyReport::getReportDate, date);
        }
        return Result.success(dailyReportMapper.selectList(query));
    }
}
