package com.xiaoban.server.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * [身份认证] JWT 令牌
 */
@Data
@ConfigurationProperties(prefix = "external-services.jwt-auth")
public class JwtAuthProperties implements ExternalServiceConfig {

    private String serviceName;
    private String description;
    private String docUrl;
    private boolean enabled = true;

    private String secret;
    private long expirationMs = 604_800_000L;
}
