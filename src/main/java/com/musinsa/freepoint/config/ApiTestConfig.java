package com.musinsa.freepoint.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Setter
@Getter
@ConfigurationProperties(prefix = "api.test")
public class ApiTestConfig {
    private String apiKey;
    private String musinsaId;
}
