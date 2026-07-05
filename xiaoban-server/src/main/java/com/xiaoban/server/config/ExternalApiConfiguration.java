package com.xiaoban.server.config;

import com.xiaoban.server.config.properties.AiDashscopeProperties;
import com.xiaoban.server.config.properties.AmapWeatherProperties;
import com.xiaoban.server.config.properties.InfrastructureProperties;
import com.xiaoban.server.config.properties.JpushProperties;
import com.xiaoban.server.config.properties.JwtAuthProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 外部 API 配置注册中心（OCP：新增服务只需增加 Properties 类并在此注册）。
 */
@Configuration
@EnableConfigurationProperties({
        InfrastructureProperties.class,
        AiDashscopeProperties.class,
        JpushProperties.class,
        AmapWeatherProperties.class,
        JwtAuthProperties.class
})
public class ExternalApiConfiguration {
}
