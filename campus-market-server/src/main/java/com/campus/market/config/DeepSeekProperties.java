package com.campus.market.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "deepseek")
public class DeepSeekProperties {
    private String apiKey;
    private String baseUrl;
    private String model;
    private Integer timeout = 15000;
}