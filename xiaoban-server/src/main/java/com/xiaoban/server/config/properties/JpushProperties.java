package com.xiaoban.server.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * [消息推送] 极光推送 JPush
 */
@Data
@ConfigurationProperties(prefix = "external-services.jpush")
public class JpushProperties implements ExternalServiceConfig {

    private String serviceName;
    private String description;
    private String docUrl;
    private boolean enabled = true;

    private String appKey;
    private String masterSecret;
}
