package com.xiaoban.server.config.properties;

/**
 * 外部服务配置通用契约（ISP：各服务配置类按需实现）。
 */
public interface ExternalServiceConfig {

    String getServiceName();

    String getDescription();

    String getDocUrl();

    boolean isEnabled();
}
