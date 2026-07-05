package com.xiaoban.server.controller;

import com.xiaoban.server.common.Result;
import com.xiaoban.server.service.ChildDashboardService;
import com.xiaoban.server.vo.ChildDashboardVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Tag(name = "子女端聚合模块")
@RestController
@RequestMapping("/api/child")
@RequiredArgsConstructor
public class ChildController {

    private final ChildDashboardService childDashboardService;

    @Operation(summary = "获取子女首页真实聚合数据")
    @GetMapping("/dashboard")
    public Result<ChildDashboardVO> dashboard(
            @RequestParam Long elderId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication auth) {
        Long childId = (Long) auth.getPrincipal();
        return Result.success(childDashboardService.getDashboard(childId, elderId, date));
    }
}
