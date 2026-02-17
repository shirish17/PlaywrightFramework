package com.cro.context;

/**
 * ThreadLocal storage for current role.
 */
public class RoleContext {
    
    private static final ThreadLocal<String> roleThreadLocal = new ThreadLocal<>();

    private RoleContext() {}

    public static void setRole(String role) {
        roleThreadLocal.set(role);
    }

    public static String getRole() {
        String role = roleThreadLocal.get();
        if (role == null) {
            throw new IllegalStateException("Role not set for current thread");
        }
        return role;
    }

    public static void clear() {
        roleThreadLocal.remove();
    }
}