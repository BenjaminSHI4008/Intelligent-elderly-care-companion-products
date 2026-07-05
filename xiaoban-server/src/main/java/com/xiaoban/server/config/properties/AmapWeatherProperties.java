package com.xiaoban.server.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * [天气服务] 高德地图 Web 服务 API
 */
@Data
@ConfigurationProperties(prefix = "external-services.amap-weather")
public class AmapWeatherProperties implements ExternalServiceConfig {

    private String serviceName;
    private String description;
    private String docUrl;
    private boolean enabled = true;

    private String apiKey;
    private String defaultCityCode;
}
