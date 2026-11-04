package com.integrity.auth.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 当前登录人读取工具。
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /** 取当前用户；未认证返回 null。 */
    public static UserPrincipal current() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal p) {
            return p;
        }
        return null;
    }

    public static Long currentUserId() {
        UserPrincipal p = current();
        return p == null ? null : p.userId();
    }

    public static String currentRole() {
        UserPrincipal p = current();
        return p == null ? null : p.role();
    }
}
