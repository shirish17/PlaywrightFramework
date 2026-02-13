package com.cro.context;

public class RoleContext {
	private static final ThreadLocal<String> ROLE = new ThreadLocal<>();

    private RoleContext() {}

    public static void setRole(String role) {
        ROLE.set(role);
    }

    public static String getRole() {
        String role = ROLE.get();
        if (role == null) {
            throw new RuntimeException("Role not set. Background step must define role.");
        }
        return role;
    }

    public static void clear() {
        ROLE.remove();
    }
}