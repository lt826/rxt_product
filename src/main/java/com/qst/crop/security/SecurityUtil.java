package com.qst.crop.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 鑾峰彇褰撳墠鐧诲綍鐢ㄦ埛鍚嶇殑宸ュ叿绫?
 */
public final class SecurityUtil {

    private SecurityUtil() {
    }

    /**
     * 杩斿洖褰撳墠鐧诲綍鐨勭敤鎴峰悕锛屾湭鐧诲綍鏃惰繑鍥?null
     */
    public static String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return String.valueOf(auth.getPrincipal());
    }
}
