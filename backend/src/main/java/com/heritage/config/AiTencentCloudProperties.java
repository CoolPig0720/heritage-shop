package com.heritage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai.tencentcloud")
public class AiTencentCloudProperties {

    private String secretId;

    private String secretKey;

    private String region;

    private String endpoint;
}

