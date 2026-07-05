package com.xiaoban.server.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * [AI 对话] 阿里云通义千问 DashScope
 */
@Data
@ConfigurationProperties(prefix = "external-services.ai-dashscope")
public class AiDashscopeProperties implements ExternalServiceConfig {

    private String serviceName;
    private String description;
    private String docUrl;
    private boolean enabled = true;

    private String apiKey;
    private String baseUrl;
    private String model;
    private int maxTokens = 500;
    private double temperature = 0.7;
}
