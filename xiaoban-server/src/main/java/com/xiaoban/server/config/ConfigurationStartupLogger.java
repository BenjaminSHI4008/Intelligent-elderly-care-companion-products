package com.xiaoban.server.config;

import com.xiaoban.server.config.properties.AiDashscopeProperties;
import com.xiaoban.server.config.properties.AmapWeatherProperties;
import com.xiaoban.server.config.properties.ExternalServiceConfig;
import com.xiaoban.server.config.properties.InfrastructureProperties;
import com.xiaoban.server.config.properties.JpushProperties;
import com.xiaoban.server.config.properties.JwtAuthProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 启动时打印外部服务配置摘要，便于排查环境差异（SRP：仅负责配置可观测性）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConfigurationStartupLogger implements ApplicationRunner {

    private final InfrastructureProperties infrastructure;
    private final AiDashscopeProperties aiDashscope;
    private final JpushProperties jpush;
    private final AmapWeatherProperties amapWeather;
    private final JwtAuthProperties jwtAuth;

    @Override
    public void run(ApplicationArguments args) {
        log.info("========== 小伴配置加载摘要 (config.yaml) ==========");
        log.info("数据库: {}:{}/{}", infrastructure.getDatabase().getHost(),
                infrastructure.getDatabase().getPort(), infrastructure.getDatabase().getName());
        log.info("服务端口: {}", infrastructure.getServer().getPort());

        List<ExternalServiceConfig> services = List.of(aiDashscope, jpush, amapWeather, jwtAuth);
        for (ExternalServiceConfig service : services) {
            logServiceStatus(service);
        }
        log.info("客户端 SDK 对照见 config.yaml -> client-sdk-reference");
        log.info("==================================================");
    }

    private void logServiceStatus(ExternalServiceConfig service) {
        String status = service.isEnabled() ? "已启用" : "已禁用";
        log.info("[{}] {} — {}", service.getServiceName(), status, service.getDescription());
        if (service.isEnabled() && isPlaceholder(service)) {
            log.warn("  ⚠ {} 可能未正确配置，请检查 config.yaml 或环境变量", service.getServiceName());
        }
    }

    private boolean isPlaceholder(ExternalServiceConfig service) {
        if (service instanceof AiDashscopeProperties ai) {
            return !StringUtils.hasText(ai.getApiKey()) || ai.getApiKey().startsWith("your-");
        }
        if (service instanceof JpushProperties push) {
            return !StringUtils.hasText(push.getAppKey()) || push.getAppKey().startsWith("your-");
        }
        if (service instanceof AmapWeatherProperties weather) {
            return !StringUtils.hasText(weather.getApiKey()) || weather.getApiKey().startsWith("your-");
        }
        if (service instanceof JwtAuthProperties jwt) {
            return !StringUtils.hasText(jwt.getSecret()) || jwt.getSecret().startsWith("your-");
        }
        return false;
    }
}
