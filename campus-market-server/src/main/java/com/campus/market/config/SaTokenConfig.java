package com.campus.market.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class SaTokenConfig implements WebMvcConfigurer {

    private final UploadProperties uploadProperties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/**",          // 注册/登录 游客
                        "/api/hello",            // 冒烟测试
                        "/api/categories",       // 分类列表 游客（成员二）
                        "/api/goods",            // 商品列表 游客（成员二）
                        "/api/goods/*"           // 商品详情 游客（成员二）
                        // /api/admin/** 和 /api/user/** 需要登录，不排除
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(uploadProperties.getUrlPrefix() + "/**")
                .addResourceLocations("file:" + uploadProperties.getPath() + "/");
    }
}
