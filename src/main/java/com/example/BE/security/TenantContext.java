package com.example.BE.security;

/**
 * Thread-local context for holding the current active Tenant identifier.
 * Used for tenant isolation in Shared SaaS mode and transparent passthrough in Dedicated mode.
 */
public final class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();
    private static volatile boolean dedicatedMode = false;
    private static volatile String defaultTenantCode = "DEFAULT";

    private TenantContext() {}

    public static void setTenant(String tenantCode) {
        CURRENT_TENANT.set(tenantCode);
    }

    public static String getTenant() {
        String tenant = CURRENT_TENANT.get();
        return tenant != null ? tenant : defaultTenantCode;
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }

    public static boolean isDedicatedMode() {
        return dedicatedMode;
    }

    public static void setDedicatedMode(boolean isDedicated) {
        dedicatedMode = isDedicated;
    }

    public static void setDefaultTenantCode(String code) {
        if (code != null && !code.isBlank()) {
            defaultTenantCode = code;
        }
    }
}
