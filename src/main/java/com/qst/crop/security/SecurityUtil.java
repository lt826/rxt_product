package com.qst.crop.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全工具类
 */
public final class SecurityUtil {

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    private SecurityUtil() {
    }

    /**
     * 返回当前登录的用户名，未登录时返回null
     */
    public static String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return String.valueOf(auth.getPrincipal());
    }

    /**
     * 加密密码
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    public static String encryptPassword(String rawPassword) {
        return PASSWORD_ENCODER.encode(rawPassword);
    }

    /**
     * 验证密码
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @return 密码是否匹配
     */
    public static boolean matchesPassword(String rawPassword, String encodedPassword) {
        return PASSWORD_ENCODER.matches(rawPassword, encodedPassword);
    }
}
