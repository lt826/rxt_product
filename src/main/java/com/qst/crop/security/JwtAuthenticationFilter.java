package com.qst.crop.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * JWT 权限过滤器：从 Bearer Token 请求头解析当前登录用户，写入 SecurityContext
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        String uri = request.getRequestURI();

        log.info("=== JWT 过滤器 ===");
        log.info("请求 URI: {}", uri);
        log.info("Authorization 头存在: {}", StringUtils.hasText(header));

        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            log.info("Token 长度: {}", token.length());
            log.info("Token 前30字符: {}", token.substring(0, Math.min(30, token.length())));

            try {
                if (jwtUtil.validateToken(token)) {
                    String username = jwtUtil.getUserNameFromToken(token);
                    String role = jwtUtil.getRoleFromToken(token);

                    log.info("Token 验证成功");
                    log.info("用户名: {}", username);
                    log.info("角色: {}", role);

                    if (SecurityContextHolder.getContext().getAuthentication() == null) {
                        // 根据角色构建权限列表
                        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                        if (role != null && !role.isEmpty()) {
                            // 添加角色权限，Spring Security 角色需要 ROLE_ 前缀
                            String roleAuthority = "ROLE_" + role.toUpperCase();
                            authorities.add(new SimpleGrantedAuthority(roleAuthority));
                            log.info("添加角色权限: {}", roleAuthority);
                        }
                        // 所有认证用户都有 USER 权限
                        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                        log.info("权限列表: {}", authorities);

                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                username, null, authorities);
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        log.info("认证信息已设置到 SecurityContext");
                    }
                } else {
                    log.warn("Token 验证失败");
                }
            } catch (Exception e) {
                log.error("Token 处理异常: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            }
        } else {
            log.warn("没有有效的 Authorization 头");
        }

        log.info("==================");

        filterChain.doFilter(request, response);
    }
}
