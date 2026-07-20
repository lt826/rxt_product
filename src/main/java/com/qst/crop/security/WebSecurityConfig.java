package com.qst.crop.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // CSRF
                .csrf(csrf -> csrf.disable())

                // 会话管理
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 权限控制
                .authorizeHttpRequests(auth -> auth
                        // 公开接口：登录、注册
                        .requestMatchers("/api/v1/user/login").permitAll()
                        .requestMatchers("/api/v1/user/register").permitAll()

                        // 上传的图片静态资源：允许匿名访问（未登录浏览商品列表时图片也要能展示）
                        .requestMatchers("/uploads/**").permitAll()

                        // 商品浏览：所有用户都可以访问（包括未登录）
                        .requestMatchers("/api/v1/goods/list").permitAll()
                        .requestMatchers("/api/v1/goods/detail/**").permitAll()

                        // 商品发布：农户可以发布销售商品(type=1)，买家可以发布收购需求(type=2)
                        .requestMatchers("/api/v1/goods/publish").hasAnyRole("FARMER", "BUYER", "ADMIN")
                        .requestMatchers("/api/v1/goods/update").hasAnyRole("FARMER", "BUYER", "ADMIN")
                        .requestMatchers("/api/v1/goods/delete/**").hasAnyRole("FARMER", "BUYER", "ADMIN")
                        .requestMatchers("/api/v1/goods/my").hasAnyRole("FARMER", "BUYER", "ADMIN")

                        // 买家和农户都可以访问自己的购物车相关
                        .requestMatchers("/api/v1/cart/**").hasAnyRole("BUYER", "FARMER", "ADMIN")

                        // 消息和农户列表：需要登录即可
                        .requestMatchers("/api/v1/message/**").authenticated()
                        .requestMatchers("/api/v1/farmers").authenticated()

                        // 买家工作台：需要 BUYER 或 ADMIN 角色
                        .requestMatchers("/api/v1/buyer/**").hasAnyRole("BUYER", "ADMIN")

                        // 首页精选内容：列表公开（未登录访客可看），配置仅管理员
                        .requestMatchers("/api/v1/homepage-feature/list").permitAll()
                        .requestMatchers("/api/v1/homepage-feature/section/**").hasRole("ADMIN")

                        // 认证接口：其他所有请求都需要登录
                        .anyRequest().authenticated()
                )

                // JWT 过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // 禁用表单登录和 HTTP Basic
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }
}
