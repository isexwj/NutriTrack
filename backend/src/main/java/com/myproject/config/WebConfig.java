package com.myproject.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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
        // 统一解析静态资源目录为绝对路径，避免分隔符和相对路径差异
        java.nio.file.Path absoluteDir = java.nio.file.Paths.get(System.getProperty("user.dir")).resolve(fileSavePath).normalize();

        // 使用 file URI，Spring 对 file:URI 解析更稳定
        String fileLocation = absoluteDir.toUri().toString();

        // 映射本地文件夹
        // 访问 URL: http://localhost:8080/images/** 将指向到上述目录
        registry.addResourceHandler("/images/**")
                // 优先本地文件系统目录（用于用户上传/编辑后的文件）
                .addResourceLocations(fileLocation,
                        // 兼容打包后的类路径静态资源（项目内置图片）
                        "classpath:/static/images/");
    }
}
