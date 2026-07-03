package com.campus.market.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 全局配置（Apifox 一键导入用）
 * @author 成员二
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "AI校园二手交易平台",
        version = "1.0",
        description = "后端接口文档 · Base URL: http://localhost:8080 · 统一返回Result<T>结构"
    )
)
@SecurityScheme(
    name = "Authorization",
    type = SecuritySchemeType.APIKEY,
    in = SecuritySchemeIn.HEADER,
    description = "JWT Token，登录接口获取"
)
public class OpenApiConfig {
}
