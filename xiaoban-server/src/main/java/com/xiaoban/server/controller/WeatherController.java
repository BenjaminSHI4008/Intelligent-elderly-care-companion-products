package com.xiaoban.server.controller;

import com.xiaoban.server.common.Result;
import com.xiaoban.server.service.WeatherService;
import com.xiaoban.server.vo.WeatherVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "天气模块")
@RestController
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    @Operation(summary = "获取当前天气")
    @GetMapping("/api/weather/current")
    public Result<WeatherVO> current(@RequestParam(required = false) String city) {
        return Result.success(weatherService.getCurrentWeather(city));
    }
}
