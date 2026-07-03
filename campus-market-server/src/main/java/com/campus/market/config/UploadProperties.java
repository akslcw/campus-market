package com.campus.market.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 文件上传配置
 * @author 成员二
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "upload")
public class UploadProperties {
    private String path = "./upload";
    private String urlPrefix = "/static";
}
