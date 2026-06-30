package com.xiaoban.server.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WeatherVO {
    private String city;
    private String weather;
    private String temperature;
}