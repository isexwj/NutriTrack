package com.myproject.config;

import com.myproject.config.JwtAuthenticationFilter; // 添加导入
import com.myproject.utils.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // 添加导入

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthFilter; // 添加依赖注入

    // 修改为一个构造函数，包含所有必需的参数
    public SecurityConfig(JwtUtil jwtUtil, UserDetailsService userDetailsService, JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.jwtAuthFilter = jwtAuthFilter; // 初始化jwtAuthFilter
    }

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configure(http))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    // 前端静态资源放在 classpath:/static/**
                    .requestMatchers(
                            "/",
                            "/index.html",
                            "/favicon.ico",
                            "/assets/**",
                            "/images/**"
                    ).permitAll()
                    .requestMatchers(
                            "/user/login",
                            "/user/captcha",
                            "/user/register",
                            "/user/register/step1",
                            "/user/register/email/send",
                            "/user/register/step2",
                            "/user/forget-password",
                            "/user/forget-password/send",
                            "/user/forget-password/reset"
                    ).permitAll()
                    .requestMatchers("/swagger-ui.html", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                    .requestMatchers("/images/**").permitAll()
                    .requestMatchers("/api/ai/**").permitAll()
                    .requestMatchers("/ai/**").permitAll()
                    .anyRequest().authenticated()
            )
            // 添加JWT过滤器到过滤器链中
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
