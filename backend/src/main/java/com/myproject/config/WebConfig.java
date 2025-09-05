package com.myproject.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(
                        "http://localhost:*",
                        "http://127.0.0.1:*",
                        "http://localhost:5173",
                        "http://localhost:3000"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Value("${file.save-path}") // 注入配置文件中定义的文件保存路径
    private String fileSavePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1) 映射图片上传目录到 /images/**
        java.nio.file.Path absoluteDir = java.nio.file.Paths.get(fileSavePath).normalize();
        String fileLocation = absoluteDir.toUri().toString();
        registry.addResourceHandler("/images/**")
                .addResourceLocations(fileLocation, "classpath:/static/images/");

        // 2) 映射前端静态资源（Vite 打包）
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/assets/");
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/favicon.ico");
        registry.addResourceHandler("/index.html")
                .addResourceLocations("classpath:/static/index.html");
    }

    // 将根路径 / 映射到 index.html（history 模式）
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
        // SPA 前端路由：确保 /login /register /forgot 等路径转发到前端
        registry.addViewController("/login").setViewName("forward:/index.html");
        registry.addViewController("/register").setViewName("forward:/index.html");
        registry.addViewController("/forgot").setViewName("forward:/index.html");
    }
}
