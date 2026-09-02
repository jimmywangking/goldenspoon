package com.example.crm.userauth.module.security;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> ROLE = new ThreadLocal<>();
    private static final ThreadLocal<Long> ORG_ID = new ThreadLocal<>();

    public static void set(Long userId, String role, Long orgId) {
        USER_ID.set(userId);
        ROLE.set(role);
        ORG_ID.set(orgId);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static String getRole() {
        return ROLE.get();
    }

    public static Long getOrgId() {
        return ORG_ID.get();
    }

    public static boolean isAdmin() {
        return "ADMIN".equals(ROLE.get());
    }

    public static boolean isOrgAdmin() {
        return "ORG_ADMIN".equals(ROLE.get());
    }

    public static void clear() {
        USER_ID.remove();
        ROLE.remove();
        ORG_ID.remove();
    }
}
